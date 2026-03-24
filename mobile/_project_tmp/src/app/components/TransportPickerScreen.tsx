/**
 * TransportPickerScreen.tsx — Transport mode selection (full-screen, no map)
 * Mirrors: Android > ui/screen/transport/TransportPickerScreen.kt
 *
 * Walk / Transit / Drive — Transit navigates to TransitDetailScreen.
 */
import { useState } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import {
  Footprints, TrainFront, Car, ChevronLeft,
  Clock, MapPin, Navigation
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

export type TransportMode = "walk" | "transit" | "drive";

const MODE_OPTIONS: {
  mode: TransportMode;
  label: string;
  desc: string;
  icon: React.ElementType;
  iconBg: string;
  iconFg: string;
  duration: string;
  eta: string;
}[] = [
  { mode: "walk", label: "Walk", desc: "Enjoy the scenery", icon: Footprints, iconBg: "#DBEAFE", iconFg: "#2563EB", duration: "5 min", eta: "ETA 9:46 AM" },
  { mode: "transit", label: "Transit", desc: "Bus & Subway", icon: TrainFront, iconBg: "#EDE9FE", iconFg: "#7C3AED", duration: "12 min", eta: "ETA 9:53 AM" },
  { mode: "drive", label: "Drive", desc: "Taxi / Car", icon: Car, iconBg: "#FFE4E6", iconFg: "#E11D48", duration: "3 min", eta: "ETA 9:44 AM" },
];

/* ══════════════════════════════════════
   MAIN COMPONENT
   ══════════════════════════════════════ */
interface TransportPickerScreenProps {
  placeId: string;
  onBack: () => void;
  onStart: (mode: TransportMode) => void;
  onTransitDetail: () => void;
}

export function TransportPickerScreen({ placeId, onBack, onStart, onTransitDetail }: TransportPickerScreenProps) {
  const place = PLACE_DATA[placeId] || PLACE_DATA.dm3;
  const [selectedMode, setSelectedMode] = useState<TransportMode>("walk");

  const selectedOption = MODE_OPTIONS.find(m => m.mode === selectedMode)!;

  const handleStart = () => {
    if (selectedMode === "transit") {
      onTransitDetail();
    } else {
      onStart(selectedMode);
    }
  };

  return (
    <div className="w-full h-full relative bg-white font-['Pretendard'] flex flex-col animate-screenSwitch">

      {/* ── Header ── */}
      <div className="flex-shrink-0 px-5 pt-4 pb-3">
        <div className="flex items-center gap-3">
          <button
            onClick={onBack}
            className="w-9 h-9 bg-[#F5F7FA] rounded-full flex items-center justify-center cursor-pointer hover:bg-[#EBEEF3] transition-colors"
          >
            <ChevronLeft size={18} color="#1A1A2E" strokeWidth={2.5} />
          </button>
          <div className="text-[17px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>
            Choose Transport
          </div>
        </div>
      </div>

      {/* ── Destination Card ── */}
      <div className="flex-shrink-0 mx-5 mb-5 p-4 bg-gradient-to-br from-[#F8FAFF] to-[#F0F4FF] rounded-2xl border border-[#E8EDFF]">
        <div className="flex items-center gap-3.5">
          <div className="w-14 h-14 rounded-2xl bg-white border-2 border-[#5478FF]/20 flex items-center justify-center flex-shrink-0 shadow-sm">
            <img src={mascotImg} alt="" className="w-9 h-9 rounded-xl object-cover" />
          </div>
          <div className="flex-1">
            <div className="text-[18px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>{place.name}</div>
            <div className="text-[13px] text-[#8892A4] mt-0.5" style={{ fontWeight: 500 }}>{place.nameKr}</div>
          </div>
        </div>
        <div className="flex items-center gap-4 mt-3 pt-3 border-t border-[#E0E7FF]/60">
          <div className="flex items-center gap-1.5">
            <MapPin size={13} color="#5478FF" strokeWidth={2.5} />
            <span className="text-[12px] text-[#5C6580]" style={{ fontWeight: 600 }}>{place.distance}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Clock size={13} color="#5478FF" strokeWidth={2.5} />
            <span className="text-[12px] text-[#5C6580]" style={{ fontWeight: 600 }}>{selectedOption.duration}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Navigation size={13} color="#5478FF" strokeWidth={2.5} />
            <span className="text-[12px] text-[#5C6580]" style={{ fontWeight: 600 }}>{selectedOption.eta.replace("ETA ", "")}</span>
          </div>
        </div>
      </div>

      {/* ── Section Title ── */}
      <div className="px-5 mb-3">
        <div className="text-[15px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>How would you like to go?</div>
        <div className="text-[12px] text-[#8892A4] mt-0.5" style={{ fontWeight: 500 }}>Select your preferred transport mode</div>
      </div>

      {/* ── Mode Cards ── */}
      <div className="flex-1 overflow-y-auto hide-scrollbar px-5 pb-4">
        <div className="flex flex-col gap-3">
          {MODE_OPTIONS.map((opt) => {
            const isSelected = selectedMode === opt.mode;
            const Icon = opt.icon;
            return (
              <button
                key={opt.mode}
                onClick={() => setSelectedMode(opt.mode)}
                className={`w-full flex items-center gap-4 p-4 rounded-2xl border-2 transition-all cursor-pointer ${
                  isSelected
                    ? "border-[#5478FF] bg-[#F8FAFF] shadow-[0_0_0_3px_rgba(84,120,255,0.08)]"
                    : "border-gray-100 bg-white hover:border-gray-200"
                }`}
              >
                {/* Icon */}
                <span
                  className="w-12 h-12 rounded-2xl flex items-center justify-center flex-shrink-0"
                  style={{ background: opt.iconBg }}
                >
                  <Icon size={22} color={opt.iconFg} strokeWidth={2} />
                </span>

                {/* Label & Description */}
                <div className="flex-1 text-left">
                  <div className="text-[16px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>{opt.label}</div>
                  <div className="text-[12px] text-[#8892A4] mt-0.5" style={{ fontWeight: 500 }}>{opt.desc}</div>
                </div>

                {/* Duration & ETA */}
                <div className="text-right flex-shrink-0 mr-1">
                  <div className="text-[16px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>{opt.duration}</div>
                  <div className="text-[11px] text-[#8892A4]" style={{ fontWeight: 500 }}>{opt.eta}</div>
                </div>

                {/* Radio */}
                <div className={`w-[22px] h-[22px] rounded-full border-2 flex items-center justify-center flex-shrink-0 transition-all ${
                  isSelected ? "border-[#5478FF]" : "border-gray-200"
                }`}>
                  {isSelected && <div className="w-[12px] h-[12px] rounded-full bg-[#5478FF]" />}
                </div>
              </button>
            );
          })}
        </div>

        {/* ── Transit hint ── */}
        {selectedMode === "transit" && (
          <div className="mt-3 p-3 bg-[#F5F3FF] rounded-xl border border-[#E9E5FF] flex items-center gap-2.5 animate-fadeUp">
            <span className="w-8 h-8 rounded-lg bg-[#EDE9FE] flex items-center justify-center flex-shrink-0">
              <TrainFront size={15} color="#7C3AED" strokeWidth={2.2} />
            </span>
            <div className="flex-1">
              <div className="text-[12px] text-[#5B21B6]" style={{ fontWeight: 600 }}>
                Tap below to see available routes
              </div>
              <div className="text-[11px] text-[#7C3AED]/70 mt-0.5" style={{ fontWeight: 500 }}>
                Bus, Subway, and transfer options
              </div>
            </div>
          </div>
        )}
      </div>

      {/* ── CTA Button ── */}
      <div className="flex-shrink-0 px-5 pb-7 pt-3">
        <button
          onClick={handleStart}
          className="w-full py-4 rounded-2xl text-white text-[15px] cursor-pointer shadow-[0_8px_24px_rgba(84,120,255,0.3)] active:scale-[0.98] transition-transform flex items-center justify-center gap-2"
          style={{
            fontWeight: 700,
            background: "linear-gradient(135deg, #325BFF 0%, #5478FF 50%, #7C98FF 100%)",
          }}
        >
          {selectedMode === "transit" ? "View Transit Routes" : "Start Navigation"}
        </button>
      </div>
    </div>
  );
}
