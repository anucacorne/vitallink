// Variation B — Mission Control (dense, live, NASA-style discipline)
// Same warm palette, but information-dense, fixed grid, mono-heavy.

function VariantMission({ mode = 'light' }) {
  const c = window.useTheme(mode);
  const { transports, alerts, tick } = window.useLiveData();
  const [selectedId, setSelectedId] = React.useState('b1b2-0002'); // start with the alerting one
  const selected = transports.find((t) => t.id === selectedId) || transports[0];
  const active = transports.filter((t) => t.status === 'IN_TRANSIT');
  const unack = alerts.filter((a) => !a.acknowledged);
  const critical = unack.filter((a) => a.severity === 'CRITICAL');
  const telemetry = React.useMemo(() => window.makeTelemetry(selected), [selected.id, tick]);

  return (
    <div style={{
      width: 1440, minHeight: 1000,
      background: c.bg, color: c.ink,
      fontFamily: 'Inter, sans-serif',
      display: 'flex', flexDirection: 'column',
    }}>
      {/* Mission bar */}
      <header style={{
        display: 'grid', gridTemplateColumns: '260px 1fr auto',
        alignItems: 'center', gap: 24,
        padding: '12px 24px', background: c.bgPanel,
        borderBottom: `1px solid ${c.line}`,
        fontFamily: 'JetBrains Mono, monospace',
      }}>
        <window.Wordmark size={16} c={c} />
        <div style={{ display: 'flex', gap: 28, fontSize: 11, color: c.inkMute }}>
          <KpiInline label="UPLINK" value="OK" c={c} ok />
          <KpiInline label="KAFKA LAG" value="0.4s" c={c} ok />
          <KpiInline label="EVENTS/MIN" value="218" c={c} />
          <KpiInline label="ACTIVE" value={String(active.length).padStart(2,'0')} c={c} />
          <KpiInline label="ALERTS" value={String(unack.length).padStart(2,'0')} c={c} warn={critical.length > 0} />
          <KpiInline label="CRITICAL" value={String(critical.length).padStart(2,'0')} c={c} warn={critical.length > 0} />
        </div>
        <div style={{ display: 'flex', gap: 18, alignItems: 'center', fontSize: 11, color: c.inkSoft }}>
          <span>2026-05-07 · 14:42:18 EET</span>
          <span style={{
            display: 'inline-flex', alignItems: 'center', gap: 6,
            padding: '4px 10px', borderRadius: 4,
            background: c.tealBg, color: c.teal, fontWeight: 600,
          }}>
            <span style={{ width: 6, height: 6, borderRadius: '50%', background: c.teal, animation: 'vl-pulse 1.6s ease-in-out infinite' }} />
            LIVE
          </span>
        </div>
      </header>

      {/* Main grid: 12 columns */}
      <div style={{
        display: 'grid', gridTemplateColumns: '1fr', gridTemplateRows: 'auto auto auto',
        gap: 12, padding: 12, flex: 1,
      }}>
        {/* Row 1: 4 KPI tiles */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
          <KpiTile
            label="Transporturi în tranzit"
            value={String(active.length)}
            sub={`${active.filter(t => t.alerts > 0).length} cu alerte active`}
            trend={[2, 3, 3, 4, 4, 5, 5, 4, 4, 3]}
            c={c} accent={c.teal}
          />
          <KpiTile
            label="Alerte deschise"
            value={String(unack.length)}
            sub={`${critical.length} critice · ${unack.filter(a=>a.severity==='HIGH').length} ridicate`}
            trend={[1, 1, 0, 0, 1, 2, 2, 3, 3, unack.length]}
            c={c} accent={c.coral} warn
          />
          <KpiTile
            label="Distanță parcursă azi"
            value="1.842"
            sub="km · 4 vehicule · 7 km medie/min"
            trend={[400, 600, 850, 1100, 1300, 1500, 1700, 1842]}
            c={c} accent={c.plum}
            unit="km"
          />
          <KpiTile
            label="Livrări complete"
            value="3"
            sub="din 7 programate · OTD 100%"
            trend={[0, 0, 1, 1, 2, 2, 3, 3]}
            c={c} accent={c.moss}
          />
        </div>

        {/* Row 2: Map (8 cols) + Alert stream (4 cols) */}
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 12 }}>
          <Panel title="Tracking Geografic" sub={`${active.length} active · refresh 2s`} c={c}>
            <div style={{ padding: 8 }}>
              <window.RomaniaMap
                transports={transports}
                c={c}
                height={360}
                onSelectTransport={setSelectedId}
                selectedId={selectedId}
              />
            </div>
          </Panel>

          <Panel title="Stream alerte" sub="time-ordered · live" c={c} accentBar>
            <div style={{ padding: 12, display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 400, overflow: 'auto' }}>
              {unack.map((a) => (
                <AlertStreamRow key={a.id} alert={a} c={c} />
              ))}
              {alerts.filter((a) => a.acknowledged).map((a) => (
                <AlertStreamRow key={a.id} alert={a} c={c} dim />
              ))}
            </div>
          </Panel>
        </div>

        {/* Row 3: Transport table + Telemetry */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <Panel title="Transporturi" sub={`${transports.length} total · selectează pentru detaliu`} c={c}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
              <thead>
                <tr style={{ background: c.bgSunk }}>
                  <Th c={c}>Cod</Th>
                  <Th c={c}>Tip</Th>
                  <Th c={c}>Rută</Th>
                  <Th c={c}>Temp</Th>
                  <Th c={c}>Vit.</Th>
                  <Th c={c}>ETA</Th>
                  <Th c={c} align="right">Stare</Th>
                </tr>
              </thead>
              <tbody>
                {transports.map((t) => {
                  const tempOK = t.tempCurrent >= t.tempMin && t.tempCurrent <= t.tempMax;
                  const accent = t.alerts > 0 ? c.coral : t.status === 'IN_TRANSIT' ? c.teal : c.plum;
                  return (
                    <tr
                      key={t.id}
                      onClick={() => setSelectedId(t.id)}
                      style={{
                        cursor: 'pointer',
                        background: selectedId === t.id ? c.tealBg : 'transparent',
                        borderTop: `1px solid ${c.lineSoft}`,
                      }}
                    >
                      <Td c={c} mono>
                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                          <span style={{ width: 4, alignSelf: 'stretch', background: accent, borderRadius: 2 }} />
                          {t.code.replace('VL-2026-', '')}
                        </span>
                      </Td>
                      <Td c={c}>{window.TYPE_CFG[t.type].label}</Td>
                      <Td c={c} mono dim>{t.origin.short.slice(0,3).toUpperCase()} → {t.destination.short.slice(0,3).toUpperCase()}</Td>
                      <Td c={c} mono color={tempOK ? c.ink : c.coral}>
                        {t.tempCurrent.toFixed(1)}°
                      </Td>
                      <Td c={c} mono dim>{t.speed}</Td>
                      <Td c={c} mono dim>
                        {t.status === 'IN_TRANSIT' ? window.fmtDuration(t.etaMin) :
                         t.status === 'SCHEDULED' ? `+${window.fmtDuration(t.etaMin)}` :
                         '—'}
                      </Td>
                      <Td c={c} align="right">
                        <window.StatusPill status={t.status} c={c} dense />
                      </Td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </Panel>

          <Panel
            title={`Telemetrie · ${selected.code}`}
            sub={`${selected.subtype}`}
            c={c}
          >
            <TelemetryView t={selected} telemetry={telemetry} c={c} />
          </Panel>
        </div>
      </div>
    </div>
  );
}

function KpiInline({ label, value, c, ok, warn }) {
  const valColor = warn ? c.coral : ok ? c.teal : c.ink;
  return (
    <span>
      <span style={{ color: c.inkFaint }}>{label} </span>
      <span style={{ color: valColor, fontWeight: 600 }}>{value}</span>
    </span>
  );
}

function KpiTile({ label, value, sub, trend, c, accent, warn, unit }) {
  return (
    <div style={{
      background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 10,
      padding: '14px 16px', display: 'flex', flexDirection: 'column', gap: 4,
      position: 'relative', overflow: 'hidden',
    }}>
      <div style={{ position: 'absolute', top: 0, left: 0, width: 3, height: '100%', background: accent }} />
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div style={{ fontSize: 10, color: c.inkMute, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em', paddingLeft: 4 }}>
          {label}
        </div>
        {warn && <span style={{ width: 6, height: 6, borderRadius: '50%', background: accent, animation: 'vl-pulse 1.4s ease-in-out infinite' }} />}
      </div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 6, paddingLeft: 4 }}>
        <span style={{ fontFamily: 'Instrument Serif, serif', fontSize: 36, fontWeight: 400, color: c.ink, letterSpacing: '-0.02em', lineHeight: 1 }}>
          {value}
        </span>
        {unit && <span style={{ fontSize: 12, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>{unit}</span>}
      </div>
      <div style={{ fontSize: 10, color: c.inkMute, paddingLeft: 4, fontFamily: 'JetBrains Mono, monospace' }}>{sub}</div>
      <div style={{ marginTop: 4 }}>
        <window.Sparkline data={trend} c={c} color={accent} w={300} h={22} />
      </div>
    </div>
  );
}

function Panel({ title, sub, c, children, accentBar }) {
  return (
    <div style={{
      background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 10,
      overflow: 'hidden', display: 'flex', flexDirection: 'column',
    }}>
      <div style={{
        padding: '10px 14px', background: c.bgSunk,
        borderBottom: `1px solid ${c.lineSoft}`,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {accentBar && <span style={{ width: 6, height: 6, borderRadius: '50%', background: c.coral, animation: 'vl-pulse 1.4s ease-in-out infinite' }} />}
          <span style={{ fontSize: 11, fontWeight: 700, color: c.ink, letterSpacing: '0.06em', textTransform: 'uppercase' }}>
            {title}
          </span>
          {sub && <span style={{ fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>{sub}</span>}
        </div>
      </div>
      <div style={{ flex: 1 }}>{children}</div>
    </div>
  );
}

function Th({ c, children, align }) {
  return (
    <th style={{
      padding: '8px 12px', textAlign: align || 'left',
      fontSize: 10, fontWeight: 700, color: c.inkMute,
      letterSpacing: '0.08em', textTransform: 'uppercase',
    }}>{children}</th>
  );
}

function Td({ c, children, mono, dim, color, align }) {
  return (
    <td style={{
      padding: '10px 12px', textAlign: align || 'left',
      fontFamily: mono ? 'JetBrains Mono, monospace' : 'inherit',
      color: color || (dim ? c.inkMute : c.ink),
      fontSize: 12, fontWeight: 500,
    }}>{children}</td>
  );
}

function AlertStreamRow({ alert: a, c, dim }) {
  const sevColor = {
    CRITICAL: c.coral, HIGH: c.amber, MEDIUM: c.plum, LOW: c.moss,
  }[a.severity];
  return (
    <div style={{
      display: 'flex', gap: 10,
      padding: '10px 12px', borderRadius: 8,
      background: dim ? c.bgSunk : (a.severity === 'CRITICAL' ? c.coralBg : c.bgSunk),
      border: a.severity === 'CRITICAL' && !dim ? `1px solid ${c.coral}40` : `1px solid ${c.lineSoft}`,
      opacity: dim ? 0.55 : 1,
    }}>
      <window.SeverityDot severity={a.severity} c={c} size={8} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 11, fontFamily: 'JetBrains Mono, monospace', color: c.inkMute, marginBottom: 3, display: 'flex', justifyContent: 'space-between' }}>
          <span style={{ color: sevColor, fontWeight: 700 }}>{a.severity}</span>
          <span>{window.timeAgo(a.minutesAgo)}</span>
        </div>
        <div style={{ fontSize: 12, color: dim ? c.inkMute : c.ink, fontWeight: 500, lineHeight: 1.4, marginBottom: 4, textDecoration: dim ? 'line-through' : 'none' }}>
          {a.message}
        </div>
        <div style={{ fontSize: 10, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
          {a.transportCode} · {a.location}
        </div>
        {dim && a.acknowledgedBy && (
          <div style={{ fontSize: 10, color: c.moss, fontFamily: 'JetBrains Mono, monospace', marginTop: 3 }}>
            ✓ confirmat de {a.acknowledgedBy}
          </div>
        )}
      </div>
    </div>
  );
}

function TelemetryView({ t, telemetry, c }) {
  const tempData = telemetry.map((p) => p.temp);
  const speedData = telemetry.map((p) => p.speed);
  const humData = telemetry.map((p) => p.humidity);
  const tempOK = t.tempCurrent >= t.tempMin && t.tempCurrent <= t.tempMax;

  return (
    <div style={{ padding: 14 }}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10, marginBottom: 14 }}>
        <BigStat label="Temperatură" value={t.tempCurrent.toFixed(1)} unit="°C" sub={`Prag ${t.tempMin}° – ${t.tempMax}°`} ok={tempOK} c={c} />
        <BigStat label="Viteză" value={String(t.speed)} unit="km/h" sub="medie 92" ok c={c} />
        <BigStat label="Umiditate" value={String(t.humidity)} unit="%" sub="în normal" ok c={c} />
      </div>

      <div style={{
        background: c.bgSunk, borderRadius: 8, padding: 12,
        border: `1px solid ${c.lineSoft}`,
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
          <span style={{ fontSize: 10, color: c.inkMute, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase' }}>
            Temperatură · ultimele 60 min
          </span>
          <div style={{ display: 'flex', gap: 12, fontSize: 10, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
            <span>min {Math.min(...tempData).toFixed(1)}°</span>
            <span>max {Math.max(...tempData).toFixed(1)}°</span>
            <span style={{ color: c.coral }}>· prag {t.tempMax}°</span>
          </div>
        </div>
        <window.Sparkline data={tempData} c={c} color={tempOK ? c.teal : c.coral} threshold={{ min: t.tempMin, max: t.tempMax }} w={620} h={70} />
      </div>

      {/* Recent telemetry events table */}
      <div style={{ marginTop: 12 }}>
        <div style={{ fontSize: 10, color: c.inkMute, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 6 }}>
          Evenimente telemetrie
        </div>
        <table style={{ width: '100%', fontSize: 11, fontFamily: 'JetBrains Mono, monospace', borderCollapse: 'collapse' }}>
          <tbody>
            {telemetry.slice(-5).reverse().map((p, i) => {
              const warn = p.temp > t.tempMax || p.temp < t.tempMin;
              return (
                <tr key={i} style={{ borderBottom: `1px solid ${c.lineSoft}` }}>
                  <td style={{ padding: '6px 0', color: c.inkMute, width: 70 }}>{p.time}</td>
                  <td style={{ padding: '6px 0', color: warn ? c.coral : c.ink, fontWeight: 600 }}>{p.temp.toFixed(2)}°C</td>
                  <td style={{ padding: '6px 0', color: c.inkMute }}>{p.humidity}%</td>
                  <td style={{ padding: '6px 0', color: c.inkMute }}>{p.speed} km/h</td>
                  <td style={{ padding: '6px 0', color: c.inkFaint, textAlign: 'right' }}>
                    {warn ? '⚠ peste prag' : 'normal'}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function BigStat({ label, value, unit, sub, ok, c }) {
  return (
    <div style={{ background: c.bgSunk, borderRadius: 8, padding: '10px 12px' }}>
      <div style={{ fontSize: 9, color: c.inkMute, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 3 }}>
        {label}
      </div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 4 }}>
        <span style={{
          fontFamily: 'JetBrains Mono, monospace', fontSize: 22, fontWeight: 600,
          color: ok ? c.ink : c.coral, letterSpacing: '-0.02em',
        }}>{value}</span>
        <span style={{ fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>{unit}</span>
      </div>
      <div style={{ fontSize: 10, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>{sub}</div>
    </div>
  );
}

window.VariantMission = VariantMission;
