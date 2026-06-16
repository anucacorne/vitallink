package com.vitallink.edge.filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste unitare pentru EdgeFilterEngine.
 *   R1 - Impact critic (accelerație > 4G)
 *   R2 - Temperatură în afara intervalului sigur
 *   R3 - Heartbeat forțat la 30 de secunde
 *   R4 - Delta temperatură >= 0.2°C
 */
class EdgeFilterEngineTest {
    private EdgeFilterEngine engine;
    @BeforeEach
    void setUp() {
        engine = new EdgeFilterEngine();
    }


    //  Helper: construiește un TelemetryReading minim
    private static EdgeFilterEngine.TelemetryReading reading(
            String sensorId, Double temperatureCelsius, Double accelerationG) {
        return new EdgeFilterEngine.TelemetryReading(
                sensorId,
                "shipment-test-001",
                temperatureCelsius,
                accelerationG,
                47.1585,   // latitudine Iași
                27.6014,   // longitudine Iași
                Instant.now()
        );
    }

    //  REGULA 1: Impact critic — accelerație > 4G

    @Test
    @DisplayName("R1: Accelerație 5.2G (> 4G) → CRITICAL_ALERT, trimis imediat")
    void r1_acceleratie_depasestePrag_declansezaAlertaCritica() {
        var result = engine.evaluate(reading("sensor-r1-a", 4.0, 5.2));

        assertTrue(result.isCritical(), "Trebuie să fie alertă critică");
        assertTrue(result.shouldForwardToCloud(), "Trebuie trimis în cloud");
        assertEquals(EdgeFilterEngine.EdgeAction.CRITICAL_ALERT, result.action());
        assertEquals(EdgeFilterEngine.FilterReason.CRITICAL_CONDITION, result.reason());
    }

    @Test
    @DisplayName("R1: Accelerație exact 4.0G (= prag) → nu declanșează R1")
    void r1_acceleratieExactEgalPrag_nuDeclanseaza() {
        // Condiția este strict > 4.0, nu >= 4.0
        var result = engine.evaluate(reading("sensor-r1-b", 4.0, 4.0));

        assertFalse(result.isCritical(), "4.0G exact nu trebuie să fie alertă");
    }

    @Test
    @DisplayName("R1: Accelerație null → nu declanșează R1, continuă cu celelalte reguli")
    void r1_acceleratieNull_saritaRegula() {
        // Primul mesaj al senzorului → R3 Heartbeat
        var result = engine.evaluate(reading("sensor-r1-c", 4.0, null));

        assertFalse(result.isCritical());
        assertTrue(result.shouldForwardToCloud()); // R3 heartbeat îl trimite
    }


    //  REGULA 2: Temperatură în afara intervalului sigur [0°C, 8°C]

    @Test
    @DisplayName("R2: Temperatură 12°C (> 8°C) → CRITICAL_ALERT")
    void r2_temperaturaDepasisteMaxim_declansezaAlertaCritica() {
        var result = engine.evaluate(reading("sensor-r2-a", 12.0, 0.5));

        assertTrue(result.isCritical());
        assertEquals(EdgeFilterEngine.FilterReason.CRITICAL_CONDITION, result.reason());
        assertTrue(result.explanation().contains("12"));
    }

    @Test
    @DisplayName("R2: Temperatură -2°C (< 0°C) → CRITICAL_ALERT")
    void r2_temperaturaSubMinim_declansezaAlertaCritica() {
        var result = engine.evaluate(reading("sensor-r2-b", -2.0, 0.5));

        assertTrue(result.isCritical());
        assertEquals(EdgeFilterEngine.EdgeAction.CRITICAL_ALERT, result.action());
    }

    @Test
    @DisplayName("R2: Temperatură exact la limita inferioară (0°C) → nu declanșează R2")
    void r2_temperaturaExactMinim_nuDeclanseaza() {
        var result = engine.evaluate(reading("sensor-r2-c", 0.0, 0.5));
        assertFalse(result.isCritical());
    }

