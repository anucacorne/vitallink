# VitalLink — Infrastructură locală

## Pornire rapidă

### Cerințe
- Docker Desktop instalat și pornit
- Porturile 5432, 9092, 2181, 8090 libere

### Pornire
```bash
docker-compose up -d
```

### Verificare stare
```bash
docker-compose ps
```

### Oprire
```bash
docker-compose down
```

### Oprire + ștergere date
```bash
docker-compose down -v
```

---

## Servicii disponibile după pornire

| Serviciu | URL / Port | Credențiale |
|---|---|---|
| PostgreSQL | localhost:5432 | user: vitallink_user / pass: vitallink_pass / db: vitallink |
| Kafka | localhost:9092 | - |
| Kafka UI | http://localhost:8090 | - |

---

## Topicuri Kafka (create automat)

| Topic | Descriere |
|---|---|
| `telemetry.raw` | Date brute de la senzori IoT |
| `telemetry.filtered` | Date filtrate de Edge Hub |
| `alerts.critical` | Alerte critice (temperatură, deviere rută) |
| `transport.status` | Schimbări de stare transport |

---

## Structură proiect

```
vitallink/
├── docker-compose.yml       # Infrastructură locală
├── init-db/
│   ├── schema.sql           # Schema PostgreSQL
│   └── seed.sql             # Date inițiale demo
├── edge-hub/                # Modul Edge (filtrare telemetrie)
└── cloud-service/           # Modul Cloud (API + persistență)
```