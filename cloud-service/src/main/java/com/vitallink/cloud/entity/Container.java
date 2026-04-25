package com.vitallink.cloud.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "containers")
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @Column(name = "container_code", nullable = false)
    private String containerCode;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "resource_description")
    private String resourceDescription;

    private Integer quantity;

    private String unit;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Transport getTransport() { return transport; }
    public void setTransport(Transport v) { this.transport = v; }
    public String getContainerCode() { return containerCode; }
    public void setContainerCode(String v) { this.containerCode = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getResourceDescription() { return resourceDescription; }
    public void setResourceDescription(String v) { this.resourceDescription = v; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer v) { this.quantity = v; }
    public String getUnit() { return unit; }
    public void setUnit(String v) { this.unit = v; }
    public Instant getCreatedAt() { return createdAt; }
}