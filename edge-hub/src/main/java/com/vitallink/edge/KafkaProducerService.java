package com.vitallink.edge;

import com.vitallink.edge.filter.EdgeFilterEngine.FilterDecision;
import com.vitallink.edge.filter.EdgeFilterEngine.TelemetryReading;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.logging.Logger;

/*
 Serviciu de publicare evenimente filtrate în Apache Kafka.
 Înlocuiește TODO-ul MQTT din EdgeHubOrchestrator cu o implementare reală Kafka.
 Topicuri utilizate:
 - telemetry.filtered  → date normale filtrate de Edge
 - alerts.critical     → alerte critice (temperatură, impact)
 */
@Service
public class KafkaProducerService {

    private static final Logger log = Logger.getLogger(KafkaProducerService.class.getName());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${vitallink.kafka.topic.telemetry}")
    private String telemetryTopic;

    @Value("${vitallink.kafka.topic.alerts}")
    private String alertsTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

 
      //Publică o decizie de filtrare în topicul Kafka corespunzător.
      //Cheia mesajului = shipmentId (garantează ordinea per transport).
    public void publish(FilterDecision decision) {
        String topic = decision.isCritical() ? alertsTopic : telemetryTopic;
        String key   = decision.reading().shipmentId();

        Map<String, Object> payload = buildPayload(decision);

        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.severe(String.format(
                                "[KAFKA] Eroare publicare pe %s: %s", topic, ex.getMessage()));
                    } else {
                        log.info(String.format(
                                "[KAFKA] Publicat pe %s | Shipment: %s | Offset: %d",
                                topic, key,
                                result.getRecordMetadata().offset()));
                    }
                });
    }

      //Construiește payload-ul JSON pentru mesajul Kafka.

    private Map<String, Object> buildPayload(FilterDecision decision) {
        TelemetryReading r = decision.reading();
        return Map.ofEntries(
                Map.entry("sensorId",     r.sensorId()),
                Map.entry("shipmentId",   r.shipmentId()),
                Map.entry("temperature",  r.temperatureCelsius() != null ? r.temperatureCelsius() : 0.0),
                Map.entry("acceleration", r.accelerationG()      != null ? r.accelerationG()      : 0.0),
                Map.entry("latitude",     r.latitude()           != null ? r.latitude()            : 0.0),
                Map.entry("longitude",    r.longitude()          != null ? r.longitude()           : 0.0),
                Map.entry("timestamp",    r.timestamp().toString()),
                Map.entry("action",       decision.action().name()),
                Map.entry("reason",       decision.reason().name()),
                Map.entry("isCritical",   decision.isCritical()),
                Map.entry("explanation",  decision.explanation())
        );
    }
}
