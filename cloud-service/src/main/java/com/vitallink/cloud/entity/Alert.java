package com.vitallink.cloud.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transport_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Transport transport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "container_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Container container;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "telemetry_event_id")
    @JsonIgnoreProperties({"transport", "sensor", "container", "hibernateLazyInitializer", "handler"})
    private TelemetryEvent telemetryEvent;

    @Column(name = "alert_type", nullable = false)
    private String alertType;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(nullable = false)
    private String message;

    @Column(name = "trigger_value")
    private BigDecimal triggerValue;

    @Column(name = "threshold_value")
    private BigDecimal thresholdValue;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(nullable = false)
    private Boolean acknowledged = false;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by")
    private String acknowledgedBy;

    @Column(name = "resolution_notes")
    private String resolutionNotes;

    @Column(nullable = false)
    private Boolean escalated = false;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Transport getTransport() { return transport; }
    public void setTransport(Transport v) { this.transport = v; }
    public Sensor getSensor() { return sensor; }
    public void setSensor(Sensor v) { this.sensor = v; }
    public Container getContainer() { return container; }
    public void setContainer(Container v) { this.container = v; }
    public TelemetryEvent getTelemetryEvent() { return telemetryEvent; }
    public void setTelemetryEvent(TelemetryEvent v) { this.telemetryEvent = v; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String v) { this.alertType = v; }
    public String getSeverity() { return severity; }
    public void setSeverity(String v) { this.severity = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public BigDecimal getTriggerValue() { return triggerValue; }
    public void setTriggerValue(BigDecimal v) { this.triggerValue = v; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal v) { this.thresholdValue = v; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal v) { this.latitude = v; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal v) { this.longitude = v; }
    public Boolean getAcknowledged() { return acknowledged; }
    public void setAcknowledged(Boolean v) { this.acknowledged = v; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant v) { this.acknowledgedAt = v; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String v) { this.acknowledgedBy = v; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String v) { this.resolutionNotes = v; }
    public Boolean getEscalated() { return escalated; }
    public void setEscalated(Boolean v) { this.escalated = v; }
    public Instant getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(Instant v) { this.escalatedAt = v; }
    public Instant getCreatedAt() { return createdAt; }
}