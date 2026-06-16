package com.vitallink.cloud.kafka;

import com.vitallink.cloud.entity.Alert;
import com.vitallink.cloud.entity.ResourceProfile;
import com.vitallink.cloud.entity.TelemetryEvent;
import com.vitallink.cloud.entity.Transport;
import com.vitallink.cloud.repository.AlertRepository;
import com.vitallink.cloud.repository.SensorRepository;
import com.vitallink.cloud.repository.TelemetryEventRepository;
import com.vitallink.cloud.repository.TransportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Teste pentru alertarea la temperatură din TelemetryConsumer
 *
 * Acoperă metoda checkTemperatureAlert() care generează automat alerte
 * de tip TEMP_EXCEEDED și TEMP_BELOW când temperatura depășește intervalul
 * admis definit în ResourceProfile

 */
@ExtendWith(MockitoExtension.class)
class TemperatureAlertTest {

    @Mock
    private AlertRepository alertRepo;
    @Mock
    private TelemetryEventRepository telemetryRepo;
    @Mock
    private TransportRepository transportRepo;
    @Mock
    private SensorRepository sensorRepo;

    private TelemetryConsumer consumer;

    // Obiecte reale (nu mock-uri) — sunt POJO-uri JPA simple
    private Transport transport;
    private TelemetryEvent event;

    @BeforeEach
    void setUp() {
        consumer = new TelemetryConsumer(telemetryRepo, alertRepo, transportRepo, sensorRepo);

        // ResourceProfile: interval sigur [2°C, 8°C] — tipic pentru organe
        ResourceProfile profile = new ResourceProfile();
        profile.setTempMinCelsius(new BigDecimal("2.0"));
        profile.setTempMaxCelsius(new BigDecimal("8.0"));

        transport = new Transport();
        transport.setResourceProfile(profile);

        event = new TelemetryEvent();
        event.setTransport(transport);
    }

    //  Temperaturi în intervalul sigur => fără alertă

    @Test
    @DisplayName("Temperatură normală (4.0°C) în interval [2°C, 8°C] → nicio alertă")
    void temperaturaInInterval_nuGenereazaAlerta() {
        event.setTemperatureCelsius(new BigDecimal("4.0"));
        consumer.checkTemperatureAlert(transport, event);
        verify(alertRepo, never()).save(any(Alert.class));
    }

    @Test
    @DisplayName("Temperatură exact la limita inferioară (2.0°C) → nicio alertă")
    void temperaturaExactLimitaInferioara_nuGenereazaAlerta() {
        event.setTemperatureCelsius(new BigDecimal("2.0"));
        consumer.checkTemperatureAlert(transport, event);
        verify(alertRepo, never()).save(any(Alert.class));
    }

    @Test
    @DisplayName("Temperatură exact la limita superioară (8.0°C) → nicio alertă")
    void temperaturaExactLimitaSuperioara_nuGenereazaAlerta() {
        event.setTemperatureCelsius(new BigDecimal("8.0"));
        consumer.checkTemperatureAlert(transport, event);
        verify(alertRepo, never()).save(any(Alert.class));
    }

    //  Temperatură depășește maximul => TEMP_EXCEEDED

    @Test
    @DisplayName("Temperatură 12°C (> 8°C) → alertă TEMP_EXCEEDED cu severitate HIGH")
    void temperaturaDepasisteMaxim_genereazaAlertaTempExceeded() {
        event.setTemperatureCelsius(new BigDecimal("12.0"));
        consumer.checkTemperatureAlert(transport, event);
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepo, times(1)).save(captor.capture());

