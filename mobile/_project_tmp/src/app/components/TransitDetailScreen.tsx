/**
 * TransitDetailScreen.tsx — Detailed transit route selection
 * Mirrors: Android > ui/screen/transport/TransitDetailScreen.kt
 *
 * Shows Bus transfer, Subway, Bus direct routes with segments, durations, ETAs.
 */
import { useState } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import {
  ChevronLeft, Bus, TrainFrontTunnel, ArrowRight,
  Clock, CircleDot, MapPin, Check
} from "lucide-react";

/* ── Place data ── */
const PLACE_DATA: Record<string, { name: string; nameKr: string; distance: string }> = {
  dm3: { name: "Gwangjang Market", nameKr: "광장시장", distance: "350m" },
  dm4: { name: "Bukchon Hanok Village", nameKr: "북촌한옥마을", distance: "1.2km" },
  dm5: { name: "Namsan Tower", nameKr: "남산타워", distance: "2.1km" },
  dm6: { name: "Ikseon-dong", nameKr: "익선동", distance: "800m" },
  dm7: { name: "Cheonggyecheon Stream", nameKr: "청계천", distance: "600m" },
  bukchon: { name: "Bukchon Hanok Village", nameKr: "북촌한옥마을", distance: "1.2km" },
  namsan: { name: "Namsan Tower", nameKr: "남산타워", distance: "2.1km" },
};

interface TransitRoute {
  id: string;
  label: string;
  desc: string;
  duration: string;
  eta: string;
  icon: React.ElementType;
  iconBg: string;
  iconFg: string;
  badgeColor: string;
  segments: { color: string; label: string; type: string; from: string; to: string; stops: number; duration: string }[];
}

const TRANSIT_ROUTES: TransitRoute[] = [
  {
    id: "bus-transfer",
    label: "Bus 144 + 272",
    desc: "Transfer at Jongno 3-ga",
    duration: "12 min",
    eta: "ETA 9:53 AM",
    icon: Bus,
    iconBg: "#DBEAFE",
    iconFg: "#2563EB",
    badgeColor: "#2563EB",
    segments: [
      { color: "#2563EB", label: "Bus 144", type: "Bus", from: "Euljiro 3-ga", to: "Jongno 3-ga", stops: 3, duration: "7 min" },
      { color: "#16A34A", label: "Bus 272", type: "Bus", from: "Jongno 3-ga", to: "Gwangjang Market", stops: 2, duration: "5 min" },
    ],
  },
  {
    id: "subway",
    label: "Subway Line 1",
    desc: "Jongno 3-ga Station",
    duration: "15 min",
    eta: "ETA 9:56 AM",
    icon: TrainFrontTunnel,
    iconBg: "#EDE9FE",
    iconFg: "#7C3AED",
    badgeColor: "#1D4ED8",
    segments: [
      { color: "#1D4ED8", label: "Line 1", type: "Subway", from: "Euljiro 1-ga Stn.", to: "Jongno 5-ga Stn.", stops: 4, duration: "10 min" },
    ],
  },
  {
    id: "bus-direct",
    label: "Bus 272",
    desc: "Direct, no transfer",
    duration: "18 min",
    eta: "ETA 9:59 AM",
    icon: Bus,
    iconBg: "#DCFCE7",
    iconFg: "#16A34A",
    badgeColor: "#16A34A",
    segments: [
      { color: "#16A34A", label: "Bus 272", type: "Bus", from: "Euljiro 3-ga", to: "Gwangjang Market", stops: 5, duration: "18 min" },
    ],
  },
];

/* ── Segment Timeline ── */
function SegmentTimeline({ route }: { route: TransitRoute }) {
  return (
    <div className="mt-3 bg-white rounded-xl border border-gray-100 overflow-hidden">
      {route.segments.map((seg, i) => (
        <div key={seg.label}>
          {/* Segment row */}
          <div className="px-3.5 py-3 flex items-start gap-3">
            {/* Timeline dot + line */}
            <div className="flex flex-col items-center pt-0.5 flex-shrink-0" style={{ width: 20 }}>
              <div className="w-3.5 h-3.5 rounded-full border-[2.5px] flex-shrink-0" style={{ borderColor: seg.color, background: "white" }} />
              <div className="w-[2px] flex-1 min-h-[32px] mt-1" style={{ background: seg.color, opacity: 0.4 }} />
            </div>

            {/* Segment info */}
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <span
                  className="px-2 py-0.5 rounded-md text-white text-[10px]"
                  style={{ fontWeight: 700, background: seg.color }}
                >
                  {seg.label}
                </span>
                <span className="text-[10px] text-[#8892A4]" style={{ fontWeight: 600 }}>{seg.type}</span>
              </div>
              <div className="mt-1.5 flex items-center gap-1.5">
                <CircleDot size={10} color={seg.color} strokeWidth={2.5} />
                <span className="text-[11px] text-[#1A1A2E]" style={{ fontWeight: 600 }}>{seg.from}</span>
              </div>
              <div className="ml-[3px] my-1 border-l-[1.5px] border-dashed pl-3 py-0.5" style={{ borderColor: seg.color + "60" }}>
                <span className="text-[10px] text-[#8892A4]" style={{ fontWeight: 500 }}>
                  {seg.stops} stops · {seg.duration}
                </span>
              </div>
              <div className="flex items-center gap-1.5">
                <MapPin size={10} color={seg.color} strokeWidth={2.5} />
                <span className="text-[11px] text-[#1A1A2E]" style={{ fontWeight: 600 }}>{seg.to}</span>
              </div>
            </div>
          </div>

          {/* Transfer indicator between segments */}
          {i < route.segments.length - 1 && (
            <div className="mx-3.5 py-2 border-t border-dashed border-[#F59E0B]/30 flex items-center gap-2 pl-[10px]">
              <div className="w-5 h-5 rounded-full bg-[#FEF3C7] flex items-center justify-center">
                <ArrowRight size={10} color="#F59E0B" strokeWidth={3} />
              </div>
              <span className="text-[11px] text-[#D97706]" style={{ fontWeight: 700 }}>Transfer at {seg.to}</span>
              <span className="text-[10px] text-[#D97706]/60" style={{ fontWeight: 500 }}>~2 min walk</span>
            </div>
          )}
        </div>
      ))}

      {/* Arrival */}
      <div className="px-3.5 py-2.5 border-t border-gray-50 flex items-center gap-3">
        <div className="flex flex-col items-center flex-shrink-0" style={{ width: 20 }}>
          <div className="w-4 h-4 rounded-full bg-[#5478FF] flex items-center justify-center">
            <Check size={10} color="white" strokeWidth={3} />
          </div>
        </div>
        <span className="text-[12px] text-[#5478FF]" style={{ fontWeight: 700 }}>Arrive at destination</span>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════
   MAIN COMPONENT
   ══════════════════════════════════════ */
interface TransitDetailScreenProps {
  placeId: string;
  onBack: () => void;
  onStart: (transitRouteId: string) => void;
}

export function TransitDetailScreen({ placeId, onBack, onStart }: TransitDetailScreenProps) {
  const place = PLACE_DATA[placeId] || PLACE_DATA.dm3;
  const [selectedRoute, setSelectedRoute] = useState<TransitRoute>(TRANSIT_ROUTES[0]);

  return (
    <div className="w-full h-full relative bg-[#F7F9FC] font-['Pretendard'] flex flex-col animate-screenSwitch">

      {/* ── Header ── */}
      <div className="flex-shrink-0 bg-white px-5 pt-4 pb-3 border-b border-gray-100">
        <div className="flex items-center gap-3">
          <button
            onClick={onBack}
            className="w-9 h-9 bg-[#F5F7FA] rounded-full flex items-center justify-center cursor-pointer hover:bg-[#EBEEF3] transition-colors"
          >
            <ChevronLeft size={18} color="#1A1A2E" strokeWidth={2.5} />
          </button>
          <div className="flex-1">
            <div className="text-[16px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>Transit Routes</div>
            <div className="text-[11px] text-[#8892A4]" style={{ fontWeight: 500 }}>to {place.name}</div>
          </div>
          <div className="w-9 h-9 rounded-full bg-[#EEF2FF] border border-[#5478FF]/20 flex items-center justify-center">
            <img src={mascotImg} alt="" className="w-6 h-6 rounded-full object-cover" />
          </div>
        </div>
      </div>

      {/* ── Route List ── */}
      <div className="flex-1 overflow-y-auto hide-scrollbar px-5 pt-4 pb-4">
        <div className="flex flex-col gap-3">
          {TRANSIT_ROUTES.map((route) => {
            const isSelected = selectedRoute.id === route.id;
            const Icon = route.icon;

            return (
              <div key={route.id} className="animate-fadeUp">
                {/* Route card */}
                <button
                  onClick={() => setSelectedRoute(route)}
                  className={`w-full text-left rounded-2xl border-2 transition-all cursor-pointer overflow-hidden ${
                    isSelected
                      ? "border-[#5478FF] bg-white shadow-[0_4px_16px_rgba(84,120,255,0.1)]"
                      : "border-transparent bg-white shadow-sm hover:shadow-md"
                  }`}
                >
                  {/* Top row */}
                  <div className="flex items-center gap-3.5 p-4 pb-3">
                    <span
                      className="w-11 h-11 rounded-[14px] flex items-center justify-center flex-shrink-0"
                      style={{ background: route.iconBg }}
                    >
                      <Icon size={20} color={route.iconFg} strokeWidth={2} />
                    </span>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5 flex-nowrap">
                        <span className="text-[14px] text-[#1A1A2E] whitespace-nowrap" style={{ fontWeight: 700 }}>{route.label}</span>
                        {route.segments.length > 1 && (
                          <span className="px-1.5 py-0.5 bg-[#FEF3C7] text-[#D97706] text-[9px] rounded whitespace-nowrap flex-shrink-0" style={{ fontWeight: 700 }}>
                            TRANSFER
                          </span>
                        )}
                        {route.segments.length === 1 && route.id === "bus-direct" && (
                          <span className="px-1.5 py-0.5 bg-[#DCFCE7] text-[#16A34A] text-[9px] rounded whitespace-nowrap flex-shrink-0" style={{ fontWeight: 700 }}>
                            DIRECT
                          </span>
                        )}
                      </div>
                      <div className="text-[11px] text-[#8892A4] mt-0.5 whitespace-nowrap truncate" style={{ fontWeight: 500 }}>{route.desc}</div>
                    </div>

                    <div className="text-right flex-shrink-0 ml-1">
                      <div className="text-[15px] text-[#1A1A2E] whitespace-nowrap" style={{ fontWeight: 700 }}>{route.duration}</div>
                      <div className="flex items-center gap-1 justify-end mt-0.5 whitespace-nowrap">
                        <Clock size={10} color="#8892A4" strokeWidth={2} />
                        <span className="text-[10px] text-[#8892A4]" style={{ fontWeight: 500 }}>{route.eta}</span>
                      </div>
                    </div>

                    {/* Radio */}
                    <div className={`w-[22px] h-[22px] rounded-full border-2 flex items-center justify-center flex-shrink-0 transition-all ${
                      isSelected ? "border-[#5478FF]" : "border-gray-200"
                    }`}>
                      {isSelected && <div className="w-[12px] h-[12px] rounded-full bg-[#5478FF]" />}
                    </div>
                  </div>

                  {/* Segment color bar */}
                  <div className="flex mx-4 mb-3 rounded-full overflow-hidden h-[4px] bg-gray-100">
                    {route.segments.map((seg, i) => (
                      <div
                        key={i}
                        className="h-full"
                        style={{
                          background: seg.color,
                          flex: route.segments.length > 1 ? (i === 0 ? 3 : 2) : 1,
                        }}
                      />
                    ))}
                  </div>
                </button>

                {/* Expanded segment timeline */}
                {isSelected && (
                  <div className="mt-1 animate-fadeUp">
                    <SegmentTimeline route={route} />
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* ── Tip card ── */}
        <div className="mt-4 flex items-end gap-2">
          {/* Kkaebi avatar */}
          <div className="w-7 h-7 rounded-full bg-gradient-to-br from-[#5478FF] to-[#7C98FF] flex items-center justify-center flex-shrink-0 shadow-sm">
            <img src={mascotImg} alt="" className="w-[18px] h-[18px] rounded-full object-cover" />
          </div>
          {/* Speech bubble */}
          <div className="relative flex-1 bg-gradient-to-br from-[#F0F4FF] to-[#F8FAFF] rounded-2xl rounded-bl-md px-3.5 py-3 shadow-[0_2px_8px_rgba(84,120,255,0.06)]">
            <div className="text-[11px] text-[#5478FF] tracking-wide" style={{ fontWeight: 700 }}>Kkaebi's Tip</div>
            <div className="text-[11.5px] text-[#3D4663] mt-1 leading-[1.55]" style={{ fontWeight: 500 }}>
              Bus 144 + 272 is the fastest option. The transfer at Jongno 3-ga takes only 2 minutes!
            </div>
          </div>
        </div>
      </div>

      {/* ── CTA Button ── */}
      <div className="flex-shrink-0 bg-white px-5 pb-7 pt-3 border-t border-gray-100">
        <button
          onClick={() => onStart(selectedRoute.id)}
          className="w-full py-4 rounded-2xl text-white text-[15px] cursor-pointer shadow-[0_8px_24px_rgba(84,120,255,0.3)] active:scale-[0.98] transition-transform flex items-center justify-center gap-2"
          style={{
            fontWeight: 700,
            background: "linear-gradient(135deg, #325BFF 0%, #5478FF 50%, #7C98FF 100%)",
          }}
        >
          Start Navigation
        </button>
      </div>
    </div>
  );
}