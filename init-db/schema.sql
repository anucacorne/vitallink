-- ============================================================
--  VitalLink — Schema bază de date
--  PostgreSQL 16
-- ============================================================

-- ─── Extensii ────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─── Enum-uri ─────────────────────────────────────────────────

-- Tipul resursei medicale transportate
CREATE TYPE transport_type AS ENUM (
    'ORGAN',
    'BLOOD_PRODUCT',
    'BIOLOGICAL_SAMPLE',
    'THERMOSENSITIVE_MEDICATION'
);

-- Starea unui transport
CREATE TYPE transport_status AS ENUM (
    'SCHEDULED',
    'IN_TRANSIT',
    'DELIVERED',
    'CANCELLED',
    'INCIDENT'
);

-- Severitatea unei alerte
CREATE TYPE alert_severity AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'CRITICAL'
);

-- Tipul alertei
CREATE TYPE alert_type AS ENUM (
    'TEMPERATURE_EXCEEDED',
    'TEMPERATURE_BELOW',
    'ROUTE_DEVIATION',
    'DELIVERY_DELAY',
    'SENSOR_FAILURE',
    'CONNECTION_LOST'
);

-- ─── Tabelul: vehicles ────────────────────────────────────────
-- Vehiculele de transport medical înregistrate în sistem
CREATE TABLE vehicles (
                          id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          license_plate   VARCHAR(20) NOT NULL UNIQUE,
                          vehicle_type    VARCHAR(50) NOT NULL,
                          active          BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                          updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ─── Tabelul: transports ──────────────────────────────────────
-- Fiecare transport medical activ sau arhivat
CREATE TABLE transports (
                            id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            vehicle_id          UUID NOT NULL REFERENCES vehicles(id),
                            transport_type      transport_type NOT NULL,
                            status              transport_status NOT NULL DEFAULT 'SCHEDULED',
                            origin              VARCHAR(255) NOT NULL,
                            destination         VARCHAR(255) NOT NULL,
    -- Coordonate GPS origine
                            origin_lat          DECIMAL(10, 8),
                            origin_lng          DECIMAL(11, 8),
    -- Coordonate GPS destinatie
                            destination_lat     DECIMAL(10, 8),
                            destination_lng     DECIMAL(11, 8),
    -- Interval de temperatura acceptat (grade Celsius)
                            temp_min_celsius    DECIMAL(5, 2) NOT NULL,
                            temp_max_celsius    DECIMAL(5, 2) NOT NULL,
    -- Timestamps
                            scheduled_at        TIMESTAMP WITH TIME ZONE NOT NULL,
                            started_at          TIMESTAMP WITH TIME ZONE,
                            delivered_at        TIMESTAMP WITH TIME ZONE,
                            created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                            updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ─── Tabelul: telemetry_events ────────────────────────────────
-- Fiecare eveniment de telemetrie primit de la senzori IoT
-- Acesta este append-only — nu se sterge, nu se modifica
CREATE TABLE telemetry_events (
                                  id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  transport_id        UUID NOT NULL REFERENCES transports(id),
                                  vehicle_id          UUID NOT NULL REFERENCES vehicles(id),
    -- Date GPS
                                  latitude            DECIMAL(10, 8) NOT NULL,
                                  longitude           DECIMAL(11, 8) NOT NULL,
                                  speed_kmh           DECIMAL(5, 2),
    -- Date senzori
                                  temperature_celsius DECIMAL(5, 2),
                                  humidity_percent    DECIMAL(5, 2),
    -- Metadata
                                  device_id           VARCHAR(100),
                                  received_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                  event_timestamp     TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Index pentru query-uri frecvente pe telemetrie
CREATE INDEX idx_telemetry_transport_id ON telemetry_events(transport_id);
CREATE INDEX idx_telemetry_received_at  ON telemetry_events(received_at DESC);
CREATE INDEX idx_telemetry_vehicle_id   ON telemetry_events(vehicle_id);

-- ─── Tabelul: alerts ──────────────────────────────────────────
-- Alertele generate de Edge Hub sau Cloud Service
CREATE TABLE alerts (
                        id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        transport_id        UUID NOT NULL REFERENCES transports(id),
                        vehicle_id          UUID NOT NULL REFERENCES vehicles(id),
                        telemetry_event_id  UUID REFERENCES telemetry_events(id),
                        alert_type          alert_type NOT NULL,
                        severity            alert_severity NOT NULL,
                        message             TEXT NOT NULL,
    -- Valoarea care a declansat alerta
                        trigger_value       DECIMAL(10, 4),
    -- Pragul depasit
                        threshold_value     DECIMAL(10, 4),
    -- Coordonate GPS la momentul alertei
                        latitude            DECIMAL(10, 8),
                        longitude           DECIMAL(11, 8),
    -- Stare
                        acknowledged        BOOLEAN NOT NULL DEFAULT FALSE,
                        acknowledged_at     TIMESTAMP WITH TIME ZONE,
                        acknowledged_by     VARCHAR(100),
                        created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_alerts_transport_id    ON alerts(transport_id);
CREATE INDEX idx_alerts_severity        ON alerts(severity);
CREATE INDEX idx_alerts_acknowledged    ON alerts(acknowledged) WHERE acknowledged = FALSE;
CREATE INDEX idx_alerts_created_at      ON alerts(created_at DESC);

-- ─── Tabelul: transport_audit_log ────────────────────────────
-- Jurnalul imutabil de audit al fiecarui transport
-- Append-only, niciodata nu se modifica
CREATE TABLE transport_audit_log (
                                     id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     transport_id    UUID NOT NULL REFERENCES transports(id),
                                     event_type      VARCHAR(100) NOT NULL,
                                     old_status      transport_status,
                                     new_status      transport_status,
                                     details         JSONB,
                                     created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_audit_transport_id ON transport_audit_log(transport_id);
CREATE INDEX idx_audit_created_at   ON transport_audit_log(created_at DESC);