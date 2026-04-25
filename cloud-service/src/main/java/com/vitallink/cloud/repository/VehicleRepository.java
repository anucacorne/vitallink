package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
}