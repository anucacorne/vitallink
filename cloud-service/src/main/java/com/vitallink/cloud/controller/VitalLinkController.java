package com.vitallink.cloud.controller;

import com.vitallink.cloud.entity.*;
import com.vitallink.cloud.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VitalLinkController {

    private final TransportRepository transportRepo;
    private final TelemetryEventRepository telemetryRepo;
    private final AlertRepository alertRepo;
    private final FacilityRepository facilityRepo;
    private final VehicleRepository vehicleRepo;
    private final DriverRepository driverRepo;
    private final ContainerRepository containerRepo;
    private final SensorRepository sensorRepo;
    private final ResourceProfileRepository resourceProfileRepo;

    public VitalLinkController(TransportRepository transportRepo,
                               TelemetryEventRepository telemetryRepo,
                               AlertRepository alertRepo,
                               FacilityRepository facilityRepo,
                               VehicleRepository vehicleRepo,
                               DriverRepository driverRepo,
                               ContainerRepository containerRepo,
                               SensorRepository sensorRepo,
                               ResourceProfileRepository resourceProfileRepo) {
        this.transportRepo = transportRepo;
        this.telemetryRepo = telemetryRepo;
        this.alertRepo = alertRepo;
        this.facilityRepo = facilityRepo;
        this.vehicleRepo = vehicleRepo;
        this.driverRepo = driverRepo;
        this.containerRepo = containerRepo;
        this.sensorRepo = sensorRepo;
        this.resourceProfileRepo = resourceProfileRepo;
    }

    // ─── Transporturi ─────────────────────────────────────────

    @GetMapping("/transports")
    public List<Transport> getAllTransports() {
        return transportRepo.findAll();
    }

    @GetMapping("/transports/active")
    public List<Transport> getActiveTransports() {
        return transportRepo.findByStatus("IN_TRANSIT");
    }

    @GetMapping("/transports/{id}")
    public ResponseEntity<Transport> getTransport(@PathVariable UUID id) {
        return transportRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transports/{id}/telemetry")
    public List<TelemetryEvent> getTelemetry(@PathVariable UUID id) {
        return telemetryRepo.findByTransportIdOrderByReceivedAtDesc(id);
    }

    @GetMapping("/transports/{id}/containers")
    public List<Container> getContainersByTransport(@PathVariable UUID id) {
        return containerRepo.findByTransportId(id);
    }

    // ─── Alerte ───────────────────────────────────────────────

    @GetMapping("/alerts")
    public List<Alert> getUnacknowledgedAlerts() {
        return alertRepo.findByAcknowledgedFalseOrderByCreatedAtDesc();
    }

    @GetMapping("/alerts/all")
    public List<Alert> getAllAlerts() {
        return alertRepo.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/alerts/critical")
    public List<Alert> getCriticalAlerts() {
        return alertRepo.findBySeverityOrderByCreatedAtDesc("CRITICAL");
    }

    @GetMapping("/alerts/{transportId}/transport")
    public List<Alert> getAlertsByTransport(@PathVariable UUID transportId) {
        return alertRepo.findByTransportIdOrderByCreatedAtDesc(transportId);
    }

    @PutMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "dispatcher") String acknowledgedBy) {
        return alertRepo.findById(id).map(alert -> {
            alert.setAcknowledged(true);
            alert.setAcknowledgedBy(acknowledgedBy);
            alert.setAcknowledgedAt(Instant.now());
            alertRepo.save(alert);
            return ResponseEntity.ok(alert);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── Facilities ───────────────────────────────────────────

    @GetMapping("/facilities")
    public List<Facility> getAllFacilities() {
        return facilityRepo.findAll();
    }

    @GetMapping("/facilities/{id}")
    public ResponseEntity<Facility> getFacility(@PathVariable UUID id) {
        return facilityRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Vehicles ─────────────────────────────────────────────

    @GetMapping("/vehicles")
    public List<Vehicle> getAllVehicles() {
        return vehicleRepo.findAll();
    }

    // ─── Drivers ──────────────────────────────────────────────

    @GetMapping("/drivers")
    public List<Driver> getAllDrivers() {
        return driverRepo.findAll();
    }

    // ─── Resource Profiles ────────────────────────────────────

    @GetMapping("/resource-profiles")
    public List<ResourceProfile> getAllResourceProfiles() {
        return resourceProfileRepo.findAll();
    }

    // ─── Dashboard ────────────────────────────────────────────

    @GetMapping("/dashboard/stats")
    public Map<String, Object> getDashboardStats() {
        long active = transportRepo.findByStatus("IN_TRANSIT").size();
        long unack = alertRepo.countByAcknowledgedFalse();
        long total = transportRepo.count();
        long facilities = facilityRepo.count();
        long vehicles = vehicleRepo.count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("activeTransports", active);
        stats.put("unacknowledgedAlerts", unack);
        stats.put("totalTransports", total);
        stats.put("totalFacilities", facilities);
        stats.put("totalVehicles", vehicles);
        return stats;
    }

    @GetMapping("/test")
    public String test() {
        return "OK";
    }
}