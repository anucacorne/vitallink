// VitalLink — shared data layer
// Bazat pe seed.sql + schema.sql din anucacorne/vitallink

const SEED_TRANSPORTS = [
  {
    id: 'b1b2-0001',
    code: 'VL-2026-0418',
    type: 'ORGAN',
    typeLabel: 'Organ',
    subtype: 'Rinichi — donator decedat',
    status: 'IN_TRANSIT',
    urgency: 'ULTRA_CRITICAL',
    origin: { name: 'Spitalul Sf. Spiridon', city: 'Iași', short: 'Iași', lat: 47.1585, lng: 27.6014 },
    destination: { name: 'Spitalul Fundeni', city: 'București', short: 'București', lat: 44.4498, lng: 26.1191 },
    tempMin: 2.0,
    tempMax: 6.0,
    tempCurrent: 4.2,
    humidity: 62,
    speed: 118,
    progressPct: 47,
    distanceKm: 412,
    distanceRemaining: 218,
    durationMin: 285,
    etaMin: 142,
    startedAgoMin: 128,
    viabilityHoursLeft: 5.4,
    viabilityHoursTotal: 8,
    vehiclePlate: 'IS-01-VTL',
    vehicleType: 'Ambulanță frigorifică',
    driver: 'Andrei Munteanu',
    driverPhone: '+40 745 ••• 218',
    containerSerial: 'CTR-887-A',
    note: 'Transport organ pornit conform programării.',
    alerts: 0,
  },
  {
    id: 'b1b2-0002',
    code: 'VL-2026-0419',
    type: 'BLOOD_PRODUCT',
    typeLabel: 'Produs sanguin',
    subtype: 'Concentrat eritrocitar — 8 unități',
    status: 'IN_TRANSIT',
    urgency: 'CRITICAL',
    origin: { name: 'Centrul de Transfuzie', city: 'Iași', short: 'Iași', lat: 47.1553, lng: 27.5877 },
    destination: { name: 'Spitalul Județean', city: 'Suceava', short: 'Suceava', lat: 47.6374, lng: 26.2530 },
    tempMin: 2.0,
    tempMax: 10.0,
    tempCurrent: 11.4,
    humidity: 55,
    speed: 87,
    progressPct: 71,
    distanceKm: 152,
    distanceRemaining: 44,
    durationMin: 105,
    etaMin: 32,
    startedAgoMin: 73,
    viabilityHoursLeft: 22.0,
    viabilityHoursTotal: 24,
    vehiclePlate: 'IS-02-VTL',
    vehicleType: 'Autoutilitară medicală',
    driver: 'Cristina Ilie',
    driverPhone: '+40 722 ••• 091',
    containerSerial: 'CTR-441-B',
    note: 'Depășire prag temperatură — verificare în curs.',
    alerts: 2,
  },
  {
    id: 'b1b2-0003',
    code: 'VL-2026-0420',
    type: 'THERMOSENSITIVE_MEDICATION',
    typeLabel: 'Medicament termosensibil',
    subtype: 'Vaccin mRNA — 2.400 doze',
    status: 'SCHEDULED',
    urgency: 'STANDARD',
    origin: { name: 'Depozit Farmaceutic', city: 'Iași', short: 'Iași', lat: 47.1607, lng: 27.5891 },
    destination: { name: 'Spitalul Municipal', city: 'Bacău', short: 'Bacău', lat: 46.5670, lng: 26.9146 },
    tempMin: -20.0,
    tempMax: -18.0,
    tempCurrent: -19.1,
    humidity: 28,
    speed: 0,
    progressPct: 0,
    distanceKm: 154,
    distanceRemaining: 154,
    durationMin: 130,
    etaMin: 130,
    startedAgoMin: 0,
    viabilityHoursLeft: 72,
    viabilityHoursTotal: 72,
    vehiclePlate: 'IS-03-VTL',
    vehicleType: 'Ambulanță standard',
    driver: 'Vlad Petrescu',
    driverPhone: '+40 731 ••• 504',
    containerSerial: 'CTR-203-C',
    note: 'Plecare programată: în 1h 47m.',
    alerts: 0,
  },
  {
    id: 'b1b2-0004',
    code: 'VL-2026-0417',
    type: 'BIOLOGICAL_SAMPLE',
    typeLabel: 'Probă biologică',
    subtype: 'Biopsii oncologice — 14 probe',
    status: 'DELIVERED',
    urgency: 'HIGH',
    origin: { name: 'Spitalul Județean', city: 'Botoșani', short: 'Botoșani', lat: 47.7464, lng: 26.6694 },
    destination: { name: 'Institutul Oncologic', city: 'Iași', short: 'Iași', lat: 47.1614, lng: 27.5848 },
    tempMin: 2.0,
    tempMax: 8.0,
    tempCurrent: 5.1,
    humidity: 60,
    speed: 0,
    progressPct: 100,
    distanceKm: 124,
    distanceRemaining: 0,
    durationMin: 95,
    etaMin: 0,
    startedAgoMin: 215,
    viabilityHoursLeft: 28,
    viabilityHoursTotal: 48,
    vehiclePlate: 'BT-04-VTL',
    vehicleType: 'Autoutilitară medicală',
    driver: 'Mihai Stanciu',
    driverPhone: '+40 758 ••• 776',
    containerSerial: 'CTR-119-D',
    note: 'Livrat 09:42 — recepționat de Dr. Ene.',
    alerts: 0,
  },
];

