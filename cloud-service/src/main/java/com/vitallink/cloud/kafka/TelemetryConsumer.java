package com.vitallink.cloud.kafka;

import com.vitallink.cloud.entity.TelemetryEvent;
import com.vitallink.cloud.entity.Transport;
import com.vitallink.cloud.repository.AlertRepository;
import com.vitallink.cloud.repository.SensorRepository;
import com.vitallink.cloud.repository.TelemetryEventRepository;
import com.vitallink.cloud.repository.TransportRepository;
import com.vitallink.cloud.service.AlertService;
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
    private final TransportRepository transportRepo;
    private final SensorRepository sensorRepo;
    private final AlertService alertService;

    public TelemetryConsumer(TelemetryEventRepository telemetryRepo,
                             TransportRepository transportRepo,
                             SensorRepository sensorRepo, AlertService alertService) {
        this.telemetryRepo = telemetryRepo;
        this.transportRepo = transportRepo;
        this.sensorRepo = sensorRepo;
        this.alertService = alertService;
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
            alertService.checkTemperatureAlert(transport, event);

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

            alertService.saveCriticalAlert(transport, payload);

        } catch (Exception e) {
            log.severe("[KAFKA CONSUMER] Eroare procesare alertă critică: " + e.getMessage());
        }
    }


    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(value.toString()); }
        catch (Exception e) { return null; }
    }
}