        Alert saved = captor.getValue();
        assertEquals("TEMP_EXCEEDED", saved.getAlertType());
        assertEquals("HIGH", saved.getSeverity());
        assertEquals(0, new BigDecimal("12.0").compareTo(saved.getTriggerValue()),
                "TriggerValue trebuie să fie temperatura care a declanșat alerta");
        assertSame(transport, saved.getTransport(),
                "Alerta trebuie asociată transportului corect");
    }

    @Test
    @DisplayName("Temperatură 8.1°C (ușor peste maxim) → alertă TEMP_EXCEEDED")
    void temperaturaUsorPesteMaxim_genereazaAlertaTempExceeded() {
        event.setTemperatureCelsius(new BigDecimal("8.1"));

        consumer.checkTemperatureAlert(transport, event);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepo).save(captor.capture());
        assertEquals("TEMP_EXCEEDED", captor.getValue().getAlertType());
    }

    @Test
    @DisplayName("Temperatură > 8°C → thresholdValue setat la valoarea maximă din profil (8°C)")
    void temperaturaDepasisteMaxim_thresholdValueEsteMaximulDinProfil() {
        event.setTemperatureCelsius(new BigDecimal("10.0"));
        consumer.checkTemperatureAlert(transport, event);
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepo).save(captor.capture());

        assertEquals(0, new BigDecimal("8.0").compareTo(captor.getValue().getThresholdValue()),
                "ThresholdValue trebuie să fie maximul din ResourceProfile (8.0°C)");
    }

    //  Temperatură sub minim => TEMP_BELOW

    @Test
    @DisplayName("Temperatură -2°C (< 2°C) → alertă TEMP_BELOW cu severitate HIGH")
    void temperaturaSubMinim_genereazaAlertaTempBelow() {
        event.setTemperatureCelsius(new BigDecimal("-2.0"));

        consumer.checkTemperatureAlert(transport, event);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepo, times(1)).save(captor.capture());

        Alert saved = captor.getValue();
        assertEquals("TEMP_BELOW", saved.getAlertType());
        assertEquals("HIGH", saved.getSeverity());
        assertEquals(0, new BigDecimal("-2.0").compareTo(saved.getTriggerValue()));
    }

    @Test
    @DisplayName("Temperatură 1.9°C (ușor sub minim) → alertă TEMP_BELOW")
    void temperaturaUsorSubMinim_genereazaAlertaTempBelow() {
        event.setTemperatureCelsius(new BigDecimal("1.9"));

        consumer.checkTemperatureAlert(transport, event);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepo).save(captor.capture());
        assertEquals("TEMP_BELOW", captor.getValue().getAlertType());
    }

    @Test
    @DisplayName("Temperatură < 2°C → thresholdValue setat la valoarea minimă din profil (2°C)")
    void temperaturaSubMinim_thresholdValueEsteMinimulDinProfil() {
        event.setTemperatureCelsius(new BigDecimal("0.0"));

        consumer.checkTemperatureAlert(transport, event);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepo).save(captor.capture());

        assertEquals(0, new BigDecimal("2.0").compareTo(captor.getValue().getThresholdValue()),
                "ThresholdValue trebuie să fie minimul din ResourceProfile (2.0°C)");
    }

    //  Cazuri speciale

    @Test
    @DisplayName("Temperatură null => nicio alertă, fără NullPointerException")
    void temperaturaNull_nuGenereazaAlertaFaraNPE() {
        event.setTemperatureCelsius(null);

        assertDoesNotThrow(() -> consumer.checkTemperatureAlert(transport, event));
        verify(alertRepo, never()).save(any());
    }

    @Test
    @DisplayName("ResourceProfile null => nicio alertă, fără NullPointerException")
    void resourceProfileNull_nuGenereazaAlertaFaraNPE() {
        Transport transportFaraProfile = new Transport();
        // Nu setăm ResourceProfile — rămâne null
        event.setTemperatureCelsius(new BigDecimal("12.0"));

        assertDoesNotThrow(() -> consumer.checkTemperatureAlert(transportFaraProfile, event));
        verify(alertRepo, never()).save(any());
    }

    @Test
    @DisplayName("Temperatura generează o singură alertă per eveniment (nu duplicate)")
    void temperaturaDepasisteMaxim_oSinguaAlertaSalvata() {
        event.setTemperatureCelsius(new BigDecimal("15.0"));

        consumer.checkTemperatureAlert(transport, event);

        // verify că alertRepo.save() a fost apelat exact o dată
        verify(alertRepo, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Mesajul alertei conține temperatura și intervalul sigur")
    void mesajulAlertei_contineValoriRelevante() {
        event.setTemperatureCelsius(new BigDecimal("10.5"));

        consumer.checkTemperatureAlert(transport, event);

        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepo).save(captor.capture());

        String message = captor.getValue().getMessage();
        assertNotNull(message);
        assertTrue(message.contains("10.5") || message.contains("10"),
                "Mesajul alertei trebuie să conțină temperatura care a declanșat-o");
    }
}