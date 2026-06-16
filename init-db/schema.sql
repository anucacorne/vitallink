--  VitalLink — Schema bază de date 
--  PostgreSQL 16 — conform entităților JPA

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- resource_profiles
CREATE TABLE resource_profiles (
                                   id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   category                VARCHAR(100) NOT NULL,
                                   subtype                 VARCHAR(100) NOT NULL UNIQUE,
                                   display_name            VARCHAR(255) NOT NULL,
                                   temp_min_celsius        DECIMAL(5,2) NOT NULL,
                                   temp_max_celsius        DECIMAL(5,2) NOT NULL,
                                   temp_critical_min       DECIMAL(5,2),
                                   temp_critical_max       DECIMAL(5,2),
                                   humidity_min_percent    DECIMAL(5,2),
                                   humidity_max_percent    DECIMAL(5,2),
                                   requires_agitation      BOOLEAN NOT NULL DEFAULT FALSE,
                                   agitation_min_rpm       DECIMAL(7,2),
                                   requires_light_protection BOOLEAN NOT NULL DEFAULT FALSE,
                                   max_light_lux           DECIMAL(10,2),
                                   max_vibration_g         DECIMAL(5,2),
                                   max_viability_hours     DECIMAL(7,2),
                                   urgency                 VARCHAR(50) NOT NULL,
                                   preservation_solution   VARCHAR(255),
                                   transport_notes         TEXT,
                                   created_at              TIMESTAMP DEFAULT NOW()
);

--facilities
CREATE TABLE facilities (
                            id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            name            VARCHAR(255) NOT NULL,
                            type            VARCHAR(100) NOT NULL,
                            address         VARCHAR(255),
                            city            VARCHAR(100) NOT NULL,
                            county          VARCHAR(100),
                            country         VARCHAR(100) NOT NULL,
                            latitude        DECIMAL(10,8),
                            longitude       DECIMAL(11,8),
                            contact_phone   VARCHAR(50),
                            contact_email   VARCHAR(255),
                            has_cold_storage    BOOLEAN NOT NULL DEFAULT FALSE,
                            has_cryo_storage    BOOLEAN NOT NULL DEFAULT FALSE,
                            active          BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at      TIMESTAMP DEFAULT NOW()
);

-- vehicles
CREATE TABLE vehicles (
                          id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          license_plate       VARCHAR(20) NOT NULL UNIQUE,
                          vehicle_type        VARCHAR(50) NOT NULL,
                          has_refrigeration   BOOLEAN NOT NULL DEFAULT FALSE,
                          has_freezer         BOOLEAN NOT NULL DEFAULT FALSE,
                          has_gps             BOOLEAN NOT NULL DEFAULT TRUE,
                          min_temp_capability DECIMAL(5,2),
                          max_temp_capability DECIMAL(5,2),
                          active              BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at          TIMESTAMP DEFAULT NOW(),
                          updated_at          TIMESTAMP DEFAULT NOW()
);

-- drivers
CREATE TABLE drivers (
                         id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         first_name                  VARCHAR(100) NOT NULL,
                         last_name                   VARCHAR(100) NOT NULL,
                         phone                       VARCHAR(50) NOT NULL,
                         license_number              VARCHAR(50),
                         license_category            VARCHAR(20),
                         certified_medical_transport BOOLEAN NOT NULL DEFAULT FALSE,
                         active                      BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at                  TIMESTAMP DEFAULT NOW()
);

