package com.vitallink.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clasa principală Spring Boot pentru Cloud Service (Nivel 3).
 *
 * Responsabilități:
 * - Consumă evenimente de telemetrie din Kafka
 * - Persistează datele în PostgreSQL
 * - Expune REST API pentru dashboard și alerte
 * - WebSocket pentru actualizări în timp real
 */
@SpringBootApplication
@EnableScheduling
public class CloudServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudServiceApplication.class, args);
    }
}