package com.vitallink.cloud.simulator;

import com.vitallink.cloud.entity.*;
import com.vitallink.cloud.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Simulator IoT — generează date telemetrice realiste
 * pentru transporturile active (IN_TRANSIT).
 *
 * Rulează automat la pornirea aplicației.
 * Generează date la fiecare 10 secunde.
 *
 * Parametrii simulați per tip de senzor:
 * - TEMPERATURE: variație realistă cu drift termic
 * - GPS: deplasare pe rută cu viteză variabilă
 * - HUMIDITY: fluctuații ±2%
 * - VIBRATION: spikes aleatorii pe drum prost
 *
 * Generează automat ALERTE când valorile depășesc limitele
 * din ResourceProfile.
 */
@Component
@ConditionalOnProperty(name = "vitallink.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class IoTSimulator implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(IoTSimulator.class.getName());

    private final TransportRepository transportRepo;
    private final SensorRepository sensorRepo;
    private final ContainerRepository containerRepo;
    private final TelemetryEventRepository telemetryRepo;
    private final AlertRepository alertRepo;

    // State per transport — reține ultima poziție și temperatură
    private final Map<UUID, SimulationState> stateMap = new HashMap<>();

    public IoTSimulator(TransportRepository transportRepo,
                        SensorRepository sensorRepo,
                        ContainerRepository containerRepo,
                        TelemetryEventRepository telemetryRepo,
                        AlertRepository alertRepo) {
        this.transportRepo = transportRepo;
        this.sensorRepo = sensorRepo;
        this.containerRepo = containerRepo;
        this.telemetryRepo = telemetryRepo;
        this.alertRepo = alertRepo;
    }

    @Override
    public void run(String... args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "iot-simulator");
            t.setDaemon(true);
            return t;
        });

        // Pornește după 5 secunde, repetă la fiecare 10 secunde
        scheduler.scheduleAtFixedRate(this::simulateTick, 5, 10, TimeUnit.SECONDS);
        log.info("[IoT SIMULATOR] Pornit — generează telemetrie la fiecare 10 secunde");
    }

    private void simulateTick() {
        try {
            List<Transport> active = transportRepo.findByStatus("IN_TRANSIT");
            if (active.isEmpty()) return;

            for (Transport transport : active) {
                SimulationState simState = stateMap.computeIfAbsent(
                        transport.getId(), id -> initState(transport)
                );

                // Găsește senzorii pentru acest transport
                List<Container> containers = containerRepo.findByTransportId(transport.getId());
                if (containers.isEmpty()) continue;

                Container container = containers.get(0);
                List<Sensor> sensors = sensorRepo.findByContainerId(container.getId());
                if (sensors.isEmpty()) continue;

                // Găsește senzorul de temperatură
                Sensor tempSensor = sensors.stream()
                        .filter(s -> "TEMPERATURE".equals(s.getType()))
                        .findFirst().orElse(sensors.get(0));

                // Actualizează starea simulării
                updateSimState(simState, transport);

                // Creează evenimentul telemetric
                TelemetryEvent event = new TelemetryEvent();
                event.setSensor(tempSensor);
                event.setTransport(transport);
                event.setContainer(container);
                event.setTemperatureCelsius(bd(simState.temperature));
                event.setHumidityPercent(bd(simState.humidity));
                event.setLatitude(bd(simState.latitude));
                event.setLongitude(bd(simState.longitude));
                event.setSpeedKmh(bd(simState.speed));
                event.setVibrationG(bd(simState.vibration));
                event.setAltitudeM(bd(simState.altitude));
                event.setBatteryPercent(bd(simState.battery));
                event.setEventTimestamp(Instant.now());
                event.setReceivedAt(Instant.now());

                telemetryRepo.save(event);

                // Verifică limite și generează alerte
                checkLimits(transport, event, tempSensor, container);

                log.info(String.format(
                        "[IoT SIM] %s | Temp: %.1f°C | GPS: %.4f, %.4f | Speed: %.0f km/h | Vib: %.2fG",
                        transport.getResourceProfile().getDisplayName(),
                        simState.temperature, simState.latitude, simState.longitude,
                        simState.speed, simState.vibration
                ));
            }
        } catch (Exception e) {
            log.warning("[IoT SIMULATOR] Eroare: " + e.getMessage());
        }
    }

    private SimulationState initState(Transport transport) {
        SimulationState s = new SimulationState();
        Facility origin = transport.getFacilityOrigin();
        Facility dest = transport.getFacilityDestination();
        ResourceProfile profile = transport.getResourceProfile();

        s.latitude = origin.getLatitude() != null ? origin.getLatitude().doubleValue() : 47.16;
        s.longitude = origin.getLongitude() != null ? origin.getLongitude().doubleValue() : 27.59;
        s.destLat = dest.getLatitude() != null ? dest.getLatitude().doubleValue() : 44.43;
        s.destLng = dest.getLongitude() != null ? dest.getLongitude().doubleValue() : 26.10;

        // Temperatura pornește din mijlocul intervalului sigur
        double tempMin = profile.getTempMinCelsius().doubleValue();
        double tempMax = profile.getTempMaxCelsius().doubleValue();
        s.temperature = (tempMin + tempMax) / 2.0;
        s.humidity = 45.0;
        s.speed = 0;
        s.vibration = 0.1;
        s.altitude = 200;
        s.battery = 98;
        s.tickCount = 0;

        return s;
    }

    private void updateSimState(SimulationState s, Transport transport) {
        Random r = new Random();
        s.tickCount++;

        // ─── GPS: deplasare spre destinație ───
        double dlat = s.destLat - s.latitude;
        double dlng = s.destLng - s.longitude;
        double dist = Math.sqrt(dlat * dlat + dlng * dlng);

        if (dist > 0.01) {
            // Normalizează direcția și adaugă viteză realistă
            double step = 0.005 + r.nextDouble() * 0.008; // ~0.5-1.3 km per tick
            s.latitude += (dlat / dist) * step;
            s.longitude += (dlng / dist) * step;
            s.speed = 60 + r.nextDouble() * 70; // 60-130 km/h
        } else {
            s.speed = 0;
        }

        // ─── Temperatură: drift lent cu noise ───
        // Tendință ușoară de creștere (simulează pierdere izolație)
        double drift = 0.02 + r.nextDouble() * 0.05;
        double noise = (r.nextDouble() - 0.5) * 0.4;
        s.temperature += drift + noise;

        // ─── Umiditate: fluctuații mici ───
        s.humidity += (r.nextDouble() - 0.5) * 2.0;
        s.humidity = Math.max(20, Math.min(80, s.humidity));

        // ─── Vibrații: normal mic, spike-uri ocazionale ───
        if (r.nextDouble() < 0.15) {
            // 15% șansă de drum prost
            s.vibration = 0.4 + r.nextDouble() * 0.4; // 0.4-0.8G
        } else {
            s.vibration = 0.05 + r.nextDouble() * 0.2; // 0.05-0.25G
        }

        // ─── Altitudine: variație ușoară ───
        s.altitude += (r.nextDouble() - 0.5) * 20;
        s.altitude = Math.max(50, Math.min(800, s.altitude));

        // ─── Baterie: scade lent ───
        s.battery -= 0.05 + r.nextDouble() * 0.05;
        s.battery = Math.max(0, s.battery);
    }

    private void checkLimits(Transport transport, TelemetryEvent event, Sensor sensor, Container container) {
        ResourceProfile profile = transport.getResourceProfile();
        if (profile == null) return;

        BigDecimal temp = event.getTemperatureCelsius();
        if (temp == null) return;

        boolean overMax = temp.compareTo(profile.getTempMaxCelsius()) > 0;
        boolean underMin = temp.compareTo(profile.getTempMinCelsius()) < 0;

        if (overMax || underMin) {
            Alert alert = new Alert();
            alert.setTransport(transport);
            alert.setSensor(sensor);
            alert.setContainer(container);
            alert.setTelemetryEvent(event);
            alert.setAlertType("TEMPERATURE_BREACH");
            alert.setSeverity(isCriticalBreach(temp, profile) ? "CRITICAL" : "HIGH");
            alert.setMessage(String.format(
                    "Temperatură %.1f°C %s intervalul sigur [%.1f, %.1f]°C — %s",
                    temp, overMax ? "depășește" : "este sub",
                    profile.getTempMinCelsius(), profile.getTempMaxCelsius(),
                    profile.getDisplayName()
            ));
            alert.setTriggerValue(temp);
            alert.setThresholdValue(overMax ? profile.getTempMaxCelsius() : profile.getTempMinCelsius());
            alert.setLatitude(event.getLatitude());
            alert.setLongitude(event.getLongitude());
            alertRepo.save(alert);

            log.warning(String.format("[ALERT] %s: %.1f°C pe %s",
                    alert.getSeverity(), temp, profile.getDisplayName()));
        }

        // Verifică vibrații
        BigDecimal vib = event.getVibrationG();
        if (vib != null && profile.getMaxVibrationG() != null
                && vib.compareTo(profile.getMaxVibrationG()) > 0) {
            Alert alert = new Alert();
            alert.setTransport(transport);
            alert.setSensor(sensor);
            alert.setContainer(container);
            alert.setAlertType("VIBRATION_EXCESS");
            alert.setSeverity("HIGH");
            alert.setMessage(String.format(
                    "Vibrații excesive: %.2fG (limită: %.2fG) — %s",
                    vib, profile.getMaxVibrationG(), profile.getDisplayName()
            ));
            alert.setTriggerValue(vib);
            alert.setThresholdValue(profile.getMaxVibrationG());
            alert.setLatitude(event.getLatitude());
            alert.setLongitude(event.getLongitude());
            alertRepo.save(alert);
        }
    }

    private boolean isCriticalBreach(BigDecimal temp, ResourceProfile profile) {
        if (profile.getTempCriticalMax() != null && temp.compareTo(profile.getTempCriticalMax()) > 0)
            return true;
        if (profile.getTempCriticalMin() != null && temp.compareTo(profile.getTempCriticalMin()) < 0)
            return true;
        return false;
    }

    private BigDecimal bd(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP);
    }

    // ─── Clasă internă pentru starea simulării ───
    private static class SimulationState {
        double latitude, longitude;
        double destLat, destLng;
        double temperature, humidity;
        double speed, vibration, altitude;
        double battery;
        int tickCount;
    }
}