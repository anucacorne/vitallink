// Variation A — Editorial Dispatch (desktop dispatcher dashboard)
// Tone: premium, editorial, calm. Big serif numbers, generous whitespace.

function VariantEditorial({ mode = 'light' }) {
  const c = window.useTheme(mode);
  const { transports, alerts, tick } = window.useLiveData();
  const [selectedId, setSelectedId] = React.useState('b1b2-0001');
  const selected = transports.find((t) => t.id === selectedId) || transports[0];
  const active = transports.filter((t) => t.status === 'IN_TRANSIT');
  const unack = alerts.filter((a) => !a.acknowledged);
  const critical = unack.filter((a) => a.severity === 'CRITICAL');
  const telemetry = React.useMemo(() => window.makeTelemetry(selected), [selected.id, tick]);
  const tempData = telemetry.map((p) => p.temp);

  return (
    <div style={{
      width: 1440, minHeight: 1000,
      background: c.bg, color: c.ink,
      fontFamily: 'Inter, sans-serif',
      display: 'flex', flexDirection: 'column',
    }}>
      {/* Top bar */}
      <header style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '20px 40px', borderBottom: `1px solid ${c.line}`,
        background: c.bgPanel,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 32 }}>
          <window.Wordmark size={20} c={c} />
          <div style={{ width: 1, height: 22, background: c.line }} />
          <nav style={{ display: 'flex', gap: 4 }}>
            {['Centru de control', 'Transporturi', 'Alerte', 'Flotă', 'Audit'].map((label, i) => (
              <button key={label} style={{
                padding: '8px 14px', fontSize: 13, fontWeight: i === 0 ? 600 : 400,
                color: i === 0 ? c.ink : c.inkMute, background: i === 0 ? c.bgSunk : 'transparent',
                border: 'none', borderRadius: 8, cursor: 'pointer', fontFamily: 'inherit',
              }}>{label}</button>
            ))}
          </nav>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <div style={{ fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
            <span style={{
              display: 'inline-block', width: 6, height: 6, borderRadius: '50%',
              background: c.teal, marginRight: 6,
              animation: 'vl-pulse 1.6s ease-in-out infinite',
            }} />
            LIVE · sync 2s
          </div>
          <div style={{
            display: 'flex', alignItems: 'center', gap: 8,
            padding: '6px 10px 6px 6px', borderRadius: 999,
            background: c.bgSunk, border: `1px solid ${c.line}`,
          }}>
            <div style={{
              width: 26, height: 26, borderRadius: '50%',
              background: c.teal, color: c.bgPanel,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 11, fontWeight: 700,
            }}>MD</div>
            <span style={{ fontSize: 12, color: c.inkSoft, fontWeight: 500 }}>M. Dumitrescu</span>
            <span style={{ fontSize: 11, color: c.inkFaint }}>· dispecer</span>
          </div>
        </div>
      </header>

      {/* Editorial heading */}
      <div style={{ padding: '32px 40px 24px', borderBottom: `1px solid ${c.line}` }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
          <div>
            <div style={{ fontSize: 11, color: c.inkMute, letterSpacing: '0.16em', textTransform: 'uppercase', marginBottom: 8, fontWeight: 600 }}>
              Joi · 7 mai 2026 · 14:42
            </div>
            <h1 style={{
              fontFamily: 'Instrument Serif, serif', fontSize: 52, lineHeight: 1.05,
              fontWeight: 400, color: c.ink, letterSpacing: '-0.02em',
              maxWidth: 720, textWrap: 'pretty',
            }}>
              <span style={{ color: c.inkMute }}>Acum în tranzit:</span> {active.length} transporturi medicale critice către {new Set(active.map(t => t.destination.short)).size} destinații.
            </h1>
          </div>
          <div style={{ display: 'flex', gap: 32, fontFamily: 'JetBrains Mono, monospace' }}>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: 10, color: c.inkMute, textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: 4 }}>Alerte deschise</div>
              <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 36, color: critical.length ? c.coral : c.ink, fontWeight: 400 }}>
                {String(unack.length).padStart(2, '0')}
              </div>
              <div style={{ fontSize: 11, color: c.inkMute }}>{critical.length} critice</div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: 10, color: c.inkMute, textTransform: 'uppercase', letterSpacing: '0.1em', marginBottom: 4 }}>Programate azi</div>
              <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 36, color: c.ink, fontWeight: 400 }}>
                {String(transports.filter(t => t.status === 'SCHEDULED').length).padStart(2, '0')}
              </div>
              <div style={{ fontSize: 11, color: c.inkMute }}>următoarea în 1h 47m</div>
            </div>
          </div>
        </div>
      </div>

      {/* Main grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: 0, flex: 1 }}>
        {/* Left: Map + transport list */}
        <div style={{ padding: '24px 24px 24px 40px', borderRight: `1px solid ${c.line}` }}>
          <div style={{
            background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 16,
            overflow: 'hidden',
          }}>
            <div style={{
              padding: '14px 20px', borderBottom: `1px solid ${c.lineSoft}`,
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <span style={{ fontSize: 12, fontWeight: 600, color: c.ink, letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                  Hartă transport
                </span>
                <span style={{ fontSize: 11, color: c.inkMute }}>· România · {active.length} active</span>
              </div>
              <div style={{ display: 'flex', gap: 14, fontSize: 10, color: c.inkMute }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                  <span style={{ width: 8, height: 8, borderRadius: '50%', background: c.teal }} /> În tranzit
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                  <span style={{ width: 8, height: 8, borderRadius: '50%', background: c.coral }} /> Cu alerte
                </span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                  <span style={{ width: 8, height: 8, borderRadius: '50%', background: c.plum }} /> Programat
                </span>
              </div>
            </div>
            <div style={{ padding: 8 }}>
              <window.RomaniaMap
                transports={transports}
                c={c}
                height={380}
                onSelectTransport={setSelectedId}
                selectedId={selectedId}
              />
            </div>
          </div>

          {/* Transport list */}
          <div style={{ marginTop: 24 }}>
            <div style={{
              display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
              marginBottom: 14,
            }}>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 10 }}>
                <h2 style={{ fontFamily: 'Instrument Serif, serif', fontSize: 24, fontWeight: 400, color: c.ink, letterSpacing: '-0.01em' }}>
                  Transporturi astăzi
                </h2>
                <span style={{ fontSize: 12, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
                  {transports.length} înregistrate
                </span>
              </div>
              <div style={{ display: 'flex', gap: 4 }}>
                {['Toate', 'În tranzit', 'Programate', 'Livrate'].map((f, i) => (
                  <button key={f} style={{
                    fontSize: 11, padding: '5px 11px', borderRadius: 999,
                    border: `1px solid ${i === 0 ? c.ink : c.line}`,
                    background: i === 0 ? c.ink : 'transparent',
                    color: i === 0 ? c.bgPanel : c.inkSoft,
                    fontWeight: 500, cursor: 'pointer', fontFamily: 'inherit',
                  }}>{f}</button>
                ))}
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {transports.map((t) => (
                <TransportCard key={t.id} t={t} c={c} selected={selectedId === t.id} onSelect={() => setSelectedId(t.id)} />
              ))}
            </div>
          </div>
        </div>

        {/* Right: Selected detail + alerts */}
        <div style={{ padding: '24px 40px 24px 24px', display: 'flex', flexDirection: 'column', gap: 20 }}>
          <SelectedDetail t={selected} telemetry={telemetry} c={c} />
          <AlertsPanel alerts={unack} c={c} />
        </div>
      </div>
    </div>
  );
}

function TransportCard({ t, c, selected, onSelect }) {
  const live = t.status === 'IN_TRANSIT';
  const tempOK = t.tempCurrent >= t.tempMin && t.tempCurrent <= t.tempMax;
  const accent = t.alerts > 0 ? c.coral : live ? c.teal : c.plum;
  return (
    <div
      onClick={onSelect}
      style={{
        background: selected ? c.bgPanel : c.bgPanel,
        border: `1px solid ${selected ? accent : c.line}`,
        borderRadius: 12, padding: '16px 18px', cursor: 'pointer',
        display: 'grid', gridTemplateColumns: '34px 1fr 220px 140px 90px', gap: 16,
        alignItems: 'center', transition: 'all 0.15s',
        boxShadow: selected ? `0 4px 16px ${accent}1a` : 'none',
      }}
    >
      <div style={{
        width: 34, height: 34, borderRadius: 8,
        background: c.bgSunk, display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: accent,
      }}>
        {window.typeIcon(t.type, accent, 18)}
      </div>
      <div style={{ minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 3 }}>
          <span style={{ fontSize: 14, fontWeight: 600, color: c.ink }}>{t.subtype}</span>
          {t.alerts > 0 && (
            <span style={{
              fontSize: 10, fontWeight: 700, padding: '2px 6px', borderRadius: 4,
              background: c.coralBg, color: c.coral,
            }}>
              {t.alerts} alerte
            </span>
          )}
        </div>
        <div style={{ fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace', letterSpacing: '0.02em' }}>
          {t.code} · {t.vehiclePlate} · {t.driver}
        </div>
      </div>
      <div style={{ fontSize: 12, color: c.inkSoft, display: 'flex', alignItems: 'center', gap: 8 }}>
        <span>{t.origin.short}</span>
        <svg width="40" height="6" viewBox="0 0 40 6">
          <line x1="0" y1="3" x2="34" y2="3" stroke={c.inkFaint} strokeWidth="1" strokeDasharray={live ? '0' : '2 2'} />
          <path d="M34,0 L40,3 L34,6 Z" fill={c.inkFaint} />
        </svg>
        <span style={{ fontWeight: 500, color: c.ink }}>{t.destination.short}</span>
      </div>
      <window.TempGauge current={t.tempCurrent} min={t.tempMin} max={t.tempMax} c={c} w={140} />
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4 }}>
        <window.StatusPill status={t.status} c={c} dense />
        <span style={{ fontSize: 10, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
          {live ? `ETA ${window.fmtDuration(t.etaMin)}` : t.status === 'SCHEDULED' ? `în ${window.fmtDuration(t.etaMin)}` : 'finalizat'}
        </span>
      </div>
    </div>
  );
}

function SelectedDetail({ t, telemetry, c }) {
  const accent = t.alerts > 0 ? c.coral : t.status === 'IN_TRANSIT' ? c.teal : c.plum;
  const tempData = telemetry.map((p) => p.temp);
  const speedData = telemetry.map((p) => p.speed);
  return (
    <div style={{
      background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 16,
      overflow: 'hidden',
    }}>
      <div style={{ padding: '16px 20px', borderBottom: `1px solid ${c.lineSoft}` }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
          <span style={{ fontSize: 11, color: c.inkMute, letterSpacing: '0.08em', textTransform: 'uppercase', fontWeight: 600 }}>
            Transport selectat
          </span>
          <window.StatusPill status={t.status} c={c} dense />
        </div>
        <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 22, color: c.ink, lineHeight: 1.2, letterSpacing: '-0.01em' }}>
          {t.subtype}
        </div>
        <div style={{ fontSize: 11, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace', marginTop: 4 }}>
          {t.code}
        </div>
      </div>
      <div style={{ padding: 20 }}>
        {/* Route */}
        <div style={{ marginBottom: 18 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
            <div style={{ width: 8, height: 8, borderRadius: '50%', background: accent }} />
            <div>
              <div style={{ fontSize: 12, color: c.ink, fontWeight: 500 }}>{t.origin.name}</div>
              <div style={{ fontSize: 10, color: c.inkMute }}>{t.origin.city} · plecare {window.timeAgo(t.startedAgoMin)}</div>
            </div>
          </div>
          {/* progress */}
          <div style={{ marginLeft: 4, paddingLeft: 8, borderLeft: `2px dashed ${c.line}`, padding: '8px 0 8px 12px' }}>
            <div style={{ height: 4, background: c.bgSunk, borderRadius: 2, overflow: 'hidden', marginBottom: 4 }}>
              <div style={{ height: '100%', width: `${t.progressPct}%`, background: accent, borderRadius: 2 }} />
            </div>
            <div style={{ fontSize: 10, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace', display: 'flex', justifyContent: 'space-between' }}>
              <span>{Math.round(t.progressPct)}% · {t.distanceKm - t.distanceRemaining}/{t.distanceKm} km</span>
              <span>ETA {window.fmtDuration(t.etaMin)}</span>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{ width: 8, height: 8, borderRadius: '50%', background: c.bgPanel, border: `2px solid ${accent}` }} />
            <div>
              <div style={{ fontSize: 12, color: c.ink, fontWeight: 500 }}>{t.destination.name}</div>
              <div style={{ fontSize: 10, color: c.inkMute }}>{t.destination.city}</div>
            </div>
          </div>
        </div>

        {/* Telemetry */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
          <TelemetryStat
            label="Temperatură"
            value={`${t.tempCurrent.toFixed(1)}°C`}
            range={`${t.tempMin}° – ${t.tempMax}°`}
            ok={t.tempCurrent >= t.tempMin && t.tempCurrent <= t.tempMax}
            data={tempData}
            threshold={{ min: t.tempMin, max: t.tempMax }}
            c={c}
          />
          <TelemetryStat
            label="Viteză"
            value={`${t.speed} km/h`}
            range="medie 92 km/h"
            ok
            data={speedData}
            c={c}
          />
        </div>

        {/* Viability */}
        <div style={{
          padding: '12px 14px', background: c.bgSunk, borderRadius: 10,
          marginBottom: 14,
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: c.inkMute, marginBottom: 6, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            <span>Fereastră viabilitate</span>
            <span style={{ fontFamily: 'JetBrains Mono, monospace', color: t.viabilityHoursLeft < 2 ? c.coral : c.inkSoft }}>
              {t.viabilityHoursLeft.toFixed(1)}h rămase
            </span>
          </div>
          <div style={{ height: 6, background: c.bgPanel, borderRadius: 3, overflow: 'hidden' }}>
            <div style={{
              height: '100%',
              width: `${(t.viabilityHoursLeft / t.viabilityHoursTotal) * 100}%`,
              background: t.viabilityHoursLeft < 2 ? c.coral : c.teal,
              borderRadius: 3,
            }} />
          </div>
        </div>

        {/* Vehicle / driver */}
        <div style={{ fontSize: 11, color: c.inkMute, lineHeight: 1.7, fontFamily: 'JetBrains Mono, monospace' }}>
          <div><span style={{ color: c.inkFaint }}>VEHICUL</span> {t.vehiclePlate} · {t.vehicleType}</div>
          <div><span style={{ color: c.inkFaint }}>ȘOFER</span> {t.driver} · {t.driverPhone}</div>
          <div><span style={{ color: c.inkFaint }}>CONTAINER</span> {t.containerSerial}</div>
        </div>
      </div>
    </div>
  );
}

function TelemetryStat({ label, value, range, ok, data, threshold, c }) {
  return (
    <div style={{ padding: '10px 12px', background: c.bgSunk, borderRadius: 10 }}>
      <div style={{ fontSize: 10, color: c.inkMute, textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 600, marginBottom: 4 }}>
        {label}
      </div>
      <div style={{
        fontFamily: 'JetBrains Mono, monospace', fontSize: 18, fontWeight: 600,
        color: ok ? c.ink : c.coral, letterSpacing: '-0.01em',
      }}>{value}</div>
      <div style={{ fontSize: 10, color: c.inkMute, marginBottom: 4 }}>{range}</div>
      <window.Sparkline data={data} c={c} color={ok ? c.teal : c.coral} threshold={threshold} w={140} h={26} />
    </div>
  );
}

function AlertsPanel({ alerts, c }) {
  return (
    <div style={{
      background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 16,
      overflow: 'hidden', flex: 1, display: 'flex', flexDirection: 'column',
    }}>
      <div style={{
        padding: '14px 20px', borderBottom: `1px solid ${c.lineSoft}`,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: 12, fontWeight: 600, color: c.ink, letterSpacing: '0.04em', textTransform: 'uppercase' }}>
            Alerte
          </span>
          <span style={{
            fontSize: 10, padding: '2px 7px', borderRadius: 999,
            background: c.coralBg, color: c.coral, fontWeight: 700,
          }}>{alerts.length}</span>
        </div>
        <button style={{
          fontSize: 11, color: c.inkMute, background: 'transparent', border: 'none',
          cursor: 'pointer', fontFamily: 'inherit',
        }}>Vezi toate ›</button>
      </div>
      <div style={{ padding: 12, display: 'flex', flexDirection: 'column', gap: 8, overflow: 'auto' }}>
        {alerts.map((a) => (
          <div key={a.id} style={{
            padding: '12px 14px', borderRadius: 10,
            background: a.severity === 'CRITICAL' ? c.coralBg : c.bgSunk,
            border: a.severity === 'CRITICAL' ? `1px solid ${c.coral}40` : `1px solid ${c.lineSoft}`,
          }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: 10, marginBottom: 6 }}>
              <window.SeverityDot severity={a.severity} c={c} size={8} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 12, color: c.ink, fontWeight: 600, marginBottom: 2, lineHeight: 1.4 }}>
                  {a.message}
                </div>
                <div style={{ fontSize: 10, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>
                  {a.transportCode} · {a.location} · {window.timeAgo(a.minutesAgo)}
                </div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
              <button style={{
                fontSize: 10, padding: '4px 10px', borderRadius: 6,
                background: a.severity === 'CRITICAL' ? c.coral : c.teal, color: c.bgPanel,
                border: 'none', fontWeight: 600, cursor: 'pointer', fontFamily: 'inherit',
              }}>Confirmă</button>
              <button style={{
                fontSize: 10, padding: '4px 10px', borderRadius: 6,
                background: 'transparent', color: c.inkSoft,
                border: `1px solid ${c.line}`, fontWeight: 500, cursor: 'pointer', fontFamily: 'inherit',
              }}>Detalii</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

window.VariantEditorial = VariantEditorial;
