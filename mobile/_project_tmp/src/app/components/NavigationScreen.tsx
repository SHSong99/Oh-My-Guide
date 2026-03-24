import { useState, useEffect, useRef, useCallback } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import { StoryModeScreen } from "./StoryModeScreen";
import { NaverMapView } from "./NaverMapView";
import {
  Headphones, Camera, Coins, MessageCircle, SkipForward,
  CircleCheckBig, Map, Home, Clock, Ticket, Ruler,
  Coffee, UtensilsCrossed, Beer, Cookie, CableCar,
  Eye, Lightbulb, MessageSquare, MapPin, Sunset, Flower2, Aperture,
  Navigation, PartyPopper, Waves, Footprints, CakeSlice, X,
  Car, TrainFrontTunnel
} from "lucide-react";

/* ── Action icon mapping ── */
const ACTION_ICONS: Record<string, { icon: React.ElementType; bg: string; fg: string }> = {
  listen:  { icon: Headphones,     bg: "#EDE9FE", fg: "#7C3AED" },
  photo:   { icon: Camera,         bg: "#FFF1E6", fg: "#EA580C" },
  prices:  { icon: Coins,          bg: "#FEF9C3", fg: "#CA8A04" },
  phrases: { icon: MessageCircle,  bg: "#DBEAFE", fg: "#2563EB" },
  skip:    { icon: SkipForward,    bg: "#F0FDF4", fg: "#16A34A" },
  arrived: { icon: CircleCheckBig, bg: "#DCFCE7", fg: "#15803D" },
  showmap: { icon: Map,            bg: "#E0F2FE", fg: "#0284C7" },
  home:    { icon: Home,           bg: "#FCE7F3", fg: "#DB2777" },
};

function ActionIcon({ action }: { action: string }) {
  const cfg = ACTION_ICONS[action];
  if (!cfg) return null;
  const Icon = cfg.icon;
  return (
    <span className="inline-flex items-center justify-center w-[22px] h-[22px] rounded-[7px] flex-shrink-0" style={{ background: cfg.bg }}>
      <Icon size={13} color={cfg.fg} strokeWidth={2.5} fill={cfg.fg} fillOpacity={0.15} />
    </span>
  );
}

/* ── Info / Price icon mapping ── */
const INFO_ICONS: Record<string, { icon: React.ElementType; bg: string; fg: string }> = {
  "Hours":  { icon: Clock,  bg: "#EDE9FE", fg: "#7C3AED" },
  "Entry":  { icon: Ticket, bg: "#DCFCE7", fg: "#16A34A" },
  "Length": { icon: Ruler,  bg: "#DBEAFE", fg: "#2563EB" },
};

const PRICE_ICONS: Record<string, { icon: React.ElementType; bg: string; fg: string }> = {
  "Americano":        { icon: Coffee,           bg: "#FEF3C7", fg: "#92400E" },
  "Kalguksu":         { icon: UtensilsCrossed,  bg: "#FFE4E6", fg: "#E11D48" },
  "Bindaetteok":      { icon: CakeSlice,        bg: "#FFF7ED", fg: "#EA580C" },
  "Draft Beer":       { icon: Beer,             bg: "#FEF9C3", fg: "#CA8A04" },
  "Hotteok":          { icon: Cookie,           bg: "#FCE7F3", fg: "#DB2777" },
  "Namsan Cable Car": { icon: CableCar,         bg: "#E0F2FE", fg: "#0284C7" },
};

interface NavigationScreenProps {
  placeId: string;
  transportMode?: string;
  transitRouteId?: string;
  onBack: () => void;
  onComplete: () => void;
  onMinimize?: () => void;
  onProgressChange?: (progress: number) => void;
}

type MsgType = {
  id: string;
  sender: "bot" | "user" | "system";
  text?: string;
  isTyping?: boolean;
  type?: "audio-guide" | "photo-poi" | "action-buttons" | "place-intro" | "photo-spots" | "poi-hero" | "info-grid" | "price-guide" | "nearby-phrases" | "poi-confirm";
  poiData?: any;
};

const delay = (ms: number) => new Promise(res => setTimeout(res, ms));

const PLACE_NAMES: Record<string, { name: string; nameKr: string; lat: number; lng: number }> = {
  dm3: { name: "Gwangjang Market", nameKr: "광장시장", lat: 37.5701, lng: 126.9990 },
  dm4: { name: "Bukchon Hanok Village", nameKr: "북촌한옥마을", lat: 37.5826, lng: 126.9831 },
  dm5: { name: "Namsan Tower", nameKr: "남산타워", lat: 37.5512, lng: 126.9882 },
  dm6: { name: "Ikseon-dong", nameKr: "익선동", lat: 37.5736, lng: 126.9920 },
  dm7: { name: "Cheonggyecheon Stream", nameKr: "청계천", lat: 37.5696, lng: 126.9785 },
};

/* ── GPS Arrival Detection ── */
const ARRIVAL_THRESHOLD_METERS = 100; // Auto-arrive when within 100m
const GPS_UPDATE_INTERVAL_MS = 5000;

function haversineDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371000; // Earth radius in meters
  const toRad = (deg: number) => (deg * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

/* ── Snap breakpoints ── */
const SNAP_MAP = 78;
const SNAP_SPLIT = 38;
const SNAP_CHAT = 8;
function clamp(v: number, min: number, max: number) { return Math.min(Math.max(v, min), max); }
function nearestSnap(pct: number): number {
  const snaps = [SNAP_CHAT, SNAP_SPLIT, SNAP_MAP];
  let best = snaps[0], bestDist = Math.abs(pct - snaps[0]);
  for (const s of snaps) { const d = Math.abs(pct - s); if (d < bestDist) { best = s; bestDist = d; } }
  return best;
}

/* ══════════════════════════════════════
   RICH CONTENT BLOCKS (outside bubbles)
   ══════════════════════════════════════ */

function POIHeroCard({ data }: { data: any }) {
  return (
    <div className="w-full mt-2 mb-3 rounded-[20px] overflow-hidden shadow-[0_8px_30px_rgba(0,0,0,0.12)] animate-fadeUp">
      <div className="relative w-full aspect-[16/10]">
        <img src={data.image} className="w-full h-full object-cover" alt={data.title} />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
        {/* Tags */}
        <div className="absolute top-3 left-3 flex items-center gap-2">
          <span className="bg-[#FFDE42] text-[#1A1A2E] px-2.5 py-1 rounded-lg text-[10px] shadow-sm flex items-center gap-1" style={{ fontWeight: 800 }}>
            <Eye size={11} strokeWidth={2.5} /> nearby
          </span>
          <span className="bg-white/20 backdrop-blur-md text-white px-2.5 py-1 rounded-lg text-[10px]" style={{ fontWeight: 700 }}>
            #{data.tag}
          </span>
        </div>
        {/* Rating */}
        <div className="absolute top-3 right-3 bg-black/50 backdrop-blur-sm text-white px-2.5 py-1 rounded-lg text-[11px] flex items-center gap-1" style={{ fontWeight: 700 }}>
          <span className="text-[#FFDE42]">★</span> {data.rating}
        </div>
        {/* Title overlay */}
        <div className="absolute bottom-0 left-0 right-0 p-4">
          <div className="flex items-end justify-between">
            <div>
              <h2 className="text-[24px] text-white drop-shadow-lg" style={{ fontFamily: "'Pretendard'", fontWeight: 800, lineHeight: 1.15 }}>
                {data.title}
              </h2>
              <p className="text-[13px] text-white/80 mt-1" style={{ fontWeight: 500 }}>{data.titleKr}</p>
            </div>
            <div className="flex items-center gap-1 bg-white/20 backdrop-blur-sm px-2.5 py-1.5 rounded-lg">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
              <span className="text-[11px] text-white" style={{ fontWeight: 700 }}>{data.distance}</span>
            </div>
          </div>
        </div>
      </div>
      {/* Description */}
      <div className="bg-white px-4 py-3.5">
        <p className="text-[13px] text-[#4A5568] leading-[1.7]" style={{ fontWeight: 500 }}>{data.desc}</p>
      </div>
    </div>
  );
}

function InfoGridBlock({ data }: { data: any }) {
  return (
    <div className="w-full grid grid-cols-3 gap-2 my-2 animate-fadeUp">
      {data.items.map((item: any, i: number) => {
        const cfg = INFO_ICONS[item.label] || { icon: Clock, bg: "#F3F4F6", fg: "#6B7280" };
        const Icon = cfg.icon;
        return (
          <div key={i} className="bg-[#F7F9FC] rounded-[14px] p-3 flex flex-col items-center text-center border border-gray-100">
            <span className="w-8 h-8 rounded-[10px] flex items-center justify-center mb-1.5" style={{ background: cfg.bg }}>
              <Icon size={16} color={cfg.fg} strokeWidth={2.5} fill={cfg.fg} fillOpacity={0.15} />
            </span>
            <span className="text-[10px] text-[#8892A4] uppercase tracking-wider mb-1" style={{ fontWeight: 600 }}>{item.label}</span>
            <span className="text-[13px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>{item.value}</span>
          </div>
        );
      })}
    </div>
  );
}

function PriceGuideBlock({ data }: { data: any }) {
  return (
    <div className="w-full my-3 animate-fadeUp">
      {/* Section title — large, bold, standalone */}
      <div className="mb-4">
        <h3 className="text-[20px] text-[#1A1A2E]" style={{ fontFamily: "'Pretendard'", fontWeight: 800, lineHeight: 1.2 }}>
          {data.title}
        </h3>
        <p className="text-[12px] text-[#8892A4] mt-1" style={{ fontWeight: 500 }}>{data.subtitle}</p>
      </div>

      {/* Price items — horizontal scroll cards */}
      <div className="flex gap-2.5 overflow-x-auto hide-scrollbar snap-x snap-mandatory pb-2 -mx-1 px-1">
        {data.items.map((item: any, i: number) => (
          <div
            key={i}
            className="flex-shrink-0 w-[120px] snap-start rounded-[16px] bg-gradient-to-b from-[#F7F9FC] to-white border border-gray-100 p-3.5 flex flex-col items-center text-center shadow-sm"
          >
            {(() => {
              const cfg = PRICE_ICONS[item.name] || { icon: Coins, bg: "#F3F4F6", fg: "#6B7280" };
              const Icon = cfg.icon;
              return (
                <span className="w-11 h-11 rounded-[13px] flex items-center justify-center mb-2" style={{ background: cfg.bg }}>
                  <Icon size={22} color={cfg.fg} strokeWidth={2} fill={cfg.fg} fillOpacity={0.15} />
                </span>
              );
            })()}
            <span className="text-[13px] text-[#1A1A2E] mb-0.5" style={{ fontWeight: 700 }}>{item.name}</span>
            {item.nameKr && (
              <span className="text-[10px] text-[#8892A4] mb-2" style={{ fontWeight: 500 }}>{item.nameKr}</span>
            )}
            <span className="text-[16px] text-[#5478FF]" style={{ fontWeight: 800 }}>{item.price}</span>
          </div>
        ))}
      </div>

      {/* Tip — full width callout */}
      {data.tip && (
        <div className="mt-3 bg-[#FFF9E6] rounded-2xl px-4 py-3 flex items-start gap-2.5">
          <span className="w-7 h-7 rounded-[8px] flex items-center justify-center flex-shrink-0 mt-0.5" style={{ background: "#FEF3C7" }}>
            <Lightbulb size={14} color="#CA8A04" strokeWidth={2.5} fill="#CA8A04" fillOpacity={0.2} />
          </span>
          <span className="text-[12px] text-[#8B7300] leading-[1.7]" style={{ fontWeight: 600 }}>{data.tip}</span>
        </div>
      )}
    </div>
  );
}

function NearbyPhrasesBlock({ data }: { data: any }) {
  return (
    <div className="w-full my-2 animate-fadeUp">
      <div className="flex items-center gap-2 mb-3">
        <span className="w-6 h-6 rounded-[7px] flex items-center justify-center" style={{ background: "#DBEAFE" }}>
          <MessageCircle size={13} color="#2563EB" strokeWidth={2.5} fill="#2563EB" fillOpacity={0.15} />
        </span>
        <h3 className="text-[14px] text-[#1A1A2E]" style={{ fontWeight: 800 }}>{data.title}</h3>
      </div>
      <div className="flex gap-2.5 overflow-x-auto hide-scrollbar snap-x snap-mandatory pb-1 -mx-1 px-1">
        {data.phrases.map((p: any, i: number) => (
          <div key={i} className="flex-shrink-0 w-[155px] snap-start bg-gradient-to-br from-[#F7F9FC] to-white rounded-[16px] p-3.5 border border-gray-100 shadow-sm">
            <div className="text-[18px] text-[#1A1A2E] mb-1" style={{ fontWeight: 800 }}>{p.ko}</div>
            <div className="text-[10px] text-[#5478FF] bg-[#F0F4FF] px-2 py-0.5 rounded-md inline-block mb-2" style={{ fontWeight: 700 }}>
              {p.pronunciation}
            </div>
            <div className="text-[12px] text-[#4A5568]" style={{ fontWeight: 600 }}>{p.en}</div>
            {p.context && (
              <div className="text-[10px] text-[#8892A4] mt-1.5 italic flex items-center gap-1" style={{ fontWeight: 500 }}>
                <MessageSquare size={9} color="#8892A4" strokeWidth={2.5} fill="#8892A4" fillOpacity={0.15} />
                {p.context}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

function PhotoSpotsBlock({ data }: { data: any }) {
  return (
    <div className="w-full my-2 animate-fadeUp">
      <div className="flex items-center gap-2 mb-3">
        <span className="w-6 h-6 rounded-[7px] flex items-center justify-center" style={{ background: "#FFF1E6" }}>
          <Camera size={13} color="#EA580C" strokeWidth={2.5} fill="#EA580C" fillOpacity={0.15} />
        </span>
        <h3 className="text-[14px] text-[#1A1A2E]" style={{ fontWeight: 800 }}>Best Photo Spots</h3>
      </div>
      <div className="flex gap-2.5 overflow-x-auto hide-scrollbar snap-x snap-mandatory pb-1 -mx-1 px-1">
        {data.spots.map((spot: any, i: number) => (
          <div key={i} className="flex-shrink-0 w-[200px] snap-start rounded-[16px] overflow-hidden shadow-sm border border-gray-100 bg-white">
            <div className="relative w-full aspect-[4/3]">
              <img src={spot.image} className="w-full h-full object-cover" alt={spot.name} />
              <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
              <div className="absolute bottom-2.5 left-3 right-3">
                <div className="text-[13px] text-white drop-shadow-sm" style={{ fontWeight: 700 }}>{spot.name}</div>
              </div>
              <div className="absolute top-2.5 right-2.5 bg-black/40 backdrop-blur-sm text-white w-6 h-6 rounded-full flex items-center justify-center text-[11px]" style={{ fontWeight: 800 }}>
                {i + 1}
              </div>
            </div>
            <div className="px-3 py-2.5">
              <p className="text-[11px] text-[#4A5568] leading-[1.5]" style={{ fontWeight: 500 }}>{spot.desc}</p>
              <div className="text-[10px] text-[#5478FF] mt-1.5 bg-[#F0F4FF] px-2 py-1 rounded-md inline-flex items-center gap-1" style={{ fontWeight: 600 }}>
                {spot.tipIcon === "aperture" && <Aperture size={9} strokeWidth={2.5} />}
                {spot.tipIcon === "sunset" && <Sunset size={9} strokeWidth={2.5} />}
                {spot.tipIcon === "flower" && <Flower2 size={9} strokeWidth={2.5} />}
                {spot.tip}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

/* ══════════════════════════════════════
   MAP SVG — mode-aware rendering
   Walk: dashed blue pedestrian path
   Drive: solid wide dark route
   Transit: colored segments with station/transfer markers
   ══════════════════════════════════════ */

/* Transit route configs for map rendering */
const TRANSIT_MAP_ROUTES: Record<string, { segments: { color: string; label: string; path: string; stations: { x: number; y: number; name: string }[] }[]; transferPoints: { x: number; y: number; name: string }[] }> = {
  "bus-transfer": {
    segments: [
      { color: "#2563EB", label: "144", path: "M 130 370 L 130 310 L 160 310 L 160 230 L 200 230", stations: [{ x: 130, y: 340, name: "Euljiro 3-ga" }, { x: 160, y: 270, name: "Mugyo-dong" }, { x: 160, y: 230, name: "Jongno 3-ga" }] },
      { color: "#16A34A", label: "272", path: "M 200 230 L 260 230 L 300 200 L 300 90 L 310 90", stations: [{ x: 260, y: 230, name: "Jongno 4-ga" }, { x: 300, y: 140, name: "Jongno 5-ga" }] },
    ],
    transferPoints: [{ x: 200, y: 230, name: "Jongno 3-ga" }],
  },
  "subway": {
    segments: [
      { color: "#1D4ED8", label: "Line 1", path: "M 130 370 L 130 310 L 90 310 L 90 230 L 90 170 L 90 90 L 160 90 L 250 90 L 310 90", stations: [{ x: 130, y: 310, name: "Euljiro 1-ga" }, { x: 90, y: 230, name: "Jonggak" }, { x: 90, y: 170, name: "Jongno 3-ga" }, { x: 90, y: 90, name: "Jongno 5-ga" }] },
    ],
    transferPoints: [],
  },
  "bus-direct": {
    segments: [
      { color: "#16A34A", label: "272", path: "M 130 370 L 130 310 L 160 310 L 160 250 L 200 250 L 250 230 L 300 200 L 300 140 L 300 90 L 310 90", stations: [{ x: 130, y: 340, name: "Euljiro 3-ga" }, { x: 160, y: 280, name: "Mugyo-dong" }, { x: 200, y: 250, name: "Jongno 3-ga" }, { x: 300, y: 170, name: "Jongno 4-ga" }, { x: 300, y: 120, name: "Jongno 5-ga" }] },
    ],
    transferPoints: [],
  },
};

const WALK_PATH = "M 130 370 L 130 310 L 160 310 L 160 200 Q 180 190 200 195 Q 240 210 260 200 L 300 185 L 300 90 L 310 90";
const DRIVE_PATH = "M 130 370 L 130 310 L 200 310 L 300 310 L 300 230 L 300 170 L 300 90 L 310 90";

function MapCanvas({ mapRatio, progress, destinationName, transportMode, transitRouteId }: { mapRatio: number; progress: number; destinationName: string; transportMode: string; transitRouteId?: string }) {
  const isDetailed = mapRatio > 0.55;
  const mode = transportMode || "walk";
  const transitRoute = transitRouteId ? TRANSIT_MAP_ROUTES[transitRouteId] : TRANSIT_MAP_ROUTES["bus-transfer"];

  /* Color & style per mode */
  const modeColor = mode === "walk" ? "#5478FF" : mode === "drive" ? "#E11D48" : "#7C3AED";
  const routeWidth = mode === "drive" ? 7 : 5;

  return (
    <div className="absolute inset-0 overflow-hidden" style={{ background: "#EDF2F7" }}>
      <svg width="100%" height="100%" viewBox="0 0 400 500" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid slice">
        {/* ── Base map (shared) ── */}
        <path d="M -20 180 Q 80 160 160 200 Q 240 240 320 210 Q 380 190 440 220" fill="none" stroke="#B3D9F2" strokeWidth="18" strokeLinecap="round" />
        <path d="M -20 180 Q 80 160 160 200 Q 240 240 320 210 Q 380 190 440 220" fill="none" stroke="#9ECAE1" strokeWidth="10" strokeLinecap="round" />
        <ellipse cx="280" cy="120" rx="50" ry="30" fill="#C6E5B3" opacity="0.5" />
        <ellipse cx="100" cy="350" rx="35" ry="22" fill="#C6E5B3" opacity="0.4" />
        <ellipse cx="340" cy="380" rx="40" ry="25" fill="#C6E5B3" opacity="0.45" />
        {/* Buildings */}
        <rect x="30" y="30" width="70" height="55" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="120" y="20" width="55" height="65" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="195" y="30" width="80" height="50" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="300" y="25" width="60" height="60" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="20" y="110" width="60" height="50" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="100" y="100" width="45" height="60" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="170" y="95" width="70" height="55" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="30" y="240" width="80" height="55" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="130" y="250" width="60" height="50" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="210" y="240" width="75" height="60" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="310" y="250" width="55" height="50" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="40" y="380" width="50" height="60" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="120" y="390" width="70" height="50" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="220" y="375" width="65" height="55" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="310" y="340" width="55" height="40" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        {/* Roads */}
        <line x1="0" y1="90" x2="400" y2="90" stroke="#FFFFFF" strokeWidth="7" />
        <line x1="0" y1="310" x2="400" y2="310" stroke="#FFFFFF" strokeWidth="7" />
        <line x1="0" y1="170" x2="400" y2="170" stroke="#FFFFFF" strokeWidth="5" opacity="0.8" />
        <line x1="0" y1="450" x2="400" y2="450" stroke="#FFFFFF" strokeWidth="5" opacity="0.8" />
        <line x1="160" y1="0" x2="160" y2="500" stroke="#FFFFFF" strokeWidth="6" />
        <line x1="300" y1="0" x2="300" y2="500" stroke="#FFFFFF" strokeWidth="6" />
        <line x1="90" y1="0" x2="90" y2="500" stroke="#FFFFFF" strokeWidth="4" opacity="0.7" />
        <line x1="220" y1="0" x2="220" y2="500" stroke="#FFFFFF" strokeWidth="4" opacity="0.7" />
        <line x1="370" y1="0" x2="370" y2="500" stroke="#FFFFFF" strokeWidth="4" opacity="0.7" />
        <line x1="0" y1="230" x2="160" y2="230" stroke="#F7FAFC" strokeWidth="2.5" />
        <line x1="160" y1="140" x2="300" y2="140" stroke="#F7FAFC" strokeWidth="2.5" />
        <line x1="300" y1="370" x2="400" y2="370" stroke="#F7FAFC" strokeWidth="2.5" />

        {/* Street labels (detailed) */}
        {isDetailed && (
          <>
            <text x="200" y="85" textAnchor="middle" fill="#A0AEC0" fontSize="7" fontWeight="600" fontFamily="Pretendard, sans-serif">종로 Jongno-daero</text>
            <text x="200" y="305" textAnchor="middle" fill="#A0AEC0" fontSize="7" fontWeight="600" fontFamily="Pretendard, sans-serif">을지로 Eulji-ro</text>
            <text x="180" y="195" textAnchor="middle" fill="#7EB8DA" fontSize="6.5" fontWeight="600" fontFamily="Pretendard, sans-serif">청계천 Cheonggyecheon</text>
          </>
        )}

        {/* ══ WALK MODE ══ */}
        {mode === "walk" && (
          <>
            {/* Shadow path */}
            <path d={WALK_PATH} fill="none" stroke={modeColor} strokeWidth={routeWidth} strokeLinecap="round" strokeLinejoin="round" opacity="0.15" />
            {/* Animated dashed path */}
            <path d={WALK_PATH} fill="none" stroke={modeColor} strokeWidth={routeWidth} strokeDasharray="8 10" strokeLinecap="round" strokeLinejoin="round" opacity="0.8" style={{ animation: "dash 1.5s linear infinite" }} />
            {/* Progress fill */}
            <path d={WALK_PATH} fill="none" stroke={modeColor} strokeWidth={routeWidth} strokeLinecap="round" strokeLinejoin="round" opacity="0.7" strokeDasharray="600" strokeDashoffset={600 - (600 * progress / 100)} style={{ transition: "stroke-dashoffset 1.5s ease-out" }} />
            {isDetailed && (
              <>
                {/* Cheonggyecheon POI */}
                <circle cx="200" cy="210" r="6" fill="#4CAF50" stroke="white" strokeWidth="2" />
                <rect x="210" y="200" width="62" height="16" rx="4" fill="white" filter="url(#shadow)" />
                <text x="241" y="211" textAnchor="middle" fill="#1A1A2E" fontSize="6.5" fontWeight="700" fontFamily="Pretendard, sans-serif">청계천</text>
              </>
            )}
          </>
        )}

        {/* ══ DRIVE MODE ══ */}
        {mode === "drive" && (
          <>
            {/* Road outline */}
            <path d={DRIVE_PATH} fill="none" stroke="#1A1A2E" strokeWidth={routeWidth + 4} strokeLinecap="round" strokeLinejoin="round" opacity="0.08" />
            {/* Main road */}
            <path d={DRIVE_PATH} fill="none" stroke={modeColor} strokeWidth={routeWidth} strokeLinecap="round" strokeLinejoin="round" opacity="0.3" />
            {/* Progress fill */}
            <path d={DRIVE_PATH} fill="none" stroke={modeColor} strokeWidth={routeWidth} strokeLinecap="round" strokeLinejoin="round" strokeDasharray="550" strokeDashoffset={550 - (550 * progress / 100)} style={{ transition: "stroke-dashoffset 1.5s ease-out" }} />
            {/* Direction arrows along the route */}
            {isDetailed && (
              <>
                {/* Turn indicator: right turn at Euljiro intersection */}
                <g transform="translate(200, 310)">
                  <circle r="10" fill="white" filter="url(#shadow)" />
                  <path d="M -3 -3 L 3 0 L -3 3" fill="none" stroke={modeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" transform="rotate(-90)" />
                </g>
                {/* Speed info */}
                <rect x="280" y="260" width="45" height="16" rx="4" fill="white" filter="url(#shadow)" />
                <text x="302" y="271" textAnchor="middle" fill="#E11D48" fontSize="7" fontWeight="800" fontFamily="Pretendard, sans-serif">40km/h</text>
                {/* Traffic light */}
                <circle cx="300" cy="230" r="5" fill="#FFB800" stroke="white" strokeWidth="1.5" />
              </>
            )}
          </>
        )}

        {/* ══ TRANSIT MODE ══ */}
        {mode === "transit" && transitRoute && (
          <>
            {/* Draw each segment with its color */}
            {transitRoute.segments.map((seg, i) => (
              <g key={i}>
                {/* Segment shadow */}
                <path d={seg.path} fill="none" stroke={seg.color} strokeWidth="6" strokeLinecap="round" strokeLinejoin="round" opacity="0.12" />
                {/* Segment line */}
                <path d={seg.path} fill="none" stroke={seg.color} strokeWidth="4.5" strokeLinecap="round" strokeLinejoin="round" opacity="0.85" />
                {/* Station dots */}
                {isDetailed && seg.stations.map((st, si) => (
                  <g key={si}>
                    <circle cx={st.x} cy={st.y} r="5" fill="white" stroke={seg.color} strokeWidth="2" />
                    <rect x={st.x + 8} y={st.y - 8} width={st.name.length * 4.5 + 12} height="16" rx="4" fill="white" filter="url(#shadow)" />
                    <text x={st.x + 14} y={st.y + 3} fill="#1A1A2E" fontSize="6" fontWeight="600" fontFamily="Pretendard, sans-serif">{st.name}</text>
                  </g>
                ))}
                {/* Segment label badge */}
                {isDetailed && (() => {
                  const midStation = seg.stations[Math.floor(seg.stations.length / 2)];
                  return (
                    <g>
                      <rect x={midStation.x - 22} y={midStation.y - 26} width={seg.label.length * 5.5 + 14} height="14" rx="7" fill={seg.color} />
                      <text x={midStation.x - 22 + (seg.label.length * 5.5 + 14) / 2} y={midStation.y - 16} textAnchor="middle" fill="white" fontSize="7" fontWeight="700" fontFamily="Pretendard, sans-serif">{seg.label}</text>
                    </g>
                  );
                })()}
              </g>
            ))}
            {/* Transfer point markers */}
            {transitRoute.transferPoints.map((tp, i) => (
              <g key={`tp-${i}`}>
                <circle cx={tp.x} cy={tp.y} r="8" fill="#FEF3C7" stroke="#F59E0B" strokeWidth="2" />
                <path d={`M ${tp.x - 3} ${tp.y} L ${tp.x + 3} ${tp.y}`} stroke="#F59E0B" strokeWidth="2" strokeLinecap="round" />
                <path d={`M ${tp.x + 1} ${tp.y - 3} L ${tp.x + 3} ${tp.y} L ${tp.x + 1} ${tp.y + 3}`} stroke="#F59E0B" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                {isDetailed && (
                  <>
                    <rect x={tp.x - 25} y={tp.y + 12} width="50" height="14" rx="4" fill="#FEF3C7" stroke="#F59E0B" strokeWidth="0.8" />
                    <text x={tp.x} y={tp.y + 22} textAnchor="middle" fill="#D97706" fontSize="6.5" fontWeight="700" fontFamily="Pretendard, sans-serif">Transfer</text>
                  </>
                )}
              </g>
            ))}
            {/* Progress overlay on first segment */}
            {transitRoute.segments.length > 0 && (
              <path d={transitRoute.segments[0].path} fill="none" stroke="white" strokeWidth="4.5" strokeLinecap="round" strokeLinejoin="round" opacity="0.3" strokeDasharray="500" strokeDashoffset={500 - (500 * Math.min(progress, 50) / 50)} style={{ transition: "stroke-dashoffset 1.5s ease-out" }} />
            )}
          </>
        )}

        <defs>
          <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0" dy="1" stdDeviation="2" floodColor="#000" floodOpacity="0.1" />
          </filter>
        </defs>
      </svg>

      {/* ── User marker ── */}
      <div className="absolute z-20" style={{ top: "70%", left: "32%", transform: "translate(-50%, -50%)" }}>
        {/* Mode-specific marker icon */}
        <div className="relative">
          <div className="w-8 h-8 rounded-full flex items-center justify-center border-[3px] border-white shadow-[0_2px_12px_rgba(0,0,0,0.25)]" style={{ background: modeColor }}>
            {mode === "walk" && <Footprints size={14} color="white" strokeWidth={2.5} />}
            {mode === "drive" && <Car size={14} color="white" strokeWidth={2.5} />}
            {mode === "transit" && <TrainFrontTunnel size={14} color="white" strokeWidth={2.5} />}
          </div>
          <div className="absolute inset-0 rounded-full animate-ping opacity-30" style={{ background: modeColor }} />
        </div>
      </div>

      {/* ── Destination pin ── */}
      <div className="absolute z-20" style={{ top: "14%", left: "77%", transform: "translate(-50%, -50%)" }}>
        <div className="flex flex-col items-center">
          <div className="relative">
            <svg width="28" height="36" viewBox="0 0 28 36" fill="none">
              <path d="M14 0C6.268 0 0 6.268 0 14c0 10.5 14 22 14 22s14-11.5 14-22C28 6.268 21.732 0 14 0z" fill="#FF5252" />
              <circle cx="14" cy="13" r="5.5" fill="white" />
            </svg>
            <MapPin size={11} color="#FF5252" strokeWidth={2.5} className="absolute top-[7px] left-1/2 -translate-x-1/2" />
          </div>
          {isDetailed && (
            <div className="bg-white px-2 py-0.5 mt-0.5 rounded-md text-[9px] text-[#1A1A2E] shadow-md whitespace-nowrap border border-gray-100" style={{ fontWeight: 700 }}>
              {destinationName}
            </div>
          )}
        </div>
      </div>

      {/* ── Mode badge (top-left on map) ── */}
      <div className="absolute z-20 top-16 left-4">
        <div className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg bg-white/90 backdrop-blur-sm shadow-sm border border-gray-100">
          {mode === "walk" && <Footprints size={12} color="#5478FF" strokeWidth={2.5} />}
          {mode === "drive" && <Car size={12} color="#E11D48" strokeWidth={2.5} />}
          {mode === "transit" && <TrainFrontTunnel size={12} color="#7C3AED" strokeWidth={2.5} />}
          <span className="text-[10px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>
            {mode === "walk" ? "Walking" : mode === "drive" ? "Driving" : "Transit"}
          </span>
        </div>
      </div>
    </div>
  );
}


/* ══════════════════════════════════════
   MAIN COMPONENT
   ══════════════════════════════════════ */
export function NavigationScreen({ placeId, transportMode = "walk", transitRouteId, onBack, onComplete, onMinimize, onProgressChange }: NavigationScreenProps) {
  const destination = PLACE_NAMES[placeId] || { name: "Destination", nameKr: "목적지", lat: 37.5696, lng: 126.9785 };

  const containerRef = useRef<HTMLDivElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);
  const dragRef = useRef<{ startY: number; startPct: number; isDragging: boolean }>({ startY: 0, startPct: SNAP_SPLIT, isDragging: false });

  const [messages, setMessages] = useState<MsgType[]>([]);
  const [sheetPct, setSheetPct] = useState(SNAP_SPLIT);
  const [isSnapping, setIsSnapping] = useState(false);
  const [progress, setProgress] = useState(0);
  const [isProcessing, setIsProcessing] = useState(false);
  const [showStory, setShowStory] = useState(false);
  const [distanceToDestination, setDistanceToDestination] = useState<number | null>(null);
  const [gpsStatus, setGpsStatus] = useState<"watching" | "arrived" | "unavailable" | "denied">("watching");
  const [userPosition, setUserPosition] = useState<{ lat: number; lng: number } | null>(null);
  const hasAutoArrivedRef = useRef(false);

  /* ── GPS Auto-Arrival Detection ── */
  const triggerAutoArrival = useCallback(async () => {
    if (hasAutoArrivedRef.current) return;
    hasAutoArrivedRef.current = true;
    setGpsStatus("arrived");
    setProgress(100);

    // Remove any existing action buttons
    setMessages(p => p.filter(m => m.type !== "action-buttons"));

    // System notification
    setMessages(p => [...p, {
      id: `gps-arrival-${Date.now()}`,
      sender: "system",
      text: "You've arrived at the destination!"
    }]);

    await delay(800);
    setMessages(p => [...p, { id: `t-arr-${Date.now()}`, sender: "bot", isTyping: true }]);
    await delay(1500);
    setMessages(p => {
      const f = p.filter(m => !m.isTyping);
      return [...f, {
        id: `arr-${Date.now()}`,
        sender: "bot",
        text: `You made it to ${destination.name}! Great job getting here. Enjoy exploring this amazing spot!`
      }];
    });

    await delay(1000);
    setMessages(p => [...p, {
      id: `btn-arr-${Date.now()}`,
      sender: "bot",
      type: "action-buttons",
      poiData: { options: [
        { label: "Back to Home", action: "home" },
      ] }
    }]);
  }, [destination.name]);

  useEffect(() => {
    if (!navigator.geolocation) {
      setGpsStatus("unavailable");
      return;
    }

    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        const { latitude, longitude, accuracy } = position.coords;
        const dist = haversineDistance(latitude, longitude, destination.lat, destination.lng);

        setUserPosition({ lat: latitude, lng: longitude });
        setDistanceToDestination(Math.round(dist));

        // Dynamically update progress based on distance (max 2km start)
        const maxDist = 2000;
        const progressFromGps = Math.min(95, Math.max(5, ((maxDist - dist) / maxDist) * 95));
        if (!hasAutoArrivedRef.current) {
          setProgress(Math.round(progressFromGps));
        }

        // Check if arrived (within threshold, and accuracy is reasonable)
        if (dist <= ARRIVAL_THRESHOLD_METERS && accuracy < 200 && !hasAutoArrivedRef.current) {
          triggerAutoArrival();
        }
      },
      (error) => {
        if (error.code === error.PERMISSION_DENIED) {
          setGpsStatus("denied");
        } else {
          setGpsStatus("unavailable");
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: GPS_UPDATE_INTERVAL_MS,
      }
    );

    return () => navigator.geolocation.clearWatch(watchId);
  }, [destination.lat, destination.lng, triggerAutoArrival]);

  // Report progress to parent for FloatingNavButton
  useEffect(() => {
    onProgressChange?.(progress);
  }, [progress, onProgressChange]);

  const mapRatio = sheetPct / 100;
  const isChatMinimised = sheetPct >= SNAP_MAP - 5;

  useEffect(() => {
    if (scrollRef.current) {
      setTimeout(() => { scrollRef.current && (scrollRef.current.scrollTop = scrollRef.current.scrollHeight); }, 100);
    }
  }, [messages]);

  /* ── drag ── */
  const getContainerH = () => containerRef.current?.getBoundingClientRect().height ?? 1;
  const onDragStart = useCallback((y: number) => { dragRef.current = { startY: y, startPct: sheetPct, isDragging: true }; setIsSnapping(false); }, [sheetPct]);
  const onDragMove = useCallback((y: number) => { if (!dragRef.current.isDragging) return; const dPct = ((y - dragRef.current.startY) / getContainerH()) * 100; setSheetPct(clamp(dragRef.current.startPct + dPct, SNAP_CHAT - 2, SNAP_MAP + 2)); }, []);
  const onDragEnd = useCallback(() => { if (!dragRef.current.isDragging) return; dragRef.current.isDragging = false; setIsSnapping(true); setSheetPct(p => nearestSnap(p)); }, []);
  const handleTouchStart = useCallback((e: React.TouchEvent) => { onDragStart(e.touches[0].clientY); }, [onDragStart]);
  const handleTouchMove = useCallback((e: React.TouchEvent) => { onDragMove(e.touches[0].clientY); }, [onDragMove]);
  const handleTouchEnd = useCallback(() => { onDragEnd(); }, [onDragEnd]);
  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    e.preventDefault(); onDragStart(e.clientY);
    const onMove = (ev: MouseEvent) => onDragMove(ev.clientY);
    const onUp = () => { onDragEnd(); window.removeEventListener("mousemove", onMove); window.removeEventListener("mouseup", onUp); };
    window.addEventListener("mousemove", onMove); window.addEventListener("mouseup", onUp);
  }, [onDragStart, onDragMove, onDragEnd]);

  /* ── Story Mode close handler ── */
  const handleStoryClose = useCallback(() => {
    setShowStory(false);
    setProgress(p => Math.min(p + 15, 70));
    setMessages(p => [...p, { id: `st-${Date.now()}`, sender: "system", text: "Story completed" }]);
    setTimeout(() => {
      setMessages(p => [...p, { id: `btn-${Date.now()}`, sender: "bot", type: "action-buttons", poiData: { options: [
        { label: "Photo Spots", action: "photo" },
        { label: "Nearby Prices", action: "prices" },
        { label: "Korean Phrases", action: "phrases" },
        { label: "Keep Walking", action: "skip" },
      ] } }]);
    }, 500);
  }, []);

  /* ══════════════════════════════════════
     CHAT SEQUENCE — rich content flow
     ══════════════════════════════════════ */
  useEffect(() => {
    let alive = true;
    const run = async () => {
      const modeLabel = transportMode === "drive" ? "Driving" : transportMode === "transit" ? "Transit" : "Walking";
      const modeDuration = transportMode === "drive" ? "3 min" : transportMode === "transit" ? "12 min" : "15 min";
      setMessages([{ id: "s1", sender: "system", text: `${modeLabel} navigation started · ${modeDuration} to destination` }]);
      await delay(1000); if (!alive) return;

      setMessages(p => [...p, { id: "t1", sender: "bot", isTyping: true }]);
      await delay(1500); if (!alive) return;
      const modeMsg = transportMode === "drive"
        ? `Let's drive to ${destination.name}! I'll keep you posted on traffic.`
        : transportMode === "transit"
        ? `Let's take transit to ${destination.name}! I'll let you know when to get off.`
        : `Let's head to ${destination.name}! I'll guide you along the way.`;
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, { id: "b1", sender: "bot", text: modeMsg }]; });

      await delay(3000); if (!alive) return;
      setProgress(25);

      setMessages(p => [...p, { id: "t2", sender: "bot", isTyping: true }]);
      await delay(1200); if (!alive) return;
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, { id: "b2", sender: "bot", text: "Oh! Look to your right!" }]; });

      // ── POI confirmation (single bubble) ──
      await delay(1200); if (!alive) return;
      setMessages(p => [...p, { id: "t2b", sender: "bot", isTyping: true }]);
      await delay(1000); if (!alive) return;
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, {
        id: "poi-ask1", sender: "bot", type: "poi-confirm",
        text: "That's Cheonggyecheon Stream!\nWant a quick guide?",
      }]; });
    };
    run();
    return () => { alive = false; };
  }, [destination.name, transportMode]);

  /* ── POI confirm handler (Yes / No) ── */
  const handlePoiConfirm = async (accepted: boolean) => {
    if (isProcessing) return;
    setIsProcessing(true);
    setMessages(p => p.filter(m => m.type !== "poi-confirm"));
    setMessages(p => [...p, { id: `u-${Date.now()}`, sender: "user", text: accepted ? "Yes, guide me!" : "No thanks" }]);

    if (!accepted) {
      await delay(800);
      setMessages(p => [...p, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
      await delay(1000);
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, { id: `b-skip-${Date.now()}`, sender: "bot", text: "No problem! Let's keep walking." }]; });
      setIsProcessing(false);
      return;
    }

    // Show POI Hero Card
    await delay(800);
    setMessages(p => [...p, {
      id: "hero1", sender: "bot", type: "poi-hero",
      poiData: {
        title: "Cheonggyecheon Stream",
        titleKr: "\uCCAD\uACC4\uCC9C",
        tag: "Nature",
        rating: 4.8,
        distance: "Right here",
        image: "https://images.unsplash.com/photo-1631176206492-f03b80b3e854?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxDaGVvbmdneWVjaGVvbiUyMHN0cmVhbSUyMFNlb3VsJTIwc3RlcHBpbmclMjBzdG9uZXN8ZW58MXx8fHwxNzczMzg5NDAyfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
        desc: "A stunning 10.9km urban oasis flowing through the heart of Seoul. Once buried under an elevated highway, it was restored in 2005 and is now one of Seoul's most beloved public spaces."
      }
    }]);

    // Info Grid
    await delay(800);
    setMessages(p => [...p, {
      id: "info1", sender: "bot", type: "info-grid",
      poiData: {
        items: [
          { label: "Hours", value: "24/7" },
          { label: "Entry", value: "Free" },
          { label: "Length", value: "10.9km" },
        ]
      }
    }]);

    // Action buttons
    await delay(600);
    setMessages(p => [...p, {
      id: "btn1", sender: "bot", type: "action-buttons",
      poiData: { options: [
        { label: "Listen to Story", action: "listen" },
        { label: "Photo Spots", action: "photo" },
        { label: "Nearby Prices", action: "prices" },
        { label: "Korean Phrases", action: "phrases" },
        { label: "Keep Walking", action: "skip" },
      ]}
    }]);
    setIsProcessing(false);
  };

  /* ── action handler ── */
  const handleUserAction = async (action: string, label: string) => {
    if (isProcessing) return;
    setIsProcessing(true);
    setMessages(p => p.filter(m => m.type !== "action-buttons"));
    setMessages(p => [...p, { id: `u-${Date.now()}`, sender: "user", text: label }]);

    if (sheetPct > SNAP_SPLIT) { setIsSnapping(true); setSheetPct(SNAP_SPLIT); }

    if (action === "listen") {
      // Open immersive Story Mode
      setShowStory(true);
      setIsProcessing(false);
      return;
    }

    if (action === "photo") {
      await delay(800);
      setMessages(p => [...p, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
      await delay(1500);
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, {
        id: `ps-${Date.now()}`, sender: "bot", type: "photo-spots",
        poiData: { spots: [
          { name: "Stepping Stones", image: "https://images.unsplash.com/photo-1631176206492-f03b80b3e854?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxDaGVvbmdneWVjaGVvbiUyMHN0cmVhbSUyMFNlb3VsJTIwc3RlcHBpbmclMjBzdG9uZXN8ZW58MXx8fHwxNzczMzg5NDAyfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", desc: "Walk across the stream on flat stones", tip: "Low angle for reflections", tipIcon: "aperture" },
          { name: "Waterfall Wall", image: "https://images.unsplash.com/photo-1762732703913-cfbf690b0300?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMHdhdGVyZmFsbCUyMHVyYmFuJTIwc3RyZWFtJTIwbmlnaHR8ZW58MXx8fHwxNzczMzg5NDA2fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", desc: "The 2-story cascade at the stream's start", tip: "Best at sunset", tipIcon: "sunset" },
          { name: "Willow Bridge", image: "https://images.unsplash.com/photo-1707298409328-55d0c5fa9370?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMHdpbGxvdyUyMHRyZWUlMjBicmlkZ2UlMjBwYXJrfGVufDF8fHx8MTc3MzM4OTQwOXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", desc: "Arched footbridge draped in willows", tip: "Frame through branches", tipIcon: "flower" },
        ] }
      }]; });
      await delay(1000);
      setMessages(p => [...p, { id: `btn-${Date.now()}`, sender: "bot", type: "action-buttons", poiData: { options: [
        { label: "Listen to Story", action: "listen" },
        { label: "Nearby Prices", action: "prices" },
        { label: "Keep Walking", action: "skip" },
      ] } }]);
      setProgress(pr => Math.min(pr + 15, 70));
    }

    if (action === "prices") {
      await delay(800);
      setMessages(p => [...p, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
      await delay(1500);
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, {
        id: `pr-${Date.now()}`, sender: "bot", type: "price-guide",
        poiData: {
          title: "Nearby Price Guide",
          subtitle: "Average prices around Cheonggyecheon",
          items: [
            { name: "Americano", nameKr: "아메리카노", price: "₩4,500" },
            { name: "Kalguksu", nameKr: "칼국수", price: "₩9,000" },
            { name: "Bindaetteok", nameKr: "빈대떡", price: "₩12,000" },
            { name: "Draft Beer", nameKr: "생맥주", price: "₩5,000" },
            { name: "Hotteok", nameKr: "호떡", price: "₩1,500" },
            { name: "Namsan Cable Car", nameKr: "남산 케이블카", price: "₩11,000" },
          ],
          tip: "Most street food stalls are cash-only. ATMs are available at every convenience store (CU, GS25)."
        }
      }]; });
      await delay(1000);
      setMessages(p => [...p, { id: `btn-${Date.now()}`, sender: "bot", type: "action-buttons", poiData: { options: [
        { label: "Korean Phrases", action: "phrases" },
        { label: "Photo Spots", action: "photo" },
        { label: "Keep Walking", action: "skip" },
      ] } }]);
    }

    if (action === "phrases") {
      await delay(800);
      setMessages(p => [...p, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
      await delay(1200);
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, {
        id: `ph-${Date.now()}`, sender: "bot", type: "nearby-phrases",
        poiData: {
          title: "Useful Korean Here",
          phrases: [
            { ko: "여기 어디예요?", pronunciation: "Yeo-gi eo-di-ye-yo?", en: "Where is this?", context: "When showing a map" },
            { ko: "사진 찍어 주세요", pronunciation: "Sa-jin jji-geo ju-se-yo", en: "Please take my photo", context: "At photo spots" },
            { ko: "이거 얼마예요?", pronunciation: "I-geo eol-ma-ye-yo?", en: "How much is this?", context: "At street food stalls" },
            { ko: "맛있어요!", pronunciation: "Ma-si-sseo-yo!", en: "It's delicious!", context: "After eating" },
            { ko: "화장실 어디예요?", pronunciation: "Hwa-jang-sil eo-di-ye-yo?", en: "Where's the restroom?", context: "Essential!" },
          ]
        }
      }]; });
      await delay(1000);
      setMessages(p => [...p, { id: `btn-${Date.now()}`, sender: "bot", type: "action-buttons", poiData: { options: [
        { label: "Nearby Prices", action: "prices" },
        { label: "Photo Spots", action: "photo" },
        { label: "Keep Walking", action: "skip" },
      ] } }]);
    }

    if (action === "skip") {
      await delay(800);
      setMessages(p => [...p, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
      await delay(1500);
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, { id: `sk-${Date.now()}`, sender: "bot", text: "Let's keep moving! You're getting close!" }]; });
      setProgress(pr => Math.min(pr + 20, 80));
      await delay(2500);
      setMessages(p => [...p, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
      await delay(1500);
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, { id: `ah-${Date.now()}`, sender: "bot", text: `Almost there! ${destination.name} is just around the corner.` }]; });
      await delay(1500);
      setMessages(p => [...p, { id: `btn-${Date.now()}`, sender: "bot", type: "action-buttons", poiData: { options: [
        { label: "I've Arrived!", action: "arrived" },
        { label: "View Map", action: "showmap" },
      ] } }]);
      setProgress(90);
    }

    if (action === "showmap") {
      setIsSnapping(true); setSheetPct(SNAP_MAP);
      await delay(500);
      setMessages(p => [...p, { id: `mh-${Date.now()}`, sender: "system", text: "Drag up to return to chat" }]);
    }

    if (action === "arrived") {
      hasAutoArrivedRef.current = true;
      setGpsStatus("arrived");
      setProgress(100);
      await delay(800);
      setMessages(p => [...p, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
      await delay(1500);
      setMessages(p => { const f = p.filter(m => !m.isTyping); return [...f, { id: `ar-${Date.now()}`, sender: "bot", text: `Welcome to ${destination.name}! Have an amazing time exploring!` }]; });
      await delay(1500);
      setMessages(p => [...p, { id: `btn-${Date.now()}`, sender: "bot", type: "action-buttons", poiData: { options: [{ label: "Back to Home", action: "home" }] } }]);
    }

    if (action === "home") { onComplete(); }
    setIsProcessing(false);
  };

  /* ══════════════════════════════════════
     RENDER MESSAGES
     ══════════════════════════════════════ */
  const renderMessage = (msg: MsgType, index: number) => {
    const isNextSame = messages[index + 1]?.sender === msg.sender;
    const mb = isNextSame ? "mb-2.5" : "mb-5";

    if (msg.sender === "system") {
      const isArrival = msg.text?.includes("arrived at the destination");
      return (
        <div key={msg.id} className="flex justify-center w-full mb-4 mt-2 animate-fadeUp">
          <div className={`backdrop-blur-sm px-4 py-1.5 rounded-full border flex items-center gap-1.5 ${
            isArrival
              ? "bg-[#DCFCE7]/80 border-[#16A34A]/20"
              : "bg-[#1A1A2E]/5 border-[#1A1A2E]/8"
          }`}>
            {isArrival && <MapPin size={11} color="#16A34A" strokeWidth={2.5} />}
            <span className={`text-[11px] tracking-wide ${isArrival ? "text-[#16A34A]" : "text-[#8892A4]"}`} style={{ fontWeight: 600 }}>{msg.text}</span>
          </div>
        </div>
      );
    }

    if (msg.sender === "bot") {
      const isFirst = index === 0 || messages[index - 1].sender !== "bot";

      /* ── Rich content blocks (full-width, no bubble) ── */
      if (msg.type === "poi-hero" && msg.poiData) {
        return <div key={msg.id} className="w-full"><POIHeroCard data={msg.poiData} /></div>;
      }
      if (msg.type === "info-grid" && msg.poiData) {
        return <div key={msg.id} className="w-full"><InfoGridBlock data={msg.poiData} /></div>;
      }
      if (msg.type === "price-guide" && msg.poiData) {
        return <div key={msg.id} className="w-full"><PriceGuideBlock data={msg.poiData} /></div>;
      }
      if (msg.type === "nearby-phrases" && msg.poiData) {
        return <div key={msg.id} className="w-full"><NearbyPhrasesBlock data={msg.poiData} /></div>;
      }
      if (msg.type === "poi-confirm") {
        return (
          <div key={msg.id} className="flex flex-col w-full animate-fadeUp gap-1.5 mb-3">
            <div className="flex items-center gap-2 mb-1 ml-1">
              <img src={mascotImg} className="w-6 h-6 rounded-full border-[1.5px] border-[#5478FF] shadow-sm object-cover bg-[#EEF2FF]" alt="Bot" />
              <span className="text-[12px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>Kkaebi</span>
            </div>
            <div className="w-fit max-w-[85%] bg-white rounded-[4px_16px_16px_16px] px-4 py-3.5 shadow-sm border border-[rgba(84,120,255,0.12)]">
              {msg.text && (
                <div className="text-[14px] text-[#1A1A2E] leading-[1.6] whitespace-pre-line" style={{ fontWeight: 500 }}>{msg.text}</div>
              )}
              <div className="flex gap-2 mt-3">
                <button
                  onClick={() => handlePoiConfirm(true)}
                  disabled={isProcessing}
                  className="flex items-center gap-1.5 px-3.5 py-2 bg-[#5478FF] text-white rounded-[10px] text-[13px] hover:bg-[#3D5FE6] transition-all disabled:opacity-50"
                  style={{ fontWeight: 600 }}
                >
                  <CircleCheckBig size={13} strokeWidth={2.5} />
                  Yes!
                </button>
                <button
                  onClick={() => handlePoiConfirm(false)}
                  disabled={isProcessing}
                  className="flex items-center gap-1.5 px-3.5 py-2 bg-[#F3F4F6] text-[#6B7280] rounded-[10px] text-[13px] hover:bg-[#E5E7EB] transition-all disabled:opacity-50"
                  style={{ fontWeight: 600 }}
                >
                  <SkipForward size={13} strokeWidth={2.5} />
                  No thanks
                </button>
              </div>
            </div>
          </div>
        );
      }
      if (msg.type === "photo-spots" && msg.poiData) {
        return <div key={msg.id} className="w-full"><PhotoSpotsBlock data={msg.poiData} /></div>;
      }

      return (
        <div key={msg.id} className={`flex flex-col w-full animate-fadeUp gap-1.5 ${mb}`}>
          {isFirst && (
            <div className="flex items-center gap-2 mb-1 ml-1">
              <img src={mascotImg} className="w-6 h-6 rounded-full border-[1.5px] border-[#5478FF] shadow-sm object-cover bg-[#EEF2FF]" alt="Bot" />
              <span className="text-[12px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>Kkaebi</span>
            </div>
          )}

          {msg.isTyping && (
            <div className="w-fit max-w-[85%] bg-white rounded-[4px_16px_16px_16px] px-4 py-3 shadow-sm border border-[rgba(84,120,255,0.12)]">
              <div className="flex items-center gap-1.5 h-5 px-1">
                <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70 [animation-delay:-0.3s]" />
                <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70 [animation-delay:-0.15s]" />
                <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70" />
              </div>
            </div>
          )}

          {msg.text && (
            <div className="w-fit max-w-[85%] bg-white rounded-[4px_16px_16px_16px] px-4 py-3 shadow-sm border border-[rgba(84,120,255,0.12)]">
              <div className="text-[14px] text-[#1A1A2E] leading-[1.6] whitespace-pre-line" style={{ fontWeight: 500 }}>{msg.text}</div>
            </div>
          )}

          {msg.type === "audio-guide" && msg.poiData && <AudioGuideCard poiData={msg.poiData} />}

          {msg.type === "action-buttons" && msg.poiData && (
            <div className="flex flex-wrap gap-2 mt-2 w-[95%]">
              {msg.poiData.options.map((opt: { label: string; action: string }) => (
                <button key={opt.action} onClick={() => handleUserAction(opt.action, opt.label)} disabled={isProcessing} className="text-left px-3.5 py-2.5 bg-white border border-[rgba(84,120,255,0.25)] hover:bg-[#5478FF] hover:text-white hover:border-[#5478FF] rounded-xl text-[13px] text-[#5478FF] transition-all shadow-sm disabled:opacity-50 flex items-center gap-2" style={{ fontWeight: 600 }}>
                  <ActionIcon action={opt.action} />
                  {opt.label}
                </button>
              ))}
            </div>
          )}
        </div>
      );
    }

    // User message
    return (
      <div key={msg.id} className={`flex justify-end w-full animate-fadeUp ${mb}`}>
        <div className="bg-[#5478FF] rounded-[16px_4px_16px_16px] px-4 py-2.5 shadow-md max-w-[80%]">
          <div className="text-[13px] text-white leading-relaxed tracking-wide" style={{ fontWeight: 600 }}>{msg.text}</div>
        </div>
      </div>
    );
  };

  /* ══════════════════════════════════════
     JSX
     ══════════════════════════════════════ */
  return (
    <div ref={containerRef} className="w-full h-full relative bg-[#EDF2F7] font-['Pretendard'] overflow-hidden animate-screenSwitch">

      <div className="absolute inset-0 z-0">
        <NaverMapView
          destination={{ lat: destination.lat, lng: destination.lng, name: destination.name }}
          userPosition={userPosition}
          transportMode={transportMode}
          transitRouteId={transitRouteId}
          progress={progress}
          mapRatio={mapRatio}
        />
      </div>

      {/* Header */}
      <div className="absolute top-0 left-0 right-0 z-40 bg-gradient-to-b from-white/80 via-white/40 to-transparent pt-3 pb-6 px-4">
        <div className="flex items-center justify-between">
          <div className="bg-white/95 backdrop-blur-md px-4 py-2 rounded-full shadow-sm border border-gray-100 flex items-center gap-2">
            {(() => {
              const mc = transportMode === "drive" ? "#E11D48" : transportMode === "transit" ? "#7C3AED" : "#5478FF";
              const ModeIcon = transportMode === "drive" ? Car : transportMode === "transit" ? TrainFrontTunnel : Footprints;
              const defaultDist = transportMode === "drive" ? "1.2km" : transportMode === "transit" ? "3 stops" : "1.2km";
              const defaultTime = transportMode === "drive" ? "~3 min" : transportMode === "transit" ? "~12 min" : "~15 min";
              const speedDiv = transportMode === "drive" ? 250 : transportMode === "transit" ? 150 : 80;
              if (gpsStatus === "arrived") return (
                <span className="text-[13px] text-[#16A34A] flex items-center gap-1.5" style={{ fontWeight: 800 }}>
                  <CircleCheckBig size={14} color="#16A34A" strokeWidth={2.5} />
                  Arrived!
                </span>
              );
              if (distanceToDestination !== null) return (
                <>
                  <ModeIcon size={13} color={mc} strokeWidth={2.5} />
                  <span className="text-[13px]" style={{ fontWeight: 800, color: mc }}>
                    {distanceToDestination >= 1000
                      ? `${(distanceToDestination / 1000).toFixed(1)}km`
                      : `${distanceToDestination}m`}
                  </span>
                  <span className="text-[11px] text-[#8892A4]" style={{ fontWeight: 600 }}>
                    · ~{Math.max(1, Math.round(distanceToDestination / speedDiv))} min
                  </span>
                </>
              );
              return (
                <>
                  <ModeIcon size={13} color={mc} strokeWidth={2.5} />
                  <span className="text-[13px]" style={{ fontWeight: 800, color: mc }}>{defaultDist}</span>
                  <span className="text-[11px] text-[#8892A4]" style={{ fontWeight: 600 }}>· {defaultTime}</span>
                </>
              );
            })()}
          </div>
          <button onClick={onMinimize ?? onBack} className="w-9 h-9 bg-white/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-sm border border-gray-100 text-[#1A1A2E]" title="Minimize navigation">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
      </div>

      {/* ETA card */}
      {isChatMinimised && (
        <div className="absolute z-30 animate-fadeUp" style={{ top: `${sheetPct - 18}%`, left: 16, right: 16 }}>
          <div className="bg-white/95 backdrop-blur-xl rounded-2xl p-4 shadow-lg border border-gray-100">
            <div className="flex items-center justify-between mb-2.5">
              <div>
                <div className="text-[10px] text-[#8892A4] uppercase tracking-wider" style={{ fontWeight: 600 }}>Heading to</div>
                <div className="text-[15px] text-[#1A1A2E] mt-0.5" style={{ fontWeight: 700 }}>{destination.name}</div>
              </div>
              <div className="text-right">
                {(() => {
                  const mc = transportMode === "drive" ? "#E11D48" : transportMode === "transit" ? "#7C3AED" : "#5478FF";
                  const speedDiv = transportMode === "drive" ? 250 : transportMode === "transit" ? 150 : 80;
                  const defaultDist = transportMode === "drive" ? "1.2km" : transportMode === "transit" ? "3 stops" : "1.2km";
                  const defaultTime = transportMode === "drive" ? "~3 min" : transportMode === "transit" ? "~12 min" : "~15 min";
                  if (gpsStatus === "arrived") return (
                    <div className="text-[18px] text-[#16A34A] flex items-center justify-end gap-1" style={{ fontWeight: 800 }}>
                      <CircleCheckBig size={16} color="#16A34A" strokeWidth={2.5} /> Arrived
                    </div>
                  );
                  if (distanceToDestination !== null) return (
                    <>
                      <div className="text-[18px]" style={{ fontWeight: 800, color: mc }}>
                        {distanceToDestination >= 1000
                          ? `${(distanceToDestination / 1000).toFixed(1)}km`
                          : `${distanceToDestination}m`}
                      </div>
                      <div className="text-[10px] text-[#8892A4]" style={{ fontWeight: 500 }}>
                        ~{Math.max(1, Math.round(distanceToDestination / speedDiv))} min
                      </div>
                    </>
                  );
                  return (
                    <>
                      <div className="text-[18px]" style={{ fontWeight: 800, color: mc }}>{defaultDist}</div>
                      <div className="text-[10px] text-[#8892A4]" style={{ fontWeight: 500 }}>{defaultTime}</div>
                    </>
                  );
                })()}
              </div>
            </div>
            <div className="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
              <div className="h-full rounded-full transition-all duration-1000 ease-out" style={{
                width: `${progress}%`,
                background: gpsStatus === "arrived" ? "#16A34A"
                  : transportMode === "drive" ? "linear-gradient(to right, #E11D48, #FB7185)"
                  : transportMode === "transit" ? "linear-gradient(to right, #7C3AED, #A78BFA)"
                  : "linear-gradient(to right, #5478FF, #7C98FF)"
              }} />
            </div>
          </div>
        </div>
      )}

      {/* ══ Bottom Sheet ══ */}
      <div
        className={`absolute left-0 right-0 bottom-0 z-30 flex flex-col bg-white rounded-t-[24px] shadow-[0_-8px_30px_rgba(0,0,0,0.12)] ${isSnapping ? "transition-[top] duration-400 ease-[cubic-bezier(0.32,0.72,0,1)]" : ""}`}
        style={{ top: `${sheetPct}%` }}
      >
        {/* Drag Handle */}
        <div
          className="flex-shrink-0 cursor-grab active:cursor-grabbing select-none touch-none"
          onTouchStart={handleTouchStart} onTouchMove={handleTouchMove} onTouchEnd={handleTouchEnd}
          onMouseDown={handleMouseDown}
        >
          <div className="flex justify-center pt-3 pb-1">
            <div className="w-10 h-1 bg-gray-300 rounded-full" />
          </div>
          <div className="flex items-center justify-between px-5 pb-3 pt-1">
            <div className="flex items-center gap-2.5">
              <div className="relative w-8 h-8 rounded-full bg-[#F0F4FF] flex items-center justify-center">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#5478FF" strokeWidth="2.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
                {/* GPS status dot */}
                <span className={`absolute -top-0.5 -right-0.5 w-2.5 h-2.5 rounded-full border-[1.5px] border-white ${
                  gpsStatus === "arrived" ? "bg-[#16A34A]" :
                  gpsStatus === "watching" && distanceToDestination !== null ? "bg-[#16A34A] animate-pulse" :
                  gpsStatus === "denied" ? "bg-[#EF4444]" :
                  "bg-[#F59E0B]"
                }`} />
              </div>
              <div className="flex-1 min-w-0">
                <div className="text-[13px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>{destination.name}</div>
                <div className="flex items-center gap-1.5 mt-1">
                  <div className="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                    <div className={`h-full rounded-full transition-all duration-700 ${gpsStatus === "arrived" ? "bg-[#16A34A]" : "bg-[#5478FF]"}`} style={{ width: `${progress}%` }} />
                  </div>
                  <span className="text-[10px] text-[#8892A4] shrink-0" style={{ fontWeight: 600 }}>{progress}%</span>
                </div>
              </div>
            </div>
            <div className="flex items-center">
              <button
                onClick={(e) => { e.stopPropagation(); onBack(); }}
                className="px-2.5 py-1 rounded-full bg-[#FEE2E2] text-[#EF4444] text-[10px] flex items-center gap-1"
                style={{ fontWeight: 700 }}
              >
                <X size={10} strokeWidth={2.5} />
                Stop
              </button>
            </div>
          </div>
        </div>

        <div className="h-[1px] bg-gray-100 mx-4" />

        {/* Chat + Rich Content */}
        <div ref={scrollRef} className="flex-1 overflow-y-auto overflow-x-hidden hide-scrollbar px-4 pt-4 pb-6">
          {messages.map((msg, i) => renderMessage(msg, i))}
        </div>
      </div>

      {/* ══ Story Mode Overlay ══ */}
      {showStory && (
        <StoryModeScreen
          poiName="Cheonggyecheon Stream"
          poiNameKr="청계천"
          poiIconId="waves"
          pages={[
            {
              text: "You're standing beside one of Seoul's greatest urban transformations. Where rushing water now flows, there was once a six-lane elevated highway thundering with traffic overhead.",
              image: "https://images.unsplash.com/photo-1631176206492-f03b80b3e854?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxDaGVvbmdneWVjaGVvbiUyMHN0cmVhbSUyMFNlb3VsJTIwc3RlcHBpbmclMjBzdG9uZXN8ZW58MXx8fHwxNzczMzg5NDAyfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
              imageCaption: "Cheonggyecheon Stream today",
              ambientLabel: "Flowing water, distant city sounds, birdsong",
            },
            {
              iconId: "building",
              text: "For over 600 years, Cheonggyecheon was a natural stream running through the heart of Seoul. But as the city grew rapidly after the Korean War, it became an open sewer. In 1958, authorities paved it over. By 1971, a massive elevated highway sat on top — a symbol of Korea's breakneck industrialization.",
              image: "https://images.unsplash.com/photo-1710006876076-aa92d8328937?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMG9sZCUyMGhpZ2h3YXklMjBkZW1vbGl0aW9uJTIwY29uc3RydWN0aW9ufGVufDF8fHx8MTc3MzM5MDI2NHww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
              imageCaption: "Seoul's rapid urban transformation",
              ambientLabel: "Construction sounds, old traffic recordings",
            },
            {
              iconId: "sparkles",
              text: "In 2003, Seoul's newly elected mayor Lee Myung-bak made a bold promise: tear down the highway and bring the stream back to life. Many thought it was impossible — the project cost $900 million and took over two years. But on October 1, 2005, the restored Cheonggyecheon was unveiled to the world.",
              image: "https://images.unsplash.com/photo-1762732703913-cfbf690b0300?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMENoZW9uZ2d5ZWNoZW9uJTIwbmlnaHQlMjBsaWdodHMlMjB1cmJhbiUyMHN0cmVhbXxlbnwxfHx8fDE3NzMzOTAyNjN8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
              imageCaption: "The stream at night, beautifully illuminated",
              ambientLabel: "Evening ambiance, gentle water flow",
            },
            {
              iconId: "leaf",
              text: "Today, this 10.9km oasis is home to over 500 species of plants, fish, and birds. The temperature here is 3-5°C cooler than surrounding streets. Every autumn, the famous Lantern Festival fills the stream with glowing works of art. Take a moment — listen to the water, feel the breeze, and enjoy this little miracle in the middle of a megacity.",
              image: "https://images.unsplash.com/photo-1737044541782-96b795fb03ba?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMGxhbnRlcm4lMjBmZXN0aXZhbCUyMHN0cmVhbSUyMGF1dHVtbnxlbnwxfHx8fDE3NzMzOTAyNjR8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
              imageCaption: "Autumn along the stream",
              ambientLabel: "Nature sounds, leaves rustling, distant laughter",
            },
          ]}
          onClose={handleStoryClose}
          onFinish={handleStoryClose}
        />
      )}
    </div>
  );
}


/* ══════════════════════════════════════
   Audio Guide
   ══════════════════════════════════════ */
function AudioGuideCard({ poiData }: { poiData: any }) {
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const totalSeconds = 84;

  useEffect(() => {
    if (isPlaying && currentTime < totalSeconds) {
      intervalRef.current = setInterval(() => {
        setCurrentTime(p => { if (p >= totalSeconds) { setIsPlaying(false); return totalSeconds; } return p + 1; });
      }, 1000);
    }
    return () => { if (intervalRef.current) clearInterval(intervalRef.current); };
  }, [isPlaying, currentTime]);

  const fmt = (s: number) => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, "0")}`;
  const pct = (currentTime / totalSeconds) * 100;

  return (
    <div className="w-[90%] mt-1 bg-[#1A1A2E] rounded-[16px] p-4 shadow-lg border border-gray-800">
      <div className="flex items-center gap-3 mb-3">
        <button onClick={() => setIsPlaying(!isPlaying)} className="w-10 h-10 rounded-full bg-[#5478FF] flex items-center justify-center flex-shrink-0 cursor-pointer shadow-[0_0_15px_rgba(84,120,255,0.5)] transition-transform active:scale-90">
          {isPlaying ? (
            <svg width="14" height="14" viewBox="0 0 24 24" fill="white"><rect x="6" y="4" width="4" height="16" rx="1" /><rect x="14" y="4" width="4" height="16" rx="1" /></svg>
          ) : (
            <svg width="16" height="16" viewBox="0 0 24 24" fill="white"><polygon points="5 3 19 12 5 21 5 3" /></svg>
          )}
        </button>
        <div className="flex-1 min-w-0">
          <p className="text-[10px] text-[#FFDE42] tracking-wider uppercase" style={{ fontWeight: 700 }}>Audio Guide</p>
          <p className="text-[13px] text-white truncate" style={{ fontWeight: 700 }}>{poiData.title}</p>
        </div>
      </div>
      <div className="w-full h-1.5 bg-white/15 rounded-full overflow-hidden mb-1.5">
        <div className="h-full bg-[#5478FF] rounded-full transition-all duration-300" style={{ width: `${pct}%` }} />
      </div>
      <div className="flex justify-between text-[10px] text-white/50" style={{ fontWeight: 500 }}>
        <span>{fmt(currentTime)}</span>
        <span>{poiData.duration}</span>
      </div>
    </div>
  );
}