    @Test
    @DisplayName("R2: Temperatură exact la limita superioară (8°C) → nu declanșează R2")
    void r2_temperaturaExactMaxim_nuDeclanseaza() {
        var result = engine.evaluate(reading("sensor-r2-d", 8.0, 0.5));
        assertFalse(result.isCritical());
    }

    @Test
    @DisplayName("R2: Temperatură null → nu declanșează R2")
    void r2_temperaturaNull_saritaRegula() {
        var result = engine.evaluate(reading("sensor-r2-e", null, 0.5));
        // Primul mesaj → R3 heartbeat (nu R2)
        assertFalse(result.isCritical());
        assertTrue(result.shouldForwardToCloud());
    }


    //  REGULA 3: Heartbeat forțat — primul mesaj sau la 30 de secunde

    @Test
    @DisplayName("R3: Primul mesaj de la un senzor nou → HEARTBEAT, trimis în cloud")
    void r3_primulMesajSenzorNou_declansezaHeartbeat() {
        var result = engine.evaluate(reading("sensor-r3-a", 4.0, 0.5));

        assertFalse(result.isCritical(), "Heartbeat nu este alertă critică");
        assertTrue(result.shouldForwardToCloud(), "Heartbeat trebuie trimis în cloud");
        assertEquals(EdgeFilterEngine.FilterReason.HEARTBEAT_INTERVAL, result.reason());
        assertEquals(EdgeFilterEngine.EdgeAction.SEND_TO_CLOUD, result.action());
    }

    @Test
    @DisplayName("R3: Al doilea mesaj imediat (< 30s, delta < 0.2°C) → DISCARD")
    void r3_aldoileaMesajImediat_niciUnFiltruNuDeclanseaza_filtreza() {
        // Primul mesaj stabilește starea
        engine.evaluate(reading("sensor-r3-b", 4.0, 0.5));
        // Al doilea mesaj imediat, fără variație semnificativă
        var result = engine.evaluate(reading("sensor-r3-b", 4.05, 0.5));

        assertFalse(result.shouldForwardToCloud(), "Sub prag și fără heartbeat → filtrat");
        assertEquals(EdgeFilterEngine.EdgeAction.DISCARD, result.action());
    }


    //  REGULA 4: Delta temperatură >= 0.2°C

    @Test
    @DisplayName("R4: Delta 0.3°C (>= 0.2°C) → SEND_TO_CLOUD, TEMPERATURE_DELTA")
    void r4_deltaTemperatura_depasestePrag_trimiteInCloud() {
        // Primul mesaj: R3 Heartbeat stabilește lastTemperature = 4.0
        engine.evaluate(reading("sensor-r4-a", 4.0, 0.5));
        // Al doilea mesaj: delta = |4.3 - 4.0| = 0.3 >= 0.2
        var result = engine.evaluate(reading("sensor-r4-a", 4.3, 0.5));

        assertFalse(result.isCritical());
        assertTrue(result.shouldForwardToCloud());
        assertEquals(EdgeFilterEngine.FilterReason.TEMPERATURE_DELTA, result.reason());
        assertEquals(EdgeFilterEngine.EdgeAction.SEND_TO_CLOUD, result.action());
    }

    @Test
    @DisplayName("R4: Delta 0.1°C (< 0.2°C) → DISCARD (filtrat la edge)")
    void r4_deltaTemperatura_subPrag_filtreazaMesajul() {
        engine.evaluate(reading("sensor-r4-b", 4.0, 0.5));
        var result = engine.evaluate(reading("sensor-r4-b", 4.1, 0.5));

        assertFalse(result.shouldForwardToCloud(), "Delta sub prag → trebuie filtrat");
        assertEquals(EdgeFilterEngine.EdgeAction.DISCARD, result.action());
        assertEquals(EdgeFilterEngine.FilterReason.BELOW_THRESHOLD, result.reason());
    }