-- transports 
CREATE TABLE transports (
                            id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            resource_profile_id     UUID NOT NULL REFERENCES resource_profiles(id),
                            facility_origin_id      UUID NOT NULL REFERENCES facilities(id),
                            facility_destination_id UUID NOT NULL REFERENCES facilities(id),
                            vehicle_id              UUID NOT NULL REFERENCES vehicles(id),
                            driver_id               UUID REFERENCES drivers(id),
                            status                  VARCHAR(50) NOT NULL,
                            priority_override       VARCHAR(50),
                            scheduled_at            TIMESTAMP NOT NULL,
                            started_at              TIMESTAMP,
                            delivered_at            TIMESTAMP,
                            viability_deadline      TIMESTAMP,
                            estimated_duration_min  INTEGER,
                            estimated_distance_km   DECIMAL(8,2),
                            notes                   TEXT,
                            created_at              TIMESTAMP DEFAULT NOW(),
                            updated_at              TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_transports_status ON transports(status);

-- containers
CREATE TABLE containers (
                            id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            transport_id         UUID NOT NULL REFERENCES transports(id),
                            container_code       VARCHAR(100) NOT NULL,
                            type                 VARCHAR(100) NOT NULL,
                            resource_description VARCHAR(255),
                            quantity             INTEGER,
                            unit                 VARCHAR(50),
                            created_at           TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_containers_transport_id ON containers(transport_id);

--sensors
CREATE TABLE sensors (
                         id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         container_id    UUID NOT NULL REFERENCES containers(id),
                         type            VARCHAR(100) NOT NULL,
                         device_id       VARCHAR(100) NOT NULL,
                         manufacturer    VARCHAR(100),
                         model           VARCHAR(100),
                         calibrated_at   TIMESTAMP,
                         is_active       BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_sensors_container_id ON sensors(container_id);
CREATE INDEX idx_sensors_device_id    ON sensors(device_id);

-- telemetry_events
CREATE TABLE telemetry_events (
                                  id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  sensor_id           UUID NOT NULL REFERENCES sensors(id),
                                  transport_id        UUID NOT NULL REFERENCES transports(id),
                                  container_id        UUID REFERENCES containers(id),
                                  temperature_celsius DECIMAL(6,2),
                                  humidity_percent    DECIMAL(5,2),
                                  latitude            DECIMAL(10,8),
                                  longitude           DECIMAL(11,8),
                                  speed_kmh           DECIMAL(6,2),
                                  altitude_m          DECIMAL(7,2),
                                  vibration_g         DECIMAL(6,3),
                                  agitation_rpm       DECIMAL(7,2),
                                  light_lux           DECIMAL(10,2),
                                  battery_percent     DECIMAL(5,2),
                                  signal_strength_dbm INTEGER,
                                  is_anomaly          BOOLEAN DEFAULT FALSE,
                                  event_timestamp     TIMESTAMP NOT NULL,
                                  received_at         TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_telemetry_transport_id ON telemetry_events(transport_id);
CREATE INDEX idx_telemetry_sensor_id    ON telemetry_events(sensor_id);
CREATE INDEX idx_telemetry_received_at  ON telemetry_events(received_at DESC);

--alerts
CREATE TABLE alerts (
                        id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        transport_id        UUID NOT NULL REFERENCES transports(id),
                        sensor_id           UUID REFERENCES sensors(id),
                        container_id        UUID REFERENCES containers(id),
                        telemetry_event_id  UUID REFERENCES telemetry_events(id),
                        alert_type          VARCHAR(100) NOT NULL,
                        severity            VARCHAR(50) NOT NULL,
                        message             TEXT NOT NULL,
                        trigger_value       DECIMAL(10,4),
                        threshold_value     DECIMAL(10,4),
                        latitude            DECIMAL(10,8),
                        longitude           DECIMAL(11,8),
                        acknowledged        BOOLEAN NOT NULL DEFAULT FALSE,
                        acknowledged_at     TIMESTAMP,
                        acknowledged_by     VARCHAR(100),
                        resolution_notes    TEXT,
                        escalated           BOOLEAN NOT NULL DEFAULT FALSE,
                        escalated_at        TIMESTAMP,
                        created_at          TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_alerts_transport_id ON alerts(transport_id);
CREATE INDEX idx_alerts_acknowledged ON alerts(acknowledged) WHERE acknowledged = FALSE;
CREATE INDEX idx_alerts_severity     ON alerts(severity);

-- transport_logs
CREATE TABLE transport_logs (
                                id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                transport_id    UUID NOT NULL REFERENCES transports(id),
                                event_type      VARCHAR(100) NOT NULL,
                                description     VARCHAR(255) NOT NULL,
                                latitude        DECIMAL(10,8),
                                longitude       DECIMAL(11,8),
                                created_by      VARCHAR(100),
                                created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_transport_logs_transport_id ON transport_logs(transport_id);
