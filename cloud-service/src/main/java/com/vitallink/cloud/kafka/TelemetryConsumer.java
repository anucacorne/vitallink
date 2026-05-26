package com.vitallink.cloud.kafka;

import com.vitallink.cloud.entity.Alert;
import com.vitallink.cloud.entity.TelemetryEvent;
import com.vitallink.cloud.entity.Transport;
import com.vitallink.cloud.entity.ResourceProfile;
import com.vitallink.cloud.repository.AlertRepository;
import com.vitallink.cloud.repository.SensorRepository;
import com.vitallink.cloud.repository.TelemetryEventRepository;
import com.vitallink.cloud.repository.TransportRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class TelemetryConsumer {

    private static final Logger log = Logger.getLogger(TelemetryConsumer.class.getName());

    private final TelemetryEventRepository telemetryRepo;
    private final AlertRepository alertRepo;
    private final TransportRepository transportRepo;
    private final SensorRepository sensorRepo;

    public TelemetryConsumer(TelemetryEventRepository telemetryRepo,
                             AlertRepository alertRepo,
                             TransportRepository transportRepo,
                             SensorRepository sensorRepo) {
        this.telemetryRepo = telemetryRepo;
        this.alertRepo = alertRepo;
        this.transportRepo = transportRepo;
        this.sensorRepo = sensorRepo;
    }

    @KafkaListener(
            topics = "${vitallink.kafka.topic.telemetry}",
            groupId = "cloud-service-telemetry",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTelemetry(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> payload = record.value();
            String shipmentId = (String) payload.get("shipmentId");

            log.info(String.format("[KAFKA CONSUMER] Telemetrie primită | Shipment: %s | Offset: %d",
                    shipmentId, record.offset()));

            Transport transport = transportRepo.findById(UUID.fromString(shipmentId)).orElse(null);
            if (transport == null) {
                log.warning("[KAFKA CONSUMER] Transport negăsit pentru ID: " + shipmentId);
                return;
            }

            TelemetryEvent event = new TelemetryEvent();
            event.setTransport(transport);
            event.setLatitude(toBigDecimal(payload.get("latitude")));
            event.setLongitude(toBigDecimal(payload.get("longitude")));
            event.setTemperatureCelsius(toBigDecimal(payload.get("temperature")));
            event.setEventTimestamp(Instant.parse((String) payload.get("timestamp")));
            event.setReceivedAt(Instant.now());

            String sensorId = (String) payload.get("sensorId");
            if (sensorId != null) {
                sensorRepo.findByDeviceId(sensorId).ifPresent(event::setSensor);
            }

            telemetryRepo.save(event);
            checkTemperatureAlert(transport, event);

        } catch (Exception e) {
            log.severe("[KAFKA CONSUMER] Eroare procesare mesaj: " + e.getMessage());
        }
    }

    @KafkaListener(
            topics = "${vitallink.kafka.topic.alerts}",
            groupId = "cloud-service-alerts",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCriticalAlert(ConsumerRecord<String, Map<String, Object>> record) {
        try {
            Map<String, Object> payload = record.value();
            String shipmentId = (String) payload.get("shipmentId");

            log.warning(String.format("[KAFKA CONSUMER] Alertă critică primită | Shipment: %s", shipmentId));

            Transport transport = transportRepo.findById(UUID.fromString(shipmentId)).orElse(null);
            if (transport == null) return;

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
            log.warning("[KAFKA CONSUMER] Alertă critică salvată în BD pentru: " + shipmentId);

        } catch (Exception e) {
            log.severe("[KAFKA CONSUMER] Eroare procesare alertă critică: " + e.getMessage());
        }
    }

    private void checkTemperatureAlert(Transport transport, TelemetryEvent event) {
        BigDecimal temp = event.getTemperatureCelsius();
        if (temp == null) return;

        ResourceProfile profile = transport.getResourceProfile();
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

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(value.toString()); }
        catch (Exception e) { return null; }
    }
}