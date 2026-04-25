package com.vitallink.cloud.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "telemetry_events")
public class TelemetryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transport_id", nullable = false)
    @JsonIgnoreProperties({"resourceProfile", "facilityOrigin", "facilityDestination", "vehicle", "driver", "hibernateLazyInitializer", "handler"})
    private Transport transport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "container_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Container container;

    @Column(name = "temperature_celsius")
    private BigDecimal temperatureCelsius;

    @Column(name = "humidity_percent")
    private BigDecimal humidityPercent;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(name = "speed_kmh")
    private BigDecimal speedKmh;

    @Column(name = "altitude_m")
    private BigDecimal altitudeM;

    @Column(name = "vibration_g")
    private BigDecimal vibrationG;

    @Column(name = "agitation_rpm")
    private BigDecimal agitationRpm;

    @Column(name = "light_lux")
    private BigDecimal lightLux;

    @Column(name = "battery_percent")
    private BigDecimal batteryPercent;

    @Column(name = "signal_strength_dbm")
    private Integer signalStrengthDbm;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly = false;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "received_at")
    private Instant receivedAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Sensor getSensor() { return sensor; }
    public void setSensor(Sensor v) { this.sensor = v; }
    public Transport getTransport() { return transport; }
    public void setTransport(Transport v) { this.transport = v; }
    public Container getContainer() { return container; }
    public void setContainer(Container v) { this.container = v; }
    public BigDecimal getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(BigDecimal v) { this.temperatureCelsius = v; }
    public BigDecimal getHumidityPercent() { return humidityPercent; }
    public void setHumidityPercent(BigDecimal v) { this.humidityPercent = v; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal v) { this.latitude = v; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal v) { this.longitude = v; }
    public BigDecimal getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(BigDecimal v) { this.speedKmh = v; }
    public BigDecimal getAltitudeM() { return altitudeM; }
    public void setAltitudeM(BigDecimal v) { this.altitudeM = v; }
    public BigDecimal getVibrationG() { return vibrationG; }
    public void setVibrationG(BigDecimal v) { this.vibrationG = v; }
    public BigDecimal getAgitationRpm() { return agitationRpm; }
    public void setAgitationRpm(BigDecimal v) { this.agitationRpm = v; }
    public BigDecimal getLightLux() { return lightLux; }
    public void setLightLux(BigDecimal v) { this.lightLux = v; }
    public BigDecimal getBatteryPercent() { return batteryPercent; }
    public void setBatteryPercent(BigDecimal v) { this.batteryPercent = v; }
    public Integer getSignalStrengthDbm() { return signalStrengthDbm; }
    public void setSignalStrengthDbm(Integer v) { this.signalStrengthDbm = v; }
    public Boolean getIsAnomaly() { return isAnomaly; }
    public void setIsAnomaly(Boolean v) { this.isAnomaly = v; }
    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant v) { this.eventTimestamp = v; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant v) { this.receivedAt = v; }
}