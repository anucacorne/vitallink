package com.vitallink.edge.sensor;

import com.vitallink.edge.filter.EdgeFilterEngine.TelemetryReading;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

/*
  Simulator IoT Senzori - Nivel 1
  Simulează un nod fizic IoT montat pe unitatea de transport medical.
  Generează date de temperatură și accelerometru la fiecare 500ms.
 */
public class IoTSensorSimulator {

    private static final Logger log = Logger.getLogger(IoTSensorSimulator.class.getName());

    private static final int    EMIT_INTERVAL_MS  = 500;   // Nivel 1: 500ms interval
    private static final double BASE_TEMPERATURE  = 4.0;   // °C - temperatură normală transport organ
    private static final double BASE_ACCELERATION = 0.3;   // G - vibrații normale transport

    // Ruta simulată: București -> Cluj-Napoca (coordonate aproximative)
    private static final double START_LAT = 44.4268;
    private static final double START_LNG = 26.1025;
    private static final double END_LAT   = 46.7712;
    private static final double END_LNG   = 23.6236;

    private final String sensorId;
    private final String shipmentId;
    private final Random random;
    private final Consumer<TelemetryReading> onReading;

    private ScheduledExecutorService scheduler;
    private volatile boolean running = false;
    private int tickCount = 0;

    // Starea curentă simulată
    private double currentTemperature;
    private double progress = 0.0;  // 0.0 -> 1.0 (progres pe rută)

    public IoTSensorSimulator(String shipmentId, Consumer<TelemetryReading> onReading) {
        this.sensorId    = "SENSOR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.shipmentId  = shipmentId;
        this.random      = new Random();
        this.onReading   = onReading;
        this.currentTemperature = BASE_TEMPERATURE;
    }

    /**
     * Pornește simularea cu Virtual Threads (Java 21 Project Loom).
     * Fiecare task de emisie rulează pe un virtual thread — lightweight.
     */
    public void start() {
        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("sensor-sim-", 0).factory()
        );
        scheduler.scheduleAtFixedRate(this::emitReading, 0, EMIT_INTERVAL_MS, TimeUnit.MILLISECONDS);
        log.info(String.format("[SENSOR %s] Simulator pornit pentru transport %s", sensorId, shipmentId));
    }

    public void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }
        log.info(String.format("[SENSOR %s] Simulator oprit. Total tick-uri: %d", sensorId, tickCount));
    }

    /*
     Generează și emite o citire de telemetrie.
     Apelat automat la fiecare 500ms.
     */
    private void emitReading() {
        if (!running) return;
        tickCount++;

        // Simulează variație graduală de temperatură (drift natural)
        currentTemperature += (random.nextGaussian() * 0.1);

        // La fiecare 60 de tick-uri (~30s), simulează o variație mai mare
        if (tickCount % 60 == 0) {
            currentTemperature += (random.nextBoolean() ? 0.3 : -0.3);
        }

        // Scenariul de urgență: la tick 200, simulează un impact sever
        double simulatedAcceleration = BASE_ACCELERATION + random.nextDouble() * 0.5;
        if (tickCount == 200) {
            simulatedAcceleration = 5.2;  // Impact! > 4G threshold
            log.warning(String.format("[SENSOR %s] SIMULARE IMPACT: %.1fG la tick %d",
                    sensorId, simulatedAcceleration, tickCount));
        }

        // Scenariul de urgență: la tick 300, temperatura iese din interval
        if (tickCount == 300) {
            currentTemperature = 9.5;  // Depășire temperatura maximă
            log.warning(String.format("[SENSOR %s] SIMULARE TEMPERATURĂ CRITICĂ: %.1f°C la tick %d",
                    sensorId, currentTemperature, tickCount));
        }

        // Avansează pe rută (sosire simulată la tick 500)
        progress = Math.min(1.0, tickCount / 500.0);
        double lat = START_LAT + (END_LAT - START_LAT) * progress;
        double lng = START_LNG + (END_LNG - START_LNG) * progress;

        TelemetryReading reading = new TelemetryReading(
                sensorId,
                shipmentId,
                currentTemperature,
                simulatedAcceleration,
                lat + random.nextGaussian() * 0.0001,  // noise GPS
                lng + random.nextGaussian() * 0.0001,
                Instant.now()
        );

        onReading.accept(reading);
    }

    public String getSensorId() { return sensorId; }
    public String getShipmentId() { return shipmentId; }
    public int getTickCount() { return tickCount; }
    public double getCurrentTemperature() { return currentTemperature; }
}
