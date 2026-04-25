package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FacilityRepository extends JpaRepository<Facility, UUID> {
}
