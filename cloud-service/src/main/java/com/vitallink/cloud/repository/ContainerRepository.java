package com.vitallink.cloud.repository;

import com.vitallink.cloud.entity.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ContainerRepository extends JpaRepository<Container, UUID> {
    List<Container> findByTransportId(UUID transportId);
}
