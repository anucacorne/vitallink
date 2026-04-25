package com.vitallink.edge.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * ============================================================
 * EdgeFilterEngine - Algoritm de Filtrare Edge Computing
 * ============================================================
 *
 * SCOP (pentru teză de licență):
 * Implementează logica de decizie "trimite sau nu în Cloud"
 * pentru a demonstra eficiența Edge Computing față de
 * procesarea directă în Cloud (Cloud-Only).
 *
 * REGULI DE FILTRARE:
 * 1. TEMPERATURE DELTA: Trimite dacă variația față de ultima
 *    valoare trimisă este >= TEMP_DELTA_THRESHOLD (0.2°C)
 * 2. HEARTBEAT: Trimite forțat la fiecare MAX_SILENCE_MS (30s)
 *    chiar dacă temperatura nu a variat suficient
 * 3. CRITICAL ALERT: Trimite IMEDIAT dacă accelerația > IMPACT_G_THRESHOLD (4G)
 *    sau temperatura e în afara intervalului sigur
 *
 * METRICI DE PERFORMANȚĂ:
 * Clasa colectează statistici pentru analiza din licență:
 * - totalMessagesReceived: toate mesajele de la senzori
 * - totalMessagesSentToCloud: mesaje care au trecut filtrul
 * - filteredOutCount: mesaje blocate (banda de rețea economisită)
 * - averageEdgeProcessingLatencyNs: latența de procesare la Edge
 */
public class EdgeFilterEngine {

    private static final Logger log = Logger.getLogger(EdgeFilterEngine.class.getName());

    // --- Constante de filtrare (pot fi externalizate în config) ---
    private static final double TEMP_DELTA_THRESHOLD  = 0.2;    // °C
    private static final long   MAX_SILENCE_MS        = 30_000; // 30 secunde
    private static final double IMPACT_G_THRESHOLD    = 4.0;    // G-force
    private static final double TEMP_MIN_SAFE         = 0.0;    // °C
    private static final double TEMP_MAX_SAFE         = 8.0;    // °C

    // --- Starea internă per senzor (Virtual Thread safe cu ConcurrentHashMap) ---
    private final Map<String, SensorState> sensorStates = new ConcurrentHashMap<>();

    // --- Metrici pentru analiza de performanță (licență) ---
    private final AtomicLong totalMessagesReceived         = new AtomicLong(0);
    private final AtomicLong totalMessagesSentToCloud      = new AtomicLong(0);
    private final AtomicLong criticalAlertsSent            = new AtomicLong(0);
    private final AtomicLong totalEdgeProcessingNs         = new AtomicLong(0);

    private final ObjectMapper objectMapper;

    public EdgeFilterEngine() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ---------------------------------------------------------------
    //  API Principal
    // ---------------------------------------------------------------

