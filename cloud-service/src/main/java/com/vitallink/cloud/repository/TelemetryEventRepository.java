package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.TelemetryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TelemetryEventRepository extends JpaRepository<TelemetryEvent, UUID> {
    List<TelemetryEvent> findByTransportIdOrderByReceivedAtDesc(UUID transportId);
}