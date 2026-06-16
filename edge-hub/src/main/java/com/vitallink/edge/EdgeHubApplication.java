package com.vitallink.edge;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
Clasa principală Spring Boot pentru modulul Edge Hub.
Activează:
 - Auto-configurarea Spring Boot
 - Scheduling pentru logPerformanceMetrics()
 - Virtual Threads (configurat în application.properties)
 */
@SpringBootApplication
@EnableScheduling
public class EdgeHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdgeHubApplication.class, args);
    }
}