const SEED_ALERTS = [
  {
    id: 'al-001',
    transportId: 'b1b2-0002',
    transportCode: 'VL-2026-0419',
    type: 'TEMPERATURE_EXCEEDED',
    typeLabel: 'Temperatură peste prag',
    severity: 'CRITICAL',
    message: 'Temperatura în container a depășit pragul de 10°C — valoare măsurată 11.4°C',
    triggerValue: 11.4,
    threshold: 10.0,
    unit: '°C',
    minutesAgo: 3,
    acknowledged: false,
    location: 'DN29, km 38 — între Botoșani și Suceava',
  },
  {
    id: 'al-002',
    transportId: 'b1b2-0002',
    transportCode: 'VL-2026-0419',
    type: 'TEMPERATURE_EXCEEDED',
    typeLabel: 'Temperatură peste prag',
    severity: 'HIGH',
    message: 'Trend ascendent constant pe ultimele 4 minute — verificați izolația containerului',
    triggerValue: 10.8,
    threshold: 10.0,
    unit: '°C',
    minutesAgo: 7,
    acknowledged: false,
    location: 'DN29, km 35',
  },
  {
    id: 'al-003',
    transportId: 'b1b2-0001',
    transportCode: 'VL-2026-0418',
    type: 'ROUTE_DEVIATION',
    typeLabel: 'Deviere de rută',
    severity: 'MEDIUM',
    message: 'Deviere 2.1 km față de ruta planificată — posibil ocolire trafic A3',
    triggerValue: 2.1,
    threshold: 1.5,
    unit: 'km',
    minutesAgo: 18,
    acknowledged: false,
    location: 'A3, km 142',
  },
  {
    id: 'al-004',
    transportId: 'b1b2-0001',
    transportCode: 'VL-2026-0418',
    type: 'CONNECTION_LOST',
    typeLabel: 'Pierdere semnal',
    severity: 'LOW',
    message: 'Pierdere semnal GPS pentru 47s — recuperat automat',
    triggerValue: 47,
    threshold: 30,
    unit: 's',
    minutesAgo: 26,
    acknowledged: true,
    acknowledgedBy: 'M. Dumitrescu',
    location: 'A3, km 128',
  },
  {
    id: 'al-005',
    transportId: 'b1b2-0004',
    transportCode: 'VL-2026-0417',
    type: 'DELIVERY_DELAY',
    typeLabel: 'Întârziere livrare',
    severity: 'LOW',
    message: 'Livrare cu 8 min peste timpul estimat',
    triggerValue: 8,
    threshold: 5,
    unit: 'min',
    minutesAgo: 142,
    acknowledged: true,
    acknowledgedBy: 'D. Pașcu',
    location: 'Iași — Institutul Oncologic',
  },
];

const SEED_FACILITIES = [
  { name: 'Spitalul Sf. Spiridon', city: 'Iași' },
  { name: 'Spitalul Fundeni', city: 'București' },
  { name: 'Centrul de Transfuzie', city: 'Iași' },
  { name: 'Spitalul Județean', city: 'Suceava' },
  { name: 'Depozit Farmaceutic', city: 'Iași' },
  { name: 'Spitalul Municipal', city: 'Bacău' },
  { name: 'Institutul Oncologic', city: 'Iași' },
  { name: 'Spitalul Județean', city: 'Botoșani' },
  { name: 'Spitalul Universitar', city: 'Cluj-Napoca' },
];

// Synthetic telemetry stream — 30 points, sliding window
function makeTelemetry(transport, n = 30) {
  const out = [];
  const base = transport.tempCurrent;
  const range = transport.tempMax - transport.tempMin;
  for (let i = 0; i < n; i++) {
    const drift = Math.sin(i * 0.4) * range * 0.15 + (Math.random() - 0.5) * range * 0.08;
    const temp = base + drift - (n - i) * 0.02;
    out.push({
      t: i,
      time: `${String(Math.floor((i * 2) / 60)).padStart(2, '0')}:${String((i * 2) % 60).padStart(2, '0')}`,
      temp: +temp.toFixed(2),
      humidity: transport.humidity + Math.round((Math.random() - 0.5) * 6),
      speed: transport.speed + Math.round((Math.random() - 0.5) * 18),
    });
  }
  return out;
}

// Hook for live-updating data
function useLiveData() {
  const [tick, setTick] = React.useState(0);
  React.useEffect(() => {
    const id = setInterval(() => setTick((t) => t + 1), 2200);
    return () => clearInterval(id);
  }, []);

  const transports = React.useMemo(() => {
    return SEED_TRANSPORTS.map((t) => {
      if (t.status !== 'IN_TRANSIT') return t;
      const wobble = Math.sin(tick * 0.5 + t.id.charCodeAt(5)) * 0.4;
      return {
        ...t,
        tempCurrent: +(t.tempCurrent + wobble * (t.tempMax - t.tempMin) * 0.05).toFixed(2),
        speed: Math.max(0, t.speed + Math.round(Math.sin(tick * 0.3) * 4)),
        progressPct: Math.min(99, t.progressPct + (tick * 0.04) * (t.id === 'b1b2-0002' ? 0.4 : 0.25)),
      };
    });
  }, [tick]);

  return { tick, transports, alerts: SEED_ALERTS, facilities: SEED_FACILITIES };
}

const TYPE_CFG = {
  ORGAN: { label: 'Organ', accent: 'organ' },
  BLOOD_PRODUCT: { label: 'Sânge', accent: 'blood' },
  BIOLOGICAL_SAMPLE: { label: 'Probă', accent: 'sample' },
  THERMOSENSITIVE_MEDICATION: { label: 'Medicament', accent: 'med' },
};

const STATUS_CFG = {
  SCHEDULED: { label: 'Programat', tone: 'neutral' },
  IN_TRANSIT: { label: 'În tranzit', tone: 'live' },
  DELIVERED: { label: 'Livrat', tone: 'done' },
  CANCELLED: { label: 'Anulat', tone: 'cancel' },
  INCIDENT: { label: 'Incident', tone: 'warn' },
};

const SEV_CFG = {
  CRITICAL: { label: 'Critic', rank: 4 },
  HIGH: { label: 'Ridicat', rank: 3 },
  MEDIUM: { label: 'Mediu', rank: 2 },
  LOW: { label: 'Scăzut', rank: 1 },
};

Object.assign(window, {
  SEED_TRANSPORTS,
  SEED_ALERTS,
  SEED_FACILITIES,
  TYPE_CFG,
  STATUS_CFG,
  SEV_CFG,
  useLiveData,
  makeTelemetry,
});
