package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.ResourceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ResourceProfileRepository extends JpaRepository<ResourceProfile, UUID> {
}