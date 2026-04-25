package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {
}