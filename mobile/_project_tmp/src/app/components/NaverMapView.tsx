/**
 * NaverMapView.tsx — Leaflet + OpenStreetMap based map
 * Replaces Naver Maps due to NCP auth issues on Figma Sites.
 * Uses CartoDB Voyager tiles for a clean, modern look.
 * Same export interface as the original Naver-based component.
 */
import { useEffect, useRef, useState, useCallback } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import {
  Footprints, Car, TrainFrontTunnel, MapPin, Navigation2,
} from "lucide-react";

/* ── Types ── */
interface LatLng { lat: number; lng: number }

interface NaverMapViewProps {
  destination: LatLng & { name: string };
  userPosition: LatLng | null;
  transportMode: string;
  transitRouteId?: string;
  progress: number;
  mapRatio: number;
}

/* ── Fallback waypoints ── */
const FALLBACK_ROUTES: Record<string, Record<string, LatLng[]>> = {
  dm3: {
    walk: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5665, lng: 126.9810 },
      { lat: 37.5672, lng: 126.9850 }, { lat: 37.5680, lng: 126.9900 },
      { lat: 37.5690, lng: 126.9940 }, { lat: 37.5695, lng: 126.9970 },
      { lat: 37.5701, lng: 126.9990 },
    ],
    drive: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5660, lng: 126.9850 },
      { lat: 37.5660, lng: 126.9920 }, { lat: 37.5680, lng: 126.9950 },
      { lat: 37.5701, lng: 126.9990 },
    ],
    transit: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5663, lng: 126.9820 },
      { lat: 37.5670, lng: 126.9870 }, { lat: 37.5685, lng: 126.9930 },
      { lat: 37.5695, lng: 126.9960 }, { lat: 37.5701, lng: 126.9990 },
    ],
  },
  dm4: {
    walk: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5680, lng: 126.9790 },
      { lat: 37.5720, lng: 126.9800 }, { lat: 37.5760, lng: 126.9810 },
      { lat: 37.5800, lng: 126.9820 }, { lat: 37.5826, lng: 126.9831 },
    ],
    drive: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5700, lng: 126.9790 },
      { lat: 37.5750, lng: 126.9800 }, { lat: 37.5790, lng: 126.9815 },
      { lat: 37.5826, lng: 126.9831 },
    ],
    transit: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5690, lng: 126.9795 },
      { lat: 37.5730, lng: 126.9805 }, { lat: 37.5770, lng: 126.9815 },
      { lat: 37.5826, lng: 126.9831 },
    ],
  },
  dm5: {
    walk: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5640, lng: 126.9800 },
      { lat: 37.5610, lng: 126.9830 }, { lat: 37.5570, lng: 126.9860 },
      { lat: 37.5540, lng: 126.9875 }, { lat: 37.5512, lng: 126.9882 },
    ],
    drive: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5630, lng: 126.9810 },
      { lat: 37.5580, lng: 126.9850 }, { lat: 37.5540, lng: 126.9870 },
      { lat: 37.5512, lng: 126.9882 },
    ],
    transit: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5635, lng: 126.9810 },
      { lat: 37.5590, lng: 126.9845 }, { lat: 37.5550, lng: 126.9865 },
      { lat: 37.5512, lng: 126.9882 },
    ],
  },
  dm6: {
    walk: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5670, lng: 126.9810 },
      { lat: 37.5690, lng: 126.9850 }, { lat: 37.5710, lng: 126.9880 },
      { lat: 37.5725, lng: 126.9900 }, { lat: 37.5736, lng: 126.9920 },
    ],
    drive: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5680, lng: 126.9830 },
      { lat: 37.5710, lng: 126.9880 }, { lat: 37.5736, lng: 126.9920 },
    ],
    transit: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5675, lng: 126.9820 },
      { lat: 37.5700, lng: 126.9870 }, { lat: 37.5720, lng: 126.9900 },
      { lat: 37.5736, lng: 126.9920 },
    ],
  },
  dm7: {
    walk: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5665, lng: 126.9785 },
      { lat: 37.5675, lng: 126.9785 }, { lat: 37.5685, lng: 126.9785 },
      { lat: 37.5696, lng: 126.9785 },
    ],
    drive: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5670, lng: 126.9780 },
      { lat: 37.5685, lng: 126.9783 }, { lat: 37.5696, lng: 126.9785 },
    ],
    transit: [
      { lat: 37.5660, lng: 126.9784 }, { lat: 37.5668, lng: 126.9782 },
      { lat: 37.5680, lng: 126.9784 }, { lat: 37.5696, lng: 126.9785 },
    ],
  },
};

const TRANSIT_SEGMENT_COLORS: Record<string, string[]> = {
  "bus-transfer": ["#2563EB", "#16A34A"],
  "subway": ["#1D4ED8"],
  "bus-direct": ["#16A34A"],
};

const MODE_COLORS: Record<string, string> = {
  walk: "#5478FF",
  drive: "#E11D48",
  transit: "#7C3AED",
};

/* ── Custom icon factories ── */
function createDestIcon(name: string): L.DivIcon {
  return L.divIcon({
    className: "",
    iconSize: [32, 52],
    iconAnchor: [16, 52],
    html: `
      <div style="display:flex;flex-direction:column;align-items:center">
        <div style="position:relative">
          <svg width="32" height="42" viewBox="0 0 28 36" fill="none">
            <path d="M14 0C6.268 0 0 6.268 0 14c0 10.5 14 22 14 22s14-11.5 14-22C28 6.268 21.732 0 14 0z" fill="#FF5252"/>
            <circle cx="14" cy="13" r="5.5" fill="white"/>
          </svg>
          <div style="position:absolute;top:6px;left:50%;transform:translateX(-50%)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#FF5252" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
        </div>
        <div style="background:white;padding:2px 8px;margin-top:2px;border-radius:6px;font-size:11px;font-weight:700;color:#1A1A2E;box-shadow:0 2px 8px rgba(0,0,0,0.15);white-space:nowrap;font-family:Pretendard,sans-serif;border:1px solid #f0f0f0">
          ${name}
        </div>
      </div>
    `,
  });
}

function createUserIcon(mode: string, color: string): L.DivIcon {
  const modeIconSvg = mode === "drive"
    ? `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m3 6 3-3h12l3 3"/><path d="M3 18V8a1 1 0 0 1 1-1h16a1 1 0 0 1 1 1v10"/><path d="M3 18h18"/><circle cx="7" cy="15" r="2"/><circle cx="17" cy="15" r="2"/></svg>`
    : mode === "transit"
    ? `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 17h20"/><path d="M6 17v4"/><path d="M18 17v4"/><path d="M6 3v14"/><path d="M18 3v14"/><path d="M10 3h4"/><path d="M6 8h12"/></svg>`
    : `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 16v-2.38C4 11.5 2.97 10.5 3 8c.03-2.72 1.49-6 5-6 3.51 0 4.97 3.28 5 6 .02 2.5-1 3.5-1 5.62V16"/><path d="M8 2c0 0-1 1-1 3.5S8 10 8 10"/><path d="M4 16h8"/><path d="M12 16v4h-1.5l-1-2.5L8 20H4v-4"/></svg>`;

  return L.divIcon({
    className: "",
    iconSize: [36, 36],
    iconAnchor: [18, 18],
    html: `
      <div style="position:relative;display:flex;align-items:center;justify-content:center">
        <div style="width:36px;height:36px;border-radius:50%;background:${color};display:flex;align-items:center;justify-content:center;border:3px solid white;box-shadow:0 2px 12px rgba(0,0,0,0.3)">
          ${modeIconSvg}
        </div>
        <div style="position:absolute;width:36px;height:36px;border-radius:50%;background:${color};opacity:0.3;animation:leafletMapPulse 2s ease-out infinite"></div>
      </div>
    `,
  });
}

function createTransferIcon(): L.DivIcon {
  return L.divIcon({
    className: "",
    iconSize: [18, 30],
    iconAnchor: [9, 9],
    html: `
      <div style="display:flex;flex-direction:column;align-items:center">
        <div style="width:18px;height:18px;border-radius:50%;background:#FEF3C7;border:2px solid #F59E0B;display:flex;align-items:center;justify-content:center">
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#D97706" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8L22 12L18 16"/><path d="M2 12H22"/>
          </svg>
        </div>
        <div style="background:#FEF3C7;border:1px solid #F59E0B;border-radius:4px;padding:1px 6px;margin-top:2px;font-size:9px;font-weight:700;color:#D97706;font-family:Pretendard,sans-serif">Transfer</div>
      </div>
    `,
  });
}

/* ═══════════════════════════════
   MAIN COMPONENT
   ═══════════════════════════════ */
export function NaverMapView({
  destination,
  userPosition,
  transportMode,
  transitRouteId,
  progress,
  mapRatio,
}: NaverMapViewProps) {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const userMarkerRef = useRef<L.Marker | null>(null);
  const destMarkerRef = useRef<L.Marker | null>(null);
  const routeLayerRef = useRef<L.LayerGroup | null>(null);
  const [mapReady, setMapReady] = useState(false);

  const mode = transportMode || "walk";
  const modeColor = MODE_COLORS[mode] || "#5478FF";

  /* ── Initialize map ── */
  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) return;

    const center: L.LatLngExpression = userPosition
      ? [userPosition.lat, userPosition.lng]
      : [destination.lat, destination.lng];

    const map = L.map(mapContainerRef.current, {
      center,
      zoom: 15,
      zoomControl: false,
      attributionControl: false,
    });

    // CartoDB Voyager — clean, modern, label-friendly tiles
    L.tileLayer(
      "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png",
      {
        maxZoom: 19,
        subdomains: "abcd",
      }
    ).addTo(map);

    // Small attribution at bottom-left
    L.control.attribution({ position: "bottomleft", prefix: false })
      .addAttribution('&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OSM</a> &copy; <a href="https://carto.com/" target="_blank">CARTO</a>')
      .addTo(map);

    // Route layer group
    routeLayerRef.current = L.layerGroup().addTo(map);

    // Destination marker
    destMarkerRef.current = L.marker(
      [destination.lat, destination.lng],
      { icon: createDestIcon(destination.name), zIndexOffset: 100 }
    ).addTo(map);

    mapRef.current = map;
    setMapReady(true);

    return () => {
      map.remove();
      mapRef.current = null;
      userMarkerRef.current = null;
      destMarkerRef.current = null;
      routeLayerRef.current = null;
      setMapReady(false);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /* ── Update user position ── */
  const updateUserMarker = useCallback((pos: LatLng) => {
    if (!mapRef.current) return;
    const icon = createUserIcon(mode, modeColor);
    if (userMarkerRef.current) {
      userMarkerRef.current.setLatLng([pos.lat, pos.lng]);
      userMarkerRef.current.setIcon(icon);
    } else {
      userMarkerRef.current = L.marker(
        [pos.lat, pos.lng],
        { icon, zIndexOffset: 200 }
      ).addTo(mapRef.current);
    }
  }, [mode, modeColor]);

  useEffect(() => {
    if (!mapReady || !userPosition) return;
    updateUserMarker(userPosition);
  }, [userPosition, mapReady, updateUserMarker]);

  /* ── Draw route ── */
  useEffect(() => {
    if (!mapReady || !mapRef.current || !routeLayerRef.current) return;

    const map = mapRef.current;
    const routeLayer = routeLayerRef.current;
    routeLayer.clearLayers();

    // Find route points
    let routePoints: LatLng[] | null = null;
    const placeId = Object.entries(FALLBACK_ROUTES).find(
      ([, routes]) => routes.walk?.some(p =>
        Math.abs(p.lat - destination.lat) < 0.001 && Math.abs(p.lng - destination.lng) < 0.001
      )
    )?.[0];
    if (placeId && FALLBACK_ROUTES[placeId]) {
      const modeKey = mode === "transit" ? "transit" : mode === "drive" ? "drive" : "walk";
      routePoints = FALLBACK_ROUTES[placeId][modeKey] || FALLBACK_ROUTES[placeId].walk;
    }

    if (routePoints && userPosition) {
      const first = routePoints[0];
      if (Math.abs(first.lat - userPosition.lat) + Math.abs(first.lng - userPosition.lng) > 0.0001) {
        routePoints = [userPosition, ...routePoints];
      }
    }

    if (!routePoints || routePoints.length < 2) {
      routePoints = [userPosition || { lat: 37.5660, lng: 126.9784 }, destination];
    }

    const latLngs: L.LatLngExpression[] = routePoints.map(p => [p.lat, p.lng] as L.LatLngExpression);

    if (mode === "transit" && transitRouteId) {
      // Multi-segment transit route
      const segColors = TRANSIT_SEGMENT_COLORS[transitRouteId] || ["#7C3AED"];
      const total = latLngs.length;
      const segSize = Math.ceil(total / segColors.length);

      segColors.forEach((color, i) => {
        const s = i * segSize;
        const e = Math.min((i + 1) * segSize + 1, total);
        const seg = latLngs.slice(s, e);
        if (seg.length < 2) return;

        // Glow
        L.polyline(seg, {
          color, opacity: 0.15, weight: 8,
          lineCap: "round", lineJoin: "round",
        }).addTo(routeLayer);
        // Main line
        L.polyline(seg, {
          color, opacity: 0.85, weight: 5,
          lineCap: "round", lineJoin: "round",
        }).addTo(routeLayer);

        // Transfer marker
        if (i < segColors.length - 1) {
          const tp = seg[seg.length - 1];
          L.marker(tp, { icon: createTransferIcon(), zIndexOffset: 150 }).addTo(routeLayer);
        }
      });
    } else {
      const isWalk = mode === "walk";
      const weight = mode === "drive" ? 7 : 5;

      // Glow
      L.polyline(latLngs, {
        color: modeColor, opacity: 0.12, weight: weight + 4,
        lineCap: "round", lineJoin: "round",
      }).addTo(routeLayer);

      // Main line
      L.polyline(latLngs, {
        color: modeColor,
        opacity: isWalk ? 0.8 : 0.7,
        weight,
        dashArray: isWalk ? "8 10" : undefined,
        lineCap: "round", lineJoin: "round",
      }).addTo(routeLayer);
    }

    // Fit bounds
    const allPoints = routePoints;
    const bounds = L.latLngBounds(
      allPoints.map(p => [p.lat, p.lng] as L.LatLngExpression)
    );
    map.fitBounds(bounds.pad(0.15), { paddingTopLeft: [20, 80], paddingBottomRight: [20, 20] });

  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mapReady, mode, transitRouteId]);

  /* ── Resize on mapRatio change ── */
  useEffect(() => {
    if (!mapRef.current) return;
    const timer = setTimeout(() => { mapRef.current?.invalidateSize(); }, 350);
    return () => clearTimeout(timer);
  }, [mapRatio]);

  /* ═══════════════════════════════
     RENDER
     ═══════════════════════════════ */
  return (
    <div className="absolute inset-0 overflow-hidden">
      <div
        ref={mapContainerRef}
        className="w-full h-full"
        style={{ minHeight: "100%" }}
      />

      {/* Mode badge (top-left) */}
      {mapReady && (
        <div className="absolute z-[1000] top-16 left-4">
          <div className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-white/90 backdrop-blur-sm shadow-sm border border-gray-100">
            {mode === "walk" && <Footprints size={12} color="#5478FF" strokeWidth={2.5} />}
            {mode === "drive" && <Car size={12} color="#E11D48" strokeWidth={2.5} />}
            {mode === "transit" && <TrainFrontTunnel size={12} color="#7C3AED" strokeWidth={2.5} />}
            <span className="text-[10px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>
              {mode === "walk" ? "Walking" : mode === "drive" ? "Driving" : "Transit"}
            </span>
          </div>
        </div>
      )}

      {/* Re-center button */}
      {mapReady && userPosition && (
        <div className="absolute z-[1000] bottom-4 right-4">
          <button
            onClick={() => {
              if (mapRef.current && userPosition) {
                mapRef.current.panTo([userPosition.lat, userPosition.lng], { animate: true, duration: 0.3 });
              }
            }}
            className="w-10 h-10 rounded-full bg-white shadow-lg border border-gray-100 flex items-center justify-center active:scale-95 transition-transform"
          >
            <Navigation2 size={18} color={modeColor} strokeWidth={2} />
          </button>
        </div>
      )}

      <style>{`
        @keyframes leafletMapPulse {
          0% { transform: scale(1); opacity: 0.3; }
          100% { transform: scale(2.5); opacity: 0; }
        }
        .leaflet-container {
          font-family: 'Pretendard', system-ui, sans-serif;
          background: #EDF2F7;
        }
        .leaflet-control-attribution {
          font-size: 9px !important;
          background: rgba(255,255,255,0.7) !important;
          padding: 2px 6px !important;
          border-radius: 4px !important;
          margin: 4px !important;
        }
        .leaflet-control-attribution a {
          color: #8892A4 !important;
        }
      `}</style>
    </div>
  );
}
