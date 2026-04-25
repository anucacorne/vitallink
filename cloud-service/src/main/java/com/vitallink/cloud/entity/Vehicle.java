package com.vitallink.cloud.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    @Column(name = "has_refrigeration", nullable = false)
    private Boolean hasRefrigeration = false;

    @Column(name = "has_freezer", nullable = false)
    private Boolean hasFreezer = false;

    @Column(name = "has_gps", nullable = false)
    private Boolean hasGps = true;

    @Column(name = "min_temp_capability")
    private BigDecimal minTempCapability;

    @Column(name = "max_temp_capability")
    private BigDecimal maxTempCapability;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String v) { this.licensePlate = v; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String v) { this.vehicleType = v; }
    public Boolean getHasRefrigeration() { return hasRefrigeration; }
    public void setHasRefrigeration(Boolean v) { this.hasRefrigeration = v; }
    public Boolean getHasFreezer() { return hasFreezer; }
    public void setHasFreezer(Boolean v) { this.hasFreezer = v; }
    public Boolean getHasGps() { return hasGps; }
    public void setHasGps(Boolean v) { this.hasGps = v; }
    public BigDecimal getMinTempCapability() { return minTempCapability; }
    public void setMinTempCapability(BigDecimal v) { this.minTempCapability = v; }
    public BigDecimal getMaxTempCapability() { return maxTempCapability; }
    public void setMaxTempCapability(BigDecimal v) { this.maxTempCapability = v; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean v) { this.active = v; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}