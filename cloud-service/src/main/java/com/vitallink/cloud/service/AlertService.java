package com.vitallink.cloud.service;

import com.vitallink.cloud.entity.Alert;
import com.vitallink.cloud.entity.TelemetryEvent;
import com.vitallink.cloud.entity.Transport;
import com.vitallink.cloud.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class AlertService {

    private static final Logger log = Logger.getLogger(AlertService.class.getName());
    private final AlertRepository alertRepo;

    public AlertService(AlertRepository alertRepo) {
        this.alertRepo = alertRepo;
    }

    public void checkTemperatureAlert(Transport transport, TelemetryEvent event) {
        BigDecimal temp = event.getTemperatureCelsius();
        if (temp == null) return;

        var profile = transport.getResourceProfile();
        if (profile == null) return;

        boolean exceeded = temp.compareTo(profile.getTempMaxCelsius()) > 0;
        boolean below = temp.compareTo(profile.getTempMinCelsius()) < 0;
        if (!exceeded && !below) return;

        Alert alert = new Alert();
        alert.setTransport(transport);
        alert.setSeverity("HIGH");
        alert.setAlertType(exceeded ? "TEMP_EXCEEDED" : "TEMP_BELOW");
        alert.setMessage(String.format(
                "Temperatură %.1f°C %s intervalul sigur [%.1f, %.1f]°C",
                temp,
                exceeded ? "depășește" : "este sub",
                profile.getTempMinCelsius(),
                profile.getTempMaxCelsius()
        ));
        alert.setTriggerValue(temp);
        alert.setThresholdValue(exceeded ? profile.getTempMaxCelsius() : profile.getTempMinCelsius());
        alert.setLatitude(event.getLatitude());
        alert.setLongitude(event.getLongitude());

        alertRepo.save(alert);
        log.warning(String.format("[ALERT] %s pentru transportul %s", alert.getAlertType(), transport.getId()));
    }

    public void saveCriticalAlert(Transport transport, Map<String, Object> payload) {
        Alert alert = new Alert();
        alert.setTransport(transport);
        alert.setSeverity("CRITICAL");
        alert.setAlertType("TEMP_EXCEEDED");
        alert.setMessage((String) payload.get("explanation"));
        alert.setTriggerValue(toBigDecimal(payload.get("temperature")));
        alert.setLatitude(toBigDecimal(payload.get("latitude")));
        alert.setLongitude(toBigDecimal(payload.get("longitude")));
        alert.setAcknowledged(false);

        alertRepo.save(alert);
        log.warning("[ALERT SERVICE] Alertă critică salvată pentru: " + transport.getId());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(value.toString()); }
        catch (Exception e) { return null; }
    }
}