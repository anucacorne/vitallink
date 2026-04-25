package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByAcknowledgedFalseOrderByCreatedAtDesc();

    @Query(value = "SELECT * FROM alerts WHERE severity = CAST(:severity AS alert_severity) ORDER BY created_at DESC", nativeQuery = true)
    List<Alert> findBySeverityOrderByCreatedAtDesc(String severity);

    List<Alert> findByTransportIdOrderByCreatedAtDesc(UUID transportId);

    List<Alert> findAllByOrderByCreatedAtDesc();

    long countByAcknowledgedFalse();
}