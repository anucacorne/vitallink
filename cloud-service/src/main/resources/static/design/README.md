# VitalLink — Design Explorations

Mockup interactiv cu 4 direcții de design pentru platforma VitalLink.

## Cum îl rulezi

**Local:**
1. Deschide `VitalLink.html` direct în browser (dublu-click) — funcționează fără server.
2. Sau servește folderul cu orice static server: `python -m http.server 8080` din folderul `design/`, apoi accesează `http://localhost:8080/VitalLink.html`.

**Pe Render / VM:**
Dacă serverul tău Express servește fișiere statice (`app.use(express.static(...))`), pune folderul `design/` într-o locație servită și accesează:
```
https://your-domain.onrender.com/design/VitalLink.html
```

## Ce conține

- **A · Editorial Dispatch** — dashboard desktop, ton editorial, focus narativ
- **B · Mission Control** — dashboard desktop dens, KPI-uri live
- **C · Driver App (iOS)** — vedere mobilă pentru șofer
- **D · Transport Detail** — deep-dive cu jurnal audit + telemetrie

Toggle Light/Dark din panoul Tweaks (colț dreapta-jos).

## Stack

- HTML + JSX transpilate în browser via Babel standalone
- React 18 (UMD)
- Fonts: Inter, Instrument Serif, JetBrains Mono (Google Fonts)
- Zero build step, zero npm install

## Notă

Acesta e un **mockup vizual / mood board interactiv**, nu o aplicație funcțională.
Datele afișate sunt simulate (inspirate din `seed.sql`) și se actualizează la fiecare 2.2s
pentru efect live. Nu există backend, auth, DB sau API real.

Folosește-l ca referință pentru stil, layout și interacțiune când vei dezvolta UI-ul real.
