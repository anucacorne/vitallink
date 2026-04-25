package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {
    List<Sensor> findByContainerId(UUID containerId);
}