package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, UUID> {
    List<Transport> findByStatus(String status);
}