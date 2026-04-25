-- ============================================================
--  VitalLink — Date initiale (seed)
--  Vehicule si transporturi demo pentru testare
-- ============================================================

-- ─── Vehicule ─────────────────────────────────────────────────
INSERT INTO vehicles (id, license_plate, vehicle_type, active) VALUES
                                                                   ('a1b2c3d4-0001-0001-0001-000000000001', 'IS-01-VTL', 'Ambulanta frigorifica', TRUE),
                                                                   ('a1b2c3d4-0002-0002-0002-000000000002', 'IS-02-VTL', 'Autoutilitara medicala', TRUE),
                                                                   ('a1b2c3d4-0003-0003-0003-000000000003', 'IS-03-VTL', 'Ambulanta standard',    TRUE);

-- ─── Transporturi demo ────────────────────────────────────────
INSERT INTO transports (
    id, vehicle_id, transport_type, status,
    origin, destination,
    origin_lat, origin_lng, destination_lat, destination_lng,
    temp_min_celsius, temp_max_celsius,
    scheduled_at, started_at
) VALUES
      (
          'b1b2c3d4-0001-0001-0001-000000000001',
          'a1b2c3d4-0001-0001-0001-000000000001',
          'ORGAN', 'IN_TRANSIT',
          'Spitalul Sf. Spiridon Iasi', 'Spitalul Fundeni Bucuresti',
          47.1585, 27.6014, 44.4498, 26.1191,
          2.0, 6.0,
          NOW(), NOW() - INTERVAL '2 hours'
      ),
      (
          'b1b2c3d4-0002-0002-0002-000000000002',
          'a1b2c3d4-0002-0002-0002-000000000002',
          'BLOOD_PRODUCT', 'IN_TRANSIT',
          'Centrul de Transfuzie Iasi', 'Spitalul Judetean Suceava',
          47.1553, 27.5877, 47.6374, 26.2530,
          2.0, 10.0,
          NOW(), NOW() - INTERVAL '1 hour'
      ),
      (
          'b1b2c3d4-0003-0003-0003-000000000003',
          'a1b2c3d4-0003-0003-0003-000000000003',
          'THERMOSENSITIVE_MEDICATION', 'SCHEDULED',
          'Depozit Farmaceutic Iasi', 'Spitalul Municipal Bacau',
          47.1607, 27.5891, 46.5670, 26.9146,
          -20.0, -18.0,
          NOW() + INTERVAL '2 hours', NULL
      );

-- ─── Audit log initial ────────────────────────────────────────
INSERT INTO transport_audit_log (transport_id, event_type, new_status, details) VALUES
                                                                                    (
                                                                                        'b1b2c3d4-0001-0001-0001-000000000001',
                                                                                        'TRANSPORT_STARTED',
                                                                                        'IN_TRANSIT',
                                                                                        '{"note": "Transport organ pornit conform programarii"}'::jsonb
                                                                                    ),
                                                                                    (
                                                                                        'b1b2c3d4-0002-0002-0002-000000000002',
                                                                                        'TRANSPORT_STARTED',
                                                                                        'IN_TRANSIT',
                                                                                        '{"note": "Transport produse sanguine pornit"}'::jsonb
                                                                                    );