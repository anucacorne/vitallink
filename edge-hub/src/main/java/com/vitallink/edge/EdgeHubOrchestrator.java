package com.vitallink.edge;

import com.vitallink.edge.filter.EdgeFilterEngine;
import com.vitallink.edge.filter.EdgeFilterEngine.*;
import com.vitallink.edge.sensor.IoTSensorSimulator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/*
 Orchestratorul principal al Edge Hub-ului (Nivel 2).
  Coordonează:
  1. Primirea datelor de la simulatorul IoT (Nivel 1)
  2. Aplicarea filtrelor Edge (EdgeFilterEngine)
  3. Buffering local când conexiunea Cloud este indisponibilă
  4. Transmiterea mesajelor filtrate prin Kafka către Cloud (Nivel 3)
  5. Alertele locale independente de conexiunea la Cloud
 */
@Service
public class EdgeHubOrchestrator {

    private static final Logger log = Logger.getLogger(EdgeHubOrchestrator.class.getName());

    private static final int LOCAL_BUFFER_CAPACITY = 1000;

    private final EdgeFilterEngine filterEngine;
    private final BlockingQueue<FilterDecision> localBuffer;
    private volatile boolean cloudConnected = true;
    private final List<IoTSensorSimulator> activeSimulators = new ArrayList<>();
    private final KafkaProducerService kafkaProducer;

    public EdgeHubOrchestrator(KafkaProducerService kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.filterEngine  = new EdgeFilterEngine();
        this.localBuffer   = new ArrayBlockingQueue<>(LOCAL_BUFFER_CAPACITY);
    }

    
      //Înregistrează un transport nou și pornește simulatorul de senzori.
     
    public IoTSensorSimulator registerShipment(String shipmentId) {
        IoTSensorSimulator simulator = new IoTSensorSimulator(shipmentId, this::onSensorReading);
        activeSimulators.add(simulator);
        simulator.start();
        log.info("[EDGE HUB] Transport înregistrat: " + shipmentId);
        return simulator;
    }

    
     // Callback apelat de fiecare senzor la 500ms.
      //Aplică logica de filtrare și direcționează mesajul.
     
    private void onSensorReading(TelemetryReading reading) {
        FilterDecision decision = filterEngine.evaluate(reading);

        if (!decision.shouldForwardToCloud()) {
            return;
        }

        if (decision.isCritical()) {
            handleLocalAlert(decision);
        }

        if (cloudConnected) {
            forwardToCloud(decision);
            flushLocalBuffer();
        } else {
            bufferLocally(decision);
        }
    }

    
     // Alertă locală IMEDIATĂ — nu depinde de conexiunea Cloud.
    private void handleLocalAlert(FilterDecision decision) {
        log.severe(String.format(
                "[EDGE ALERT LOCAL] %s | Sensor: %s | Motiv: %s",
                decision.action(),
                decision.reading().sensorId(),
                decision.explanation()
        ));
    }

     //Transmite mesajul filtrat în Kafka.
     
    private void forwardToCloud(FilterDecision decision) {
        log.info(String.format(
                "[EDGE -> KAFKA] Shipment: %s | Motiv: %s",
                decision.reading().shipmentId(), decision.reason()
        ));
        kafkaProducer.publish(decision);
    }


     // Stochează local în caz de pierdere semnal GSM.
     
    private void bufferLocally(FilterDecision decision) {
        boolean added = localBuffer.offer(decision);
        if (!added) {
            log.warning("[EDGE BUFFER] Buffer plin! Mesaj pierdut pentru: "
                    + decision.reading().shipmentId());
        } else {
            log.info(String.format("[EDGE BUFFER] Mesaj buffered. Dimensiune: %d/%d",
                    localBuffer.size(), LOCAL_BUFFER_CAPACITY));
        }
    }

      //Golește buffer-ul după revenirea conexiunii Cloud.
    private void flushLocalBuffer() {
        if (localBuffer.isEmpty()) return;
        int flushed = 0;
        FilterDecision buffered;
        while ((buffered = localBuffer.poll()) != null) {
            forwardToCloud(buffered);
            flushed++;
        }
        if (flushed > 0) {
            log.info("[EDGE BUFFER] Buffer golit: " + flushed + " mesaje trimise.");
        }
    }

     // Afișează metricile de performanță la fiecare 60 de secunde.
    @Scheduled(fixedDelay = 60_000)
    public void logPerformanceMetrics() {
        PerformanceMetrics metrics = filterEngine.getMetrics();
        log.info(metrics.toString());
    }

    public void setCloudConnected(boolean connected) {
        this.cloudConnected = connected;
        log.info("[EDGE HUB] Stare conexiune Cloud: " + (connected ? "ONLINE" : "OFFLINE"));
        if (connected) flushLocalBuffer();
    }

    public PerformanceMetrics getMetrics() {
        return filterEngine.getMetrics();
    }

    public int getBufferSize() {
        return localBuffer.size();
    }
}
