// Stylized Romania map with transport routes
// Uses a simplified country outline + city markers + animated routes

const RO_OUTLINE = "M58,134 C70,118 88,108 110,102 C132,96 158,92 186,90 C212,88 240,90 268,96 C292,100 314,108 332,120 C348,130 358,144 364,160 C368,178 366,196 360,212 C352,228 340,240 322,248 C300,256 274,260 244,260 C214,260 186,254 162,244 C140,234 122,220 108,202 C96,186 84,168 74,150 C66,140 60,136 58,134 Z";

const CITY_POS = {
  'Iași': { x: 322, y: 130 },
  'București': { x: 232, y: 240 },
  'Suceava': { x: 274, y: 110 },
  'Bacău': { x: 270, y: 168 },
  'Botoșani': { x: 290, y: 102 },
  'Cluj-Napoca': { x: 152, y: 152 },
  'Timișoara': { x: 92, y: 188 },
  'Constanța': { x: 312, y: 226 },
  'Craiova': { x: 168, y: 232 },
  'Brașov': { x: 222, y: 192 },
};

function RomaniaMap({ transports, c, height = 380, showLabels = true, accentLive = true, onSelectTransport, selectedId }) {
  const W = 420;
  const H = height;
  const scale = H / 320;

  // Build routes from active transports
  const routes = transports
    .filter((t) => t.status === 'IN_TRANSIT' || t.status === 'SCHEDULED')
    .map((t) => {
      const a = CITY_POS[t.origin.short];
      const b = CITY_POS[t.destination.short];
      if (!a || !b) return null;
      const live = t.status === 'IN_TRANSIT';
      const accent = t.alerts > 0 ? c.coral : live ? c.teal : c.plum;
      const pos = {
        x: a.x + (b.x - a.x) * (t.progressPct / 100),
        y: a.y + (b.y - a.y) * (t.progressPct / 100),
      };
      return { transport: t, a, b, accent, pos, live };
    })
    .filter(Boolean);

  // Cities to render
  const cityList = Object.keys(CITY_POS).map((name) => ({ name, ...CITY_POS[name] }));

  return (
    <svg viewBox={`0 0 ${W} 320`} width="100%" height={H} style={{ display: 'block' }}>
      {/* Subtle grid */}
      <defs>
        <pattern id="vl-grid" width="20" height="20" patternUnits="userSpaceOnUse">
          <path d="M 20 0 L 0 0 0 20" fill="none" stroke={c.line} strokeWidth="0.4" opacity="0.4" />
        </pattern>
        <radialGradient id="vl-mapglow" cx="50%" cy="50%" r="60%">
          <stop offset="0%" stopColor={c.tealBg} stopOpacity="0.6" />
          <stop offset="100%" stopColor={c.tealBg} stopOpacity="0" />
        </radialGradient>
      </defs>
      <rect width={W} height="320" fill="url(#vl-grid)" />

      {/* Country outline — abstract topographic shape */}
      <path d={RO_OUTLINE} fill={c.bgSunk} stroke={c.line} strokeWidth="1" />
      <path d={RO_OUTLINE} fill="url(#vl-mapglow)" />

      {/* Inner contour lines for texture */}
      <path
        d="M120,150 C160,140 200,138 240,142 C280,148 310,160 330,180"
        fill="none" stroke={c.line} strokeWidth="0.6" opacity="0.5"
      />
      <path
        d="M100,180 C150,178 200,180 250,190 C290,198 320,212 340,228"
        fill="none" stroke={c.line} strokeWidth="0.6" opacity="0.4"
      />

      {/* Routes */}
      {routes.map((r) => {
        const dx = r.b.x - r.a.x;
        const dy = r.b.y - r.a.y;
        const mx = (r.a.x + r.b.x) / 2 + dy * 0.08;
        const my = (r.a.y + r.b.y) / 2 - dx * 0.08;
        const path = `M${r.a.x},${r.a.y} Q${mx},${my} ${r.b.x},${r.b.y}`;
        return (
          <g key={r.transport.id}>
            <path
              d={path}
              fill="none"
              stroke={r.accent}
              strokeWidth={selectedId === r.transport.id ? 2.5 : 1.5}
              strokeLinecap="round"
              strokeDasharray={r.live ? '0' : '3 3'}
              opacity={r.live ? 0.8 : 0.4}
            />
            {/* Origin */}
            <circle cx={r.a.x} cy={r.a.y} r="3" fill={r.accent} />
            <circle cx={r.a.x} cy={r.a.y} r="6" fill="none" stroke={r.accent} strokeWidth="1" opacity="0.4" />
            {/* Destination */}
            <circle cx={r.b.x} cy={r.b.y} r="3" fill={c.bgPanel} stroke={r.accent} strokeWidth="1.5" />
            {/* Vehicle position */}
            {r.live && (
              <g
                style={{ cursor: onSelectTransport ? 'pointer' : 'default' }}
                onClick={() => onSelectTransport?.(r.transport.id)}
              >
                <circle cx={r.pos.x} cy={r.pos.y} r="14" fill={r.accent} opacity="0.15">
                  <animate attributeName="r" from="8" to="18" dur="2s" repeatCount="indefinite" />
                  <animate attributeName="opacity" from="0.3" to="0" dur="2s" repeatCount="indefinite" />
                </circle>
                <circle cx={r.pos.x} cy={r.pos.y} r="6" fill={r.accent} stroke={c.bgPanel} strokeWidth="2" />
              </g>
            )}
          </g>
        );
      })}

      {/* Cities */}
      {cityList.map((city) => {
        const used = transports.some(
          (t) => t.origin.short === city.name || t.destination.short === city.name,
        );
        return (
          <g key={city.name} opacity={used ? 1 : 0.55}>
            {!used && <circle cx={city.x} cy={city.y} r="1.8" fill={c.inkFaint} />}
            {showLabels && (
              <text
                x={city.x + 8}
                y={city.y + 3}
                fontSize="9"
                fontFamily="Inter, sans-serif"
                fontWeight={used ? 600 : 400}
                fill={used ? c.inkSoft : c.inkFaint}
              >
                {city.name}
              </text>
            )}
          </g>
        );
      })}
    </svg>
  );
}

Object.assign(window, { RomaniaMap, CITY_POS });