    /**
     * Evaluează un mesaj de telemetrie brut și decide acțiunea Edge.
     *
     * @param message Mesajul primit de la senzor (Nivel 1)
     * @return FilterDecision cu decizia și motivul
     */
    public FilterDecision evaluate(TelemetryReading message) {
        long startNs = System.nanoTime();
        totalMessagesReceived.incrementAndGet();

        try {
            // --- REGULA 1: Impact critic - alertă IMEDIATĂ, bypass orice filtrare ---
            if (message.accelerationG() != null
                    && message.accelerationG() > IMPACT_G_THRESHOLD) {

                criticalAlertsSent.incrementAndGet();
                totalMessagesSentToCloud.incrementAndGet();
                log.warning(String.format(
                        "[EDGE ALERT] IMPACT DETECTAT pe senzorul %s: %.2fG > %.1fG threshold",
                        message.sensorId(), message.accelerationG(), IMPACT_G_THRESHOLD
                ));
                return FilterDecision.criticalAlert(message,
                        String.format("Impact %.2fG depășește pragul de %.1fG",
                                message.accelerationG(), IMPACT_G_THRESHOLD));
            }

            // --- REGULA 2: Temperatură în afara intervalului sigur ---
            if (message.temperatureCelsius() != null) {
                if (message.temperatureCelsius() < TEMP_MIN_SAFE
                        || message.temperatureCelsius() > TEMP_MAX_SAFE) {

                    criticalAlertsSent.incrementAndGet();
                    totalMessagesSentToCloud.incrementAndGet();
                    log.warning(String.format(
                            "[EDGE ALERT] Temperatură critică pe senzorul %s: %.1f°C (interval sigur: %.1f-%.1f°C)",
                            message.sensorId(), message.temperatureCelsius(),
                            TEMP_MIN_SAFE, TEMP_MAX_SAFE
                    ));
                    return FilterDecision.criticalAlert(message,
                            String.format("Temperatură %.1f°C în afara intervalului sigur [%.1f, %.1f]",
                                    message.temperatureCelsius(), TEMP_MIN_SAFE, TEMP_MAX_SAFE));
                }
            }

            // --- Recuperează sau inițializează starea senzorului ---
            SensorState state = sensorStates.computeIfAbsent(
                    message.sensorId(), id -> new SensorState());

            long now = System.currentTimeMillis();

            // --- REGULA 3: Heartbeat forțat la 30 secunde (chiar fără variație) ---
            if (state.lastSentTimestampMs == 0
                    || (now - state.lastSentTimestampMs) >= MAX_SILENCE_MS) {

                state.update(message.temperatureCelsius(), now);
                totalMessagesSentToCloud.incrementAndGet();
                return FilterDecision.sendToCloud(message,
                        FilterReason.HEARTBEAT_INTERVAL,
                        "Heartbeat forțat după " + MAX_SILENCE_MS / 1000 + "s");
            }

            // --- REGULA 4: Delta temperatură >= 0.2°C ---
            if (message.temperatureCelsius() != null && state.lastTemperature != null) {
                double delta = Math.abs(message.temperatureCelsius() - state.lastTemperature);

                if (delta >= TEMP_DELTA_THRESHOLD) {
                    state.update(message.temperatureCelsius(), now);
                    totalMessagesSentToCloud.incrementAndGet();
                    return FilterDecision.sendToCloud(message,
                            FilterReason.TEMPERATURE_DELTA,
                            String.format("Delta temperatură %.3f°C >= prag %.1f°C",
                                    delta, TEMP_DELTA_THRESHOLD));
                }
            }

            // --- Nicio regulă nu s-a declanșat: FILTREAZĂ (nu trimite în Cloud) ---
            return FilterDecision.filtered(message,
                    "Delta temperatură sub prag și nu a expirat intervalul heartbeat");

        } finally {
            // Înregistrează latența pentru analiza comparativă Edge vs Cloud
            long elapsedNs = System.nanoTime() - startNs;
            totalEdgeProcessingNs.addAndGet(elapsedNs);
        }
    }

    /**
     * Returnează un snapshot al metricilor de performanță.
     * Utilizat pentru capitolul de analiză comparativă din licență.
     */
    public PerformanceMetrics getMetrics() {
        long received = totalMessagesReceived.get();
        long sent = totalMessagesSentToCloud.get();
        long filtered = received - sent;
        double reductionRate = received > 0 ? (double) filtered / received * 100.0 : 0.0;
        double avgLatencyNs = received > 0
                ? (double) totalEdgeProcessingNs.get() / received : 0.0;

        return new PerformanceMetrics(
                received,
                sent,
                filtered,
                criticalAlertsSent.get(),
                reductionRate,
                avgLatencyNs,
                avgLatencyNs / 1_000_000.0  // conversie în ms
        );
    }

    public void resetMetrics() {
        totalMessagesReceived.set(0);
        totalMessagesSentToCloud.set(0);
        criticalAlertsSent.set(0);
        totalEdgeProcessingNs.set(0);
    }

    // ---------------------------------------------------------------
    //  Clase interne / Records
    // ---------------------------------------------------------------

    /**
     * Starea internă menținută pentru fiecare senzor activ.
     * Nu thread-safe în izolare — acces protejat prin ConcurrentHashMap.
     */
    private static class SensorState {
        Double lastTemperature   = null;
        long   lastSentTimestampMs = 0;

        void update(Double newTemp, long nowMs) {
            if (newTemp != null) this.lastTemperature = newTemp;
            this.lastSentTimestampMs = nowMs;
        }
    }

    /**
     * Decizia returnată de EdgeFilterEngine după evaluarea unui mesaj.
     */
    public record FilterDecision(
            TelemetryReading reading,
            EdgeAction action,
            FilterReason reason,
            String explanation,
            long evaluatedAtMs
    ) {
        static FilterDecision sendToCloud(TelemetryReading r, FilterReason reason, String explanation) {
            return new FilterDecision(r, EdgeAction.SEND_TO_CLOUD, reason, explanation,
                    System.currentTimeMillis());
        }

        static FilterDecision criticalAlert(TelemetryReading r, String explanation) {
            return new FilterDecision(r, EdgeAction.CRITICAL_ALERT, FilterReason.CRITICAL_CONDITION,
                    explanation, System.currentTimeMillis());
        }

        static FilterDecision filtered(TelemetryReading r, String explanation) {
            return new FilterDecision(r, EdgeAction.DISCARD, FilterReason.BELOW_THRESHOLD,
                    explanation, System.currentTimeMillis());
        }

        public boolean shouldForwardToCloud() {
            return action == EdgeAction.SEND_TO_CLOUD || action == EdgeAction.CRITICAL_ALERT;
        }

        public boolean isCritical() {
            return action == EdgeAction.CRITICAL_ALERT;
        }
    }

    public enum EdgeAction {
        SEND_TO_CLOUD,    // Trimite normal pe topic-ul `telemetry-data`
        CRITICAL_ALERT,   // Trimite IMEDIAT pe topic-ul `critical-alerts`
        DISCARD           // Blochează mesajul (nu consumă bandă)
    }

    public enum FilterReason {
        TEMPERATURE_DELTA,    // Variație >= 0.2°C
        HEARTBEAT_INTERVAL,   // 30s fără transmitere
        CRITICAL_CONDITION,   // Impact sau temperatură în afara intervalului
        BELOW_THRESHOLD       // Sub prag, filtrare normală
    }

    /**
     * Snapshot de performanță pentru analiza comparativă din licență.
     * Edge vs Cloud: câte mesaje au fost reținute la Edge.
     */
    public record PerformanceMetrics(
            long totalReceived,
            long sentToCloud,
            long filteredAtEdge,
            long criticalAlerts,
            double trafficReductionPercent,
            double avgEdgeLatencyNs,
            double avgEdgeLatencyMs
    ) {
        @Override
        public String toString() {
            return String.format(
                    """
                    === VitalLink Edge Performance Metrics ===
                    Mesaje primite de la senzori : %d
                    Trimise în Cloud             : %d
                    Filtrate la Edge             : %d
                    Alerte critice               : %d
                    Reducere trafic              : %.1f%%
                    Latență medie Edge           : %.3f ms (%.0f ns)
                    ==========================================
                    """,
                    totalReceived, sentToCloud, filteredAtEdge, criticalAlerts,
                    trafficReductionPercent, avgEdgeLatencyMs, avgEdgeLatencyNs
            );
        }
    }

    // ---------------------------------------------------------------
    //  Record pentru datele brute de la senzor (Nivel 1 input)
    // ---------------------------------------------------------------

    /**
     * Date brute emise de un nod IoT (Nivel 1 - Senzori).
     * Generat la fiecare 500ms de simulatorul de senzori.
     */
    public record TelemetryReading(
            String sensorId,
            String shipmentId,
            Double temperatureCelsius,
            Double accelerationG,
            Double latitude,
            Double longitude,
            Instant timestamp
    ) {}
}