package com.vitallink.cloud.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transports")
public class Transport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_profile_id", nullable = false)
    private ResourceProfile resourceProfile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_origin_id", nullable = false)
    private Facility facilityOrigin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_destination_id", nullable = false)
    private Facility facilityDestination;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Driver driver;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "priority_override")
    private String priorityOverride;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "viability_deadline")
    private Instant viabilityDeadline;

    @Column(name = "estimated_duration_min")
    private Integer estimatedDurationMin;

    @Column(name = "estimated_distance_km")
    private BigDecimal estimatedDistanceKm;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ResourceProfile getResourceProfile() { return resourceProfile; }
    public void setResourceProfile(ResourceProfile v) { this.resourceProfile = v; }
    public Facility getFacilityOrigin() { return facilityOrigin; }
    public void setFacilityOrigin(Facility v) { this.facilityOrigin = v; }
    public Facility getFacilityDestination() { return facilityDestination; }
    public void setFacilityDestination(Facility v) { this.facilityDestination = v; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle v) { this.vehicle = v; }
    public Driver getDriver() { return driver; }
    public void setDriver(Driver v) { this.driver = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getPriorityOverride() { return priorityOverride; }
    public void setPriorityOverride(String v) { this.priorityOverride = v; }
    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant v) { this.scheduledAt = v; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant v) { this.startedAt = v; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant v) { this.deliveredAt = v; }
    public Instant getViabilityDeadline() { return viabilityDeadline; }
    public void setViabilityDeadline(Instant v) { this.viabilityDeadline = v; }
    public Integer getEstimatedDurationMin() { return estimatedDurationMin; }
    public void setEstimatedDurationMin(Integer v) { this.estimatedDurationMin = v; }
    public BigDecimal getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(BigDecimal v) { this.estimatedDistanceKm = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant v) { this.updatedAt = v; }
}