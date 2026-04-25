package com.vitallink.cloud.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "facilities")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    private String address;

    @Column(nullable = false)
    private String city;

    private String county;

    @Column(nullable = false)
    private String country;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "has_cold_storage", nullable = false)
    private Boolean hasColdStorage = false;

    @Column(name = "has_cryo_storage", nullable = false)
    private Boolean hasCryoStorage = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal v) { this.latitude = v; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal v) { this.longitude = v; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String v) { this.contactPhone = v; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String v) { this.contactEmail = v; }
    public Boolean getHasColdStorage() { return hasColdStorage; }
    public void setHasColdStorage(Boolean v) { this.hasColdStorage = v; }
    public Boolean getHasCryoStorage() { return hasCryoStorage; }
    public void setHasCryoStorage(Boolean v) { this.hasCryoStorage = v; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean v) { this.active = v; }
    public Instant getCreatedAt() { return createdAt; }
}