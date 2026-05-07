// Variation C — Driver Mobile App (iOS, in-cabin view)
// Single-purpose: the driver's active transport, big touch targets, glanceable.

function VariantDriver({ mode = 'light' }) {
  const c = window.useTheme(mode);
  const { transports, alerts, tick } = window.useLiveData();
  const t = transports.find((x) => x.id === 'b1b2-0001');
  const tempOK = t.tempCurrent >= t.tempMin && t.tempCurrent <= t.tempMax;
  const myAlerts = alerts.filter((a) => a.transportId === t.id && !a.acknowledged);
  const telemetry = React.useMemo(() => window.makeTelemetry(t), [t.id, tick]);
  const tempData = telemetry.map((p) => p.temp);

  return (
    <window.IOSDevice
      width={390}
      height={844}
      dark={mode === 'dark'}
    >
      <div style={{
        width: '100%', height: '100%',
        background: c.bg, color: c.ink,
        fontFamily: 'Inter, sans-serif',
        display: 'flex', flexDirection: 'column',
        overflow: 'hidden',
      }}>
        {/* Top */}
        <div style={{ padding: '8px 20px 12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <window.Wordmark size={14} c={c} />
          <button style={{
            width: 36, height: 36, borderRadius: '50%',
            background: c.bgSunk, border: 'none', cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke={c.inkSoft} strokeWidth="1.8">
              <circle cx="12" cy="12" r="3" />
              <path d="M12 1v6m0 10v6M4.22 4.22l4.24 4.24m7.08 7.08l4.24 4.24M1 12h6m10 0h6M4.22 19.78l4.24-4.24m7.08-7.08l4.24-4.24" />
            </svg>
          </button>
        </div>

        {/* Mission card */}
        <div style={{ padding: '0 16px 16px' }}>
          <div style={{
            background: c.teal, color: c.bgPanel,
            borderRadius: 22, padding: '20px 22px',
            position: 'relative', overflow: 'hidden',
          }}>
            <div style={{
              position: 'absolute', top: -40, right: -40,
              width: 160, height: 160, borderRadius: '50%',
              background: c.tealSoft, opacity: 0.5,
            }} />
            <div style={{ position: 'relative' }}>
              <div style={{ fontSize: 10, color: '#ffffff99', fontWeight: 600, letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: 8 }}>
                Misiune activă · {t.code}
              </div>
              <div style={{ fontFamily: 'Instrument Serif, serif', fontSize: 26, lineHeight: 1.15, fontWeight: 400, marginBottom: 10, letterSpacing: '-0.01em' }}>
                {t.subtype}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, opacity: 0.9 }}>
                <span>{t.origin.short}</span>
                <svg width="20" height="6" viewBox="0 0 20 6">
                  <line x1="0" y1="3" x2="14" y2="3" stroke="#ffffff" strokeWidth="1" />
                  <path d="M14,0 L20,3 L14,6 Z" fill="#ffffff" />
                </svg>
                <span style={{ fontWeight: 600 }}>{t.destination.short}</span>
                <span style={{ marginLeft: 'auto', fontSize: 11, opacity: 0.8, fontFamily: 'JetBrains Mono, monospace' }}>
                  {Math.round(t.progressPct)}%
                </span>
              </div>
              <div style={{ height: 4, background: '#ffffff22', borderRadius: 2, marginTop: 8, overflow: 'hidden' }}>
                <div style={{ height: '100%', width: `${t.progressPct}%`, background: '#ffffff' }} />
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 14, fontFamily: 'JetBrains Mono, monospace' }}>
                <div>
                  <div style={{ fontSize: 9, opacity: 0.7, textTransform: 'uppercase', letterSpacing: '0.08em' }}>ETA</div>
                  <div style={{ fontSize: 18, fontWeight: 600 }}>{window.fmtDuration(t.etaMin)}</div>
                </div>
                <div>
                  <div style={{ fontSize: 9, opacity: 0.7, textTransform: 'uppercase', letterSpacing: '0.08em' }}>Distanță</div>
                  <div style={{ fontSize: 18, fontWeight: 600 }}>{t.distanceRemaining} km</div>
                </div>
                <div>
                  <div style={{ fontSize: 9, opacity: 0.7, textTransform: 'uppercase', letterSpacing: '0.08em' }}>Viabilitate</div>
                  <div style={{ fontSize: 18, fontWeight: 600 }}>{t.viabilityHoursLeft.toFixed(1)}h</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Vital reading */}
        <div style={{ padding: '0 16px 14px' }}>
          <div style={{
            background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 18,
            padding: '16px 18px',
          }}>
            <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 10 }}>
              <div>
                <div style={{ fontSize: 10, color: c.inkMute, fontWeight: 600, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 4 }}>
                  Temperatură container
                </div>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
                  <span style={{ fontFamily: 'Instrument Serif, serif', fontSize: 38, color: tempOK ? c.ink : c.coral, fontWeight: 400, letterSpacing: '-0.02em', lineHeight: 1 }}>
                    {t.tempCurrent.toFixed(1)}
                  </span>
                  <span style={{ fontSize: 14, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>°C</span>
                  <span style={{
                    marginLeft: 6, fontSize: 10, padding: '3px 8px', borderRadius: 999,
                    background: tempOK ? c.tealBg : c.coralBg,
                    color: tempOK ? c.teal : c.coral, fontWeight: 700,
                    letterSpacing: '0.04em', textTransform: 'uppercase',
                  }}>
                    {tempOK ? 'În prag' : 'Atenție'}
                  </span>
                </div>
              </div>
              <window.Sparkline data={tempData} c={c} color={tempOK ? c.teal : c.coral} threshold={{ min: t.tempMin, max: t.tempMax }} w={100} h={40} />
            </div>
            <window.TempGauge current={t.tempCurrent} min={t.tempMin} max={t.tempMax} c={c} w={310} />
          </div>
        </div>

        {/* Stats grid */}
        <div style={{ padding: '0 16px 14px', display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
          <MiniStat label="Viteză" value={t.speed} unit="km/h" c={c} />
          <MiniStat label="Umiditate" value={t.humidity} unit="%" c={c} />
          <MiniStat label="Pornit" value={Math.floor(t.startedAgoMin/60)+'h'+(t.startedAgoMin%60)+'m'} c={c} />
        </div>

        {/* Alerts (if any) */}
        {myAlerts.length > 0 && (
          <div style={{ padding: '0 16px 14px' }}>
            <div style={{
              background: c.coralBg, border: `1px solid ${c.coral}40`,
              borderRadius: 14, padding: '12px 14px',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                <window.SeverityDot severity={myAlerts[0].severity} c={c} size={10} />
                <span style={{ fontSize: 11, fontWeight: 700, color: c.coral, letterSpacing: '0.06em', textTransform: 'uppercase' }}>
                  {myAlerts.length} alert{myAlerts.length > 1 ? 'e' : 'ă'} dispecerat
                </span>
              </div>
              <div style={{ fontSize: 13, color: c.ink, fontWeight: 500, lineHeight: 1.4 }}>
                {myAlerts[0].message}
              </div>
            </div>
          </div>
        )}

        {/* Quick actions */}
        <div style={{ padding: '0 16px 14px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          <ActionBtn label="Apel dispecerat" icon="phone" c={c} primary />
          <ActionBtn label="Raportează incident" icon="alert" c={c} />
        </div>

        {/* Bottom nav */}
        <div style={{ marginTop: 'auto', borderTop: `1px solid ${c.line}`, background: c.bgPanel, padding: '8px 24px 14px', display: 'flex', justifyContent: 'space-between' }}>
          {[
            { label: 'Misiune', active: true, ic: 'm' },
            { label: 'Hartă', active: false, ic: 'h' },
            { label: 'Telemetrie', active: false, ic: 't' },
            { label: 'Profil', active: false, ic: 'p' },
          ].map((n) => (
            <div key={n.label} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3, color: n.active ? c.teal : c.inkFaint, fontSize: 10, fontWeight: 600 }}>
              <div style={{
                width: 28, height: 28, borderRadius: 8,
                background: n.active ? c.tealBg : 'transparent',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
                <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 12, fontWeight: 700 }}>{n.ic}</span>
              </div>
              {n.label}
            </div>
          ))}
        </div>
      </div>
    </window.IOSDevice>
  );
}

function MiniStat({ label, value, unit, c }) {
  return (
    <div style={{ background: c.bgPanel, border: `1px solid ${c.line}`, borderRadius: 14, padding: '10px 12px' }}>
      <div style={{ fontSize: 9, color: c.inkMute, fontWeight: 600, letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 3 }}>
        {label}
      </div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 3 }}>
        <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 18, color: c.ink, fontWeight: 600 }}>{value}</span>
        {unit && <span style={{ fontSize: 10, color: c.inkMute, fontFamily: 'JetBrains Mono, monospace' }}>{unit}</span>}
      </div>
    </div>
  );
}

function ActionBtn({ label, icon, c, primary }) {
  const ics = {
    phone: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>,
    alert: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>,
  };
  return (
    <button style={{
      padding: '14px 16px', borderRadius: 14,
      background: primary ? c.teal : c.bgPanel,
      color: primary ? c.bgPanel : c.ink,
      border: primary ? 'none' : `1px solid ${c.line}`,
      display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
      fontSize: 14, fontWeight: 600, fontFamily: 'inherit', cursor: 'pointer',
    }}>
      {ics[icon]}
      {label}
    </button>
  );
}

window.VariantDriver = VariantDriver;
