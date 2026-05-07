// Brand tokens + shared primitives (Health-tech premium)
const TOKENS = {
  light: {
    bg: '#F6F2EA',
    bgPanel: '#FBF8F2',
    bgSunk: '#EFE9DD',
    line: '#E2D9C7',
    lineSoft: '#EBE2D0',
    ink: '#1A2A2A',
    inkSoft: '#3D5050',
    inkMute: '#7A8585',
    inkFaint: '#A89F8E',
    teal: '#0F4C4A',
    tealSoft: '#1B6562',
    tealBg: '#E5EDEB',
    coral: '#C84F3F',
    coralBg: '#F5E2DC',
    amber: '#B07523',
    amberBg: '#F2E6CC',
    moss: '#4A6E3F',
    mossBg: '#E5ECDA',
    plum: '#6B3A5C',
    plumBg: '#EFE0E9',
  },
  dark: {
    bg: '#0E1818',
    bgPanel: '#152222',
    bgSunk: '#0A1212',
    line: '#243333',
    lineSoft: '#1C2929',
    ink: '#F0EBDF',
    inkSoft: '#C8C2B4',
    inkMute: '#8A8B82',
    inkFaint: '#5A5C54',
    teal: '#5FB8B0',
    tealSoft: '#3E8E87',
    tealBg: '#1A2E2C',
    coral: '#E87A6A',
    coralBg: '#3A1E18',
    amber: '#D9A45D',
    amberBg: '#332416',
    moss: '#9BBE85',
    mossBg: '#1F2C18',
    plum: '#C49AB6',
    plumBg: '#2E1F2A',
  },
};

function useTheme(mode) {
  return TOKENS[mode] || TOKENS.light;
}

// VitalLink wordmark — original geometric mark (NOT a heart)
function Wordmark({ size = 18, c }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <svg width={size + 6} height={size + 6} viewBox="0 0 24 24" fill="none">
        <rect x="2" y="2" width="20" height="20" rx="5" fill={c.teal} />
        <path
          d="M5 13.5 L9 13.5 L10.5 9 L13 17 L14.5 11 L19 11"
          stroke={c.bgPanel}
          strokeWidth="1.7"
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </svg>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
        <span style={{ fontFamily: 'Instrument Serif, serif', fontSize: size + 4, color: c.ink, letterSpacing: '-0.01em', fontWeight: 400 }}>
          VitalLink
        </span>
      </div>
    </div>
  );
}

// Status pill
function StatusPill({ status, c, dense }) {
  const cfg = window.STATUS_CFG[status] || { label: status, tone: 'neutral' };
  const map = {
    live: { fg: c.teal, bg: c.tealBg, dot: c.teal },
    done: { fg: c.inkMute, bg: c.bgSunk, dot: c.inkMute },
    neutral: { fg: c.plum, bg: c.plumBg, dot: c.plum },
    warn: { fg: c.amber, bg: c.amberBg, dot: c.amber },
    cancel: { fg: c.coral, bg: c.coralBg, dot: c.coral },
  };
  const s = map[cfg.tone];
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      fontSize: dense ? 10 : 11, fontWeight: 600, letterSpacing: '0.04em',
      textTransform: 'uppercase',
      color: s.fg, background: s.bg, padding: dense ? '2px 7px' : '4px 10px',
      borderRadius: 999,
    }}>
      <span style={{
        width: 6, height: 6, borderRadius: '50%', background: s.dot,
        boxShadow: cfg.tone === 'live' ? `0 0 0 3px ${s.dot}22` : 'none',
        animation: cfg.tone === 'live' ? 'vl-pulse 1.6s ease-in-out infinite' : 'none',
      }} />
      {cfg.label}
    </span>
  );
}

function SeverityDot({ severity, c, size = 8 }) {
  const map = {
    CRITICAL: c.coral,
    HIGH: c.amber,
    MEDIUM: c.plum,
    LOW: c.moss,
  };
  return (
    <span style={{
      width: size, height: size, borderRadius: '50%',
      background: map[severity], display: 'inline-block', flexShrink: 0,
      boxShadow: severity === 'CRITICAL' ? `0 0 0 4px ${map.CRITICAL}22` : 'none',
    }} />
  );
}

// Mini sparkline
function Sparkline({ data, c, color, threshold, w = 120, h = 30 }) {
  if (!data || !data.length) return null;
  const min = Math.min(...data, threshold?.min ?? Infinity);
  const max = Math.max(...data, threshold?.max ?? -Infinity);
  const range = max - min || 1;
  const pts = data.map((v, i) => {
    const x = (i / (data.length - 1)) * w;
    const y = h - ((v - min) / range) * (h - 4) - 2;
    return [x, y];
  });
  const path = pts.map((p, i) => (i ? 'L' : 'M') + p[0].toFixed(1) + ',' + p[1].toFixed(1)).join(' ');
  const area = path + ` L ${w},${h} L 0,${h} Z`;
  const lineColor = color || c.teal;
  return (
    <svg width={w} height={h} style={{ display: 'block' }}>
      {threshold?.max != null && (
        <line
          x1={0} x2={w}
          y1={h - ((threshold.max - min) / range) * (h - 4) - 2}
          y2={h - ((threshold.max - min) / range) * (h - 4) - 2}
          stroke={c.coral} strokeDasharray="2 3" strokeWidth="1" opacity="0.5"
        />
      )}
      <path d={area} fill={lineColor} opacity="0.12" />
      <path d={path} fill="none" stroke={lineColor} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx={pts[pts.length - 1][0]} cy={pts[pts.length - 1][1]} r="2.5" fill={lineColor} />
    </svg>
  );
}

// Romania map shape — SVG outline (simplified)
const RO_PATH = "M82,128 L98,118 L118,108 L138,98 L158,92 L182,88 L208,92 L232,98 L258,108 L282,118 L302,132 L322,148 L338,168 L348,188 L352,208 L348,228 L338,248 L322,262 L298,272 L272,278 L246,278 L222,272 L198,262 L172,248 L148,232 L128,212 L108,192 L92,168 L82,148 Z";

// Map dot for a city
function CityDot({ city, c, color, size = 8, label, status }) {
  const positions = {
    'Iași': { x: 304, y: 138 },
    'București': { x: 220, y: 282 },
    'Suceava': { x: 252, y: 102 },
    'Bacău': { x: 254, y: 178 },
    'Botoșani': { x: 274, y: 100 },
    'Cluj-Napoca': { x: 132, y: 158 },
  };
  const pos = positions[city] || { x: 200, y: 200 };
  return { pos, color };
}

// Compact temperature gauge
function TempGauge({ current, min, max, c, w = 140 }) {
  const range = max - min;
  const pad = range * 0.4;
  const lo = min - pad;
  const hi = max + pad;
  const pct = Math.max(0, Math.min(1, (current - lo) / (hi - lo)));
  const minPct = (min - lo) / (hi - lo);
  const maxPct = (max - lo) / (hi - lo);
  const inRange = current >= min && current <= max;
  const dotColor = inRange ? c.teal : c.coral;
  return (
    <div style={{ width: w }}>
      <div style={{
        position: 'relative', height: 6, borderRadius: 999,
        background: c.bgSunk, overflow: 'visible',
      }}>
        <div style={{
          position: 'absolute', left: `${minPct * 100}%`, width: `${(maxPct - minPct) * 100}%`,
          top: 0, bottom: 0, background: c.tealBg, borderRadius: 999,
        }} />
        <div style={{
          position: 'absolute', left: `calc(${pct * 100}% - 5px)`, top: -2,
          width: 10, height: 10, borderRadius: '50%', background: dotColor,
          boxShadow: `0 0 0 3px ${c.bgPanel}, 0 0 0 4px ${dotColor}55`,
        }} />
      </div>
      <div style={{
        display: 'flex', justifyContent: 'space-between',
        fontSize: 10, color: c.inkFaint, marginTop: 4,
        fontFamily: 'JetBrains Mono, monospace',
      }}>
        <span>{min}°</span>
        <span style={{ color: dotColor, fontWeight: 600 }}>{current}°</span>
        <span>{max}°</span>
      </div>
    </div>
  );
}

// Time-ago helper
function timeAgo(min) {
  if (min < 1) return 'acum';
  if (min < 60) return `acum ${min}m`;
  const h = Math.floor(min / 60);
  const m = min % 60;
  return `acum ${h}h ${m ? m + 'm' : ''}`.trim();
}

function fmtDuration(min) {
  if (min < 60) return `${min}m`;
  return `${Math.floor(min / 60)}h ${min % 60}m`;
}

function typeIcon(type, color, size = 16) {
  const ic = {
    ORGAN: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
        <path d="M9 4c-3 0-5 2.5-5 5.5 0 4 4 6 8 10 4-4 8-6 8-10 0-3-2-5.5-5-5.5-1.5 0-2.5.7-3 1.5C11.5 4.7 10.5 4 9 4z" />
      </svg>
    ),
    BLOOD_PRODUCT: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 3 C8 9 5 12 5 15.5 A7 7 0 0 0 19 15.5 C19 12 16 9 12 3z" />
      </svg>
    ),
    BIOLOGICAL_SAMPLE: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
        <path d="M9 3h6v3l-2 2v8a3 3 0 0 1-6 0V8l2-2V3z" transform="translate(2 0)" />
        <line x1="9" y1="14" x2="15" y2="14" transform="translate(2 0)" />
      </svg>
    ),
    THERMOSENSITIVE_MEDICATION: (
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="8" width="18" height="9" rx="2" />
        <path d="M7 8V6a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v2" />
        <line x1="9" y1="12" x2="15" y2="12" />
      </svg>
    ),
  };
  return ic[type] || ic.ORGAN;
}

Object.assign(window, { TOKENS, useTheme, Wordmark, StatusPill, SeverityDot, Sparkline, TempGauge, timeAgo, fmtDuration, typeIcon, CityDot });
