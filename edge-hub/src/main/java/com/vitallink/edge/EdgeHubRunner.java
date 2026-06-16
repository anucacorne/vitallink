package com.vitallink.edge;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;


// Pornește simularea automată la startup-ul aplicației.
 //Înregistrează 3 transporturi demo corespunzătoare datelor seed din baza de date.
 
@Component
public class EdgeHubRunner implements ApplicationRunner {
    private static final Logger log = Logger.getLogger(EdgeHubRunner.class.getName());
    private final EdgeHubOrchestrator orchestrator;
    public EdgeHubRunner(EdgeHubOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("║       VitalLink Edge Hub v1.0            ║");
        log.info("║  Monitoring medical transport in real    ║");
        log.info("║  time with Edge Computing + Kafka        ║");

        // Pornește simularea pentru cele 3 transporturi din seed.sql
        orchestrator.registerShipment("b1b2c3d4-0001-0001-0001-000000000001"); // Organ Iași→București
        orchestrator.registerShipment("b1b2c3d4-0002-0002-0002-000000000002"); // Sânge Iași→Suceava

        log.info("[EDGE HUB] 2 transporturi active. Simulare pornită.");
    }
}

