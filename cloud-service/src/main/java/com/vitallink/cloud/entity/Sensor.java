package com.vitallink.cloud.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "container_id", nullable = false)
    private Container container;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    private String manufacturer;

    private String model;

    @Column(name = "calibrated_at")
    private Instant calibratedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Container getContainer() { return container; }
    public void setContainer(Container v) { this.container = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String v) { this.deviceId = v; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String v) { this.manufacturer = v; }
    public String getModel() { return model; }
    public void setModel(String v) { this.model = v; }
    public Instant getCalibratedAt() { return calibratedAt; }
    public void setCalibratedAt(Instant v) { this.calibratedAt = v; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean v) { this.isActive = v; }
    public Instant getCreatedAt() { return createdAt; }
}