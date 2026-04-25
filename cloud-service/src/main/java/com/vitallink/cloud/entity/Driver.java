package com.vitallink.cloud.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "license_category")
    private String licenseCategory;

    @Column(name = "certified_medical_transport", nullable = false)
    private Boolean certifiedMedicalTransport = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String v) { this.licenseNumber = v; }
    public String getLicenseCategory() { return licenseCategory; }
    public void setLicenseCategory(String v) { this.licenseCategory = v; }
    public Boolean getCertifiedMedicalTransport() { return certifiedMedicalTransport; }
    public void setCertifiedMedicalTransport(Boolean v) { this.certifiedMedicalTransport = v; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean v) { this.active = v; }
    public Instant getCreatedAt() { return createdAt; }
}