    @Test
    @DisplayName("R4: Delta negativă de 0.3°C → SEND_TO_CLOUD (valoare absolută)")
    void r4_deltaTemperatura_negativa_trimiteInCloud() {
        engine.evaluate(reading("sensor-r4-c", 5.0, 0.5));
        // 4.7 - 5.0 = -0.3, |delta| = 0.3 >= 0.2
        var result = engine.evaluate(reading("sensor-r4-c", 4.7, 0.5));

        assertTrue(result.shouldForwardToCloud());
        assertEquals(EdgeFilterEngine.FilterReason.TEMPERATURE_DELTA, result.reason());
    }

    @Test
    @DisplayName("R4: temperatură null pe al doilea mesaj → nu declanșează R4")
    void r4_temperaturaNull_nuDeclanseazaDelta() {
        engine.evaluate(reading("sensor-r4-d", 4.0, 0.5));
        var result = engine.evaluate(reading("sensor-r4-d", null, 0.5));

        // Delta nu poate fi calculat => DISCARD
        assertEquals(EdgeFilterEngine.EdgeAction.DISCARD, result.action());
    }

    //  Prioritatea regulilor: R1 înainte de R2

    @Test
    @DisplayName("Prioritate: Accelerație 5G ȘI temperatură 12°C → R1 câștigă (CRITICAL_ALERT)")
    void prioritate_r1InainteDeR2_ambeleCondiiDeclanseaza() {
        // Ambele R1 și R2 ar putea declanșa, dar R1 are prioritate
        var result = engine.evaluate(reading("sensor-prio", 12.0, 5.0));

        assertTrue(result.isCritical());
        // Mesajul din explanation vine de la R1 (impact), nu de la R2 (temperatură)
        assertTrue(result.explanation().contains("5") || result.explanation().contains("4"),
                "Explanation trebuie să conțină valoarea accelerației");
    }

    //  Metrici de performanță

    @Test
    @DisplayName("Metrici: 3 mesaje din care 1 trimis și 2 filtrate → reducere trafic 66%")
    void metrici_rataReducereTraficCalculataCorect() {
        engine.evaluate(reading("sensor-m1", 4.0, 0.5)); // R3 → trimis
        engine.evaluate(reading("sensor-m1", 4.05, 0.5)); // sub prag → filtrat
        engine.evaluate(reading("sensor-m1", 4.08, 0.5)); // sub prag → filtrat

        var m = engine.getMetrics();

        assertEquals(3, m.totalReceived());
        assertEquals(1, m.sentToCloud());
        assertEquals(2, m.filteredAtEdge());
        assertTrue(m.trafficReductionPercent() > 60.0,
                "Reducere trafic așteptată > 60%, obținut: " + m.trafficReductionPercent());
        assertTrue(m.avgEdgeLatencyMs() >= 0,
                "Latența Edge trebuie să fie pozitivă");
    }

    @Test
    @DisplayName("Metrici: alertele critice sunt numărate separat")
    void metrici_alerteleCriticeNumarateCorect() {
        engine.evaluate(reading("sensor-m2", 4.0, 5.5));  // R1 → alertă critică
        engine.evaluate(reading("sensor-m3", 12.0, 0.5)); // R2 → alertă critică
        engine.evaluate(reading("sensor-m4", 4.0, 0.5));  // R3 → normal

        var m = engine.getMetrics();

        assertEquals(2, m.criticalAlerts());
        assertEquals(3, m.sentToCloud());
    }

    @Test
    @DisplayName("resetMetrics: după reset, toți contoarele sunt zero")
    void resetMetrics_toateContoareleResetate() {
        engine.evaluate(reading("sensor-reset", 4.0, 5.0)); // R1 alertă critică
        engine.resetMetrics();

        var m = engine.getMetrics();

        assertEquals(0, m.totalReceived());
        assertEquals(0, m.sentToCloud());
        assertEquals(0, m.filteredAtEdge());
        assertEquals(0, m.criticalAlerts());
    }
}