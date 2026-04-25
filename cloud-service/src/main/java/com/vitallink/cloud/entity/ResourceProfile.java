package com.vitallink.cloud.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resource_profiles")
public class ResourceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "subtype", nullable = false, unique = true)
    private String subtype;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "temp_min_celsius", nullable = false)
    private BigDecimal tempMinCelsius;

    @Column(name = "temp_max_celsius", nullable = false)
    private BigDecimal tempMaxCelsius;

    @Column(name = "temp_critical_min")
    private BigDecimal tempCriticalMin;

    @Column(name = "temp_critical_max")
    private BigDecimal tempCriticalMax;

    @Column(name = "humidity_min_percent")
    private BigDecimal humidityMinPercent;

    @Column(name = "humidity_max_percent")
    private BigDecimal humidityMaxPercent;

    @Column(name = "requires_agitation", nullable = false)
    private Boolean requiresAgitation = false;

    @Column(name = "agitation_min_rpm")
    private BigDecimal agitationMinRpm;

    @Column(name = "requires_light_protection", nullable = false)
    private Boolean requiresLightProtection = false;

    @Column(name = "max_light_lux")
    private BigDecimal maxLightLux;

    @Column(name = "max_vibration_g")
    private BigDecimal maxVibrationG;

    @Column(name = "max_viability_hours")
    private BigDecimal maxViabilityHours;

    @Column(name = "urgency", nullable = false)
    private String urgency;

    @Column(name = "preservation_solution")
    private String preservationSolution;

    @Column(name = "transport_notes")
    private String transportNotes;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public BigDecimal getTempMinCelsius() { return tempMinCelsius; }
    public void setTempMinCelsius(BigDecimal v) { this.tempMinCelsius = v; }
    public BigDecimal getTempMaxCelsius() { return tempMaxCelsius; }
    public void setTempMaxCelsius(BigDecimal v) { this.tempMaxCelsius = v; }
    public BigDecimal getTempCriticalMin() { return tempCriticalMin; }
    public void setTempCriticalMin(BigDecimal v) { this.tempCriticalMin = v; }
    public BigDecimal getTempCriticalMax() { return tempCriticalMax; }
    public void setTempCriticalMax(BigDecimal v) { this.tempCriticalMax = v; }
    public BigDecimal getHumidityMinPercent() { return humidityMinPercent; }
    public void setHumidityMinPercent(BigDecimal v) { this.humidityMinPercent = v; }
    public BigDecimal getHumidityMaxPercent() { return humidityMaxPercent; }
    public void setHumidityMaxPercent(BigDecimal v) { this.humidityMaxPercent = v; }
    public Boolean getRequiresAgitation() { return requiresAgitation; }
    public void setRequiresAgitation(Boolean v) { this.requiresAgitation = v; }
    public BigDecimal getAgitationMinRpm() { return agitationMinRpm; }
    public void setAgitationMinRpm(BigDecimal v) { this.agitationMinRpm = v; }
    public Boolean getRequiresLightProtection() { return requiresLightProtection; }
    public void setRequiresLightProtection(Boolean v) { this.requiresLightProtection = v; }
    public BigDecimal getMaxLightLux() { return maxLightLux; }
    public void setMaxLightLux(BigDecimal v) { this.maxLightLux = v; }
    public BigDecimal getMaxVibrationG() { return maxVibrationG; }
    public void setMaxVibrationG(BigDecimal v) { this.maxVibrationG = v; }
    public BigDecimal getMaxViabilityHours() { return maxViabilityHours; }
    public void setMaxViabilityHours(BigDecimal v) { this.maxViabilityHours = v; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getPreservationSolution() { return preservationSolution; }
    public void setPreservationSolution(String v) { this.preservationSolution = v; }
    public String getTransportNotes() { return transportNotes; }
    public void setTransportNotes(String v) { this.transportNotes = v; }
    public Instant getCreatedAt() { return createdAt; }
}