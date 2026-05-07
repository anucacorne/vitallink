// Variation D — Transport Detail (deep-dive single-transport view)
// Combines audit timeline + telemetry chart + container/sensor + map snippet

function VariantDetail({ mode = 'light' }) {
  const c = window.useTheme(mode);
  const { transports, alerts, tick } = window.useLiveData();
  const t = transports.find((x) => x.id === 'b1b2-0002');
  const tempOK = t.tempCurrent >= t.tempMin && t.tempCurrent <= t.tempMax;
  const myAlerts = alerts.filter((a) => a.transportId === t.id);
  const telemetry = React.useMemo(() => window.makeTelemetry(t, 50), [t.id, tick]);
  const tempData = telemetry.map((p) => p.temp);
  const humData = telemetry.map((p) => p.humidity);
  const speedData = telemetry.map((p) => p.speed);

  const events = [
    { time: '13:29', label: 'Transport pornit', detail: 'Plecare din ' + t.origin.name, type: 'start' },
    { time: '13:34', label: 'Container sigilat', detail: 'Sigiliu CTR-441-B verificat de A. Popa', type: 'check' },
    { time: '13:42', label: 'Intrare DN29', detail: 'Pe ruta planificată', type: 'route' },
    { time: '14:31', label: 'Temperatură peste prag', detail: '11.2°C măsurat — alertă HIGH', type: 'alert', severity: 'HIGH' },
    { time: '14:39', label: 'Trend ascendent confirmat', detail: 'Alertă escaladată la CRITICAL', type: 'alert', severity: 'CRITICAL' },
    { time: '14:42', label: 'În curs', detail: 'ETA Suceava — ' + window.fmtDuration(t.etaMin), type: 'live' },
  ];

  return (
    <div style={{
      width: 1200, minHeight: 900,
      background: c.bg, color: c.ink,
      fontFamily: 'Inter, sans-serif',
    }}>
      {/* Top bar */}
      <header style={{
        padding: '16px 32px', borderBottom: `1px solid ${c.line}`, background: c.bgPanel,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <button style={{
            background: 'transparent', border: 'none', cursor: 'pointer', padding: 4,
            color: c.inkSoft, display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, fontFamily: 'inherit',
          }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M15 18l-6-6 6-6" /></svg>
            Înapoi
          </button>
          <span style={{ fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
            Transporturi / {t.code}
          </span>
        </div>
        <div style={{ fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
          <span style={{ display: 'inline-block', width: 6, height: 6, borderRadius: '50%', background: c.teal, marginRight: 6, animation: 'vl-pulse 1.6s ease-in-out infinite' }} />
          live · ultim eveniment acum 2s
        </div>
      </header>

      {/* Hero */}
      <div style={{ padding: '32px 32px 28px', borderBottom: `1px solid ${c.line}`, display: 'grid', gridTemplateColumns: '1fr auto', gap: 32, alignItems: 'flex-end' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
            <window.StatusPill status={t.status} c={c} />
            <span style={{ fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace', letterSpacing: '0.04em' }}>
              {t.code} · pornit acum {window.fmtDuration(t.startedAgoMin)}
            </span>
            {myAlerts.filter(a => !a.acknowledged).length > 0 && (
              <span style={{ fontSize: 10, padding: '3px 8px', borderRadius: 999, background: c.coralBg, color: c.coral, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                {myAlerts.filter(a => !a.acknowledged).length} alerte deschise
              </span>
            )}
          </div>
          <h1 style={{ fontFamily: 'Instrument Serif, serif', fontSize: 44, fontWeight: 400, color: c.ink, letterSpacing: '-0.02em', lineHeight: 1.05, marginBottom: 8 }}>
            {t.subtype}
          </h1>
          <div style={{ fontSize: 14, color: c.inkSoft, display: 'flex', alignItems: 'center', gap: 10 }}>
            <span style={{ fontWeight: 500 }}>{t.origin.name}</span>
            <span style={{ color: c.inkFaint }}>·</span>
            <span style={{ color: c.inkMute }}>{t.origin.city}</span>
            <svg width="22" height="6"><line x1="0" y1="3" x2="16" y2="3" stroke={c.inkFaint} strokeWidth="1" /><path d="M16,0 L22,3 L16,6 Z" fill={c.inkFaint} /></svg>
            <span style={{ fontWeight: 500 }}>{t.destination.name}</span>
            <span style={{ color: c.inkFaint }}>·</span>
            <span style={{ color: c.inkMute }}>{t.destination.city}</span>
          </div>
        </div>
        <div style={{ display: 'flex', gap: 28, fontFamily: 'JetBrains Mono, monospace', textAlign: 'right' }}>
          <div>
            <div style={{ fontSize: 10, color: c.inkMute, textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 4 }}>ETA</div>
            <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 32, color: c.ink, fontWeight: 400, letterSpacing: '-0.02em', lineHeight: 1 }}>
              {window.fmtDuration(t.etaMin)}
            </div>
            <div style={{ fontSize: 11, color: c.inkMute }}>{t.distanceRemaining} km rămași</div>
          </div>
          <div>
            <div style={{ fontSize: 10, color: c.inkMute, textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 4 }}>Temperatură</div>
            <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 32, color: tempOK ? c.ink : c.coral, fontWeight: 400, letterSpacing: '-0.02em', lineHeight: 1 }}>
              {t.tempCurrent.toFixed(1)}°
            </div>
            <div style={{ fontSize: 11, color: c.inkMute }}>prag {t.tempMin}°–{t.tempMax}°</div>
          </div>
          <div>
            <div style={{ fontSize: 10, color: c.inkMute, textTransform: 'uppercase', letterSpacing: '0.08em', marginBottom: 4 }}>Viabilitate</div>
            <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 32, color: c.ink, fontWeight: 400, letterSpacing: '-0.02em', lineHeight: 1 }}>
              {t.viabilityHoursLeft.toFixed(0)}h
            </div>
            <div style={{ fontSize: 11, color: c.inkMute }}>din {t.viabilityHoursTotal}h total</div>
          </div>
        </div>
      </div>

      {/* Two-column body */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: 0 }}>
        {/* Left: telemetry charts + map */}
        <div style={{ padding: 28, borderRight: `1px solid ${c.line}`, display: 'flex', flexDirection: 'column', gap: 20 }}>
          {/* Big temperature chart */}
          <div style={{ background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 14, padding: 20 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 14 }}>
              <div>
                <div style={{ fontSize: 11, color: c.inkMute, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 4 }}>
                  Temperatură · ultimele 100 min
                </div>
                <div style={{ fontSize: 13, color: c.inkSoft }}>
                  {tempData.filter(v => v > t.tempMax).length} citiri peste prag · trend ascendent
                </div>
              </div>
              <div style={{ display: 'flex', gap: 14, fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
                <span>min <span style={{ color: c.ink }}>{Math.min(...tempData).toFixed(2)}°</span></span>
                <span>medie <span style={{ color: c.ink }}>{(tempData.reduce((a,b)=>a+b,0)/tempData.length).toFixed(2)}°</span></span>
                <span>max <span style={{ color: c.coral }}>{Math.max(...tempData).toFixed(2)}°</span></span>
              </div>
            </div>
            <window.Sparkline data={tempData} c={c} color={tempOK ? c.teal : c.coral} threshold={{ min: t.tempMin, max: t.tempMax }} w={620} h={140} />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 10, color: c.inkFaint, fontFamily: 'JetBrains Mono, monospace', marginTop: 6 }}>
              <span>13:30</span><span>13:50</span><span>14:10</span><span>14:30</span><span>acum</span>
            </div>
          </div>

          {/* Two smaller charts */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div style={{ background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 14, padding: 16 }}>
              <div style={{ fontSize: 10, color: c.inkMute, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 8 }}>
                Umiditate
              </div>
              <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 28, color: c.ink, fontWeight: 400, letterSpacing: '-0.02em', lineHeight: 1, marginBottom: 8 }}>
                {t.humidity}<span style={{ fontSize: 14, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace', marginLeft: 4 }}>%</span>
              </div>
              <window.Sparkline data={humData} c={c} color={c.plum} w={300} h={50} />
            </div>
            <div style={{ background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 14, padding: 16 }}>
              <div style={{ fontSize: 10, color: c.inkMute, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 8 }}>
                Viteză
              </div>
              <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 28, color: c.ink, fontWeight: 400, letterSpacing: '-0.02em', lineHeight: 1, marginBottom: 8 }}>
                {t.speed}<span style={{ fontSize: 14, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace', marginLeft: 4 }}>km/h</span>
              </div>
              <window.Sparkline data={speedData} c={c} color={c.moss} w={300} h={50} />
            </div>
          </div>

          {/* Mini map */}
          <div style={{ background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 14, overflow: 'hidden' }}>
            <div style={{ padding: '12px 16px', borderBottom: `1px solid ${c.lineSoft}`, fontSize: 11, fontWeight: 700, color: c.ink, letterSpacing: '0.06em', textTransform: 'uppercase' }}>
              Poziție pe rută
            </div>
            <div style={{ padding: 8 }}>
              <window.RomaniaMap transports={[t]} c={c} height={200} showLabels={true} />
            </div>
          </div>
        </div>

        {/* Right: Audit timeline + container/driver info */}
        <div style={{ padding: 28, display: 'flex', flexDirection: 'column', gap: 20 }}>
          {/* Audit log */}
          <div>
            <div style={{ fontSize: 11, color: c.inkMute, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 14 }}>
              Jurnal audit · imutabil
            </div>
            <div style={{ position: 'relative', paddingLeft: 18 }}>
              <div style={{ position: 'absolute', left: 5, top: 4, bottom: 4, width: 1, background: c.line }} />
              {events.map((e, i) => {
                const dotColor = e.type === 'alert' ? (e.severity === 'CRITICAL' ? c.coral : c.amber) :
                                 e.type === 'live' ? c.teal :
                                 e.type === 'start' ? c.moss :
                                 c.inkMute;
                return (
                  <div key={i} style={{ position: 'relative', paddingBottom: 16 }}>
                    <span style={{
                      position: 'absolute', left: -18, top: 4,
                      width: 11, height: 11, borderRadius: '50%',
                      background: c.bgPanel, border: `2px solid ${dotColor}`,
                      boxShadow: e.type === 'live' ? `0 0 0 4px ${dotColor}22` : 'none',
                    }} />
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 2 }}>
                      <span style={{ fontSize: 13, fontWeight: 600, color: c.ink }}>{e.label}</span>
                      <span style={{ fontSize: 10, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>{e.time}</span>
                    </div>
                    <div style={{ fontSize: 12, color: c.inkMute, lineHeight: 1.4 }}>{e.detail}</div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Resource profile */}
          <DefList c={c} title="Profil resursă" rows={[
            ['Categorie', t.typeLabel],
            ['Subtip', t.subtype],
            ['Prag temperatură', `${t.tempMin}°C – ${t.tempMax}°C`],
            ['Viabilitate max', `${t.viabilityHoursTotal}h`],
            ['Container', t.containerSerial],
          ]} />

          {/* Vehicle / driver */}
          <DefList c={c} title="Vehicul & șofer" rows={[
            ['Înmatriculare', t.vehiclePlate],
            ['Tip', t.vehicleType],
            ['Șofer', t.driver],
            ['Telefon', t.driverPhone],
          ]} />

          {/* Actions */}
          <div style={{ display: 'flex', gap: 8 }}>
            <button style={{
              flex: 1, padding: '10px 14px', borderRadius: 10,
              background: c.teal, color: c.bgPanel, border: 'none',
              fontSize: 12, fontWeight: 600, cursor: 'pointer', fontFamily: 'inherit',
            }}>Contactează șofer</button>
            <button style={{
              flex: 1, padding: '10px 14px', borderRadius: 10,
              background: 'transparent', color: c.ink, border: `1px solid ${c.line}`,
              fontSize: 12, fontWeight: 600, cursor: 'pointer', fontFamily: 'inherit',
            }}>Export raport PDF</button>
          </div>
        </div>
      </div>
    </div>
  );
}

function DefList({ c, title, rows }) {
  return (
    <div>
      <div style={{ fontSize: 11, color: c.inkMute, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 10 }}>
        {title}
      </div>
      <div style={{ background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 12, overflow: 'hidden' }}>
        {rows.map(([k, v], i) => (
          <div key={k} style={{
            display: 'grid', gridTemplateColumns: '120px 1fr', gap: 12,
            padding: '10px 14px',
            borderTop: i ? `1px solid ${c.lineSoft}` : 'none',
            fontSize: 12,
          }}>
            <span style={{ color: c.inkMute, fontWeight: 500 }}>{k}</span>
            <span style={{ color: c.ink, fontWeight: 500 }}>{v}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

window.VariantDetail = VariantDetail;
