import { useState } from "react";
import { Map as MapIcon } from "lucide-react";

interface MapScreenProps {
  selectedCategories: string[];
}

export function MapScreen({ selectedCategories }: MapScreenProps) {
  return (
    <div
      className="flex-1 flex flex-col items-center justify-center animate-screenSwitch"
      style={{ background: "#F8FAFF" }}
    >
      {/* Placeholder map */}
      <div
        className="w-full flex-1 relative"
        style={{ background: "#EDF2F7" }}
      >
        <svg
          width="100%"
          height="100%"
          viewBox="0 0 400 600"
          xmlns="http://www.w3.org/2000/svg"
          preserveAspectRatio="xMidYMid slice"
        >
          <defs>
            <pattern
              id="grid"
              width="40"
              height="40"
              patternUnits="userSpaceOnUse"
            >
              <path
                d="M 40 0 L 0 0 0 40"
                fill="none"
                stroke="#E2E8F0"
                strokeWidth="0.5"
                opacity="0.6"
              />
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)" />
          <path
            d="M -50 300 Q 150 400 450 250 L 450 320 Q 150 450 -50 350 Z"
            fill="#E8F0FE"
            opacity="0.8"
          />
          <path
            d="M 100 -50 L 120 150 Q 140 250 280 300 L 450 330"
            fill="none"
            stroke="#FFFFFF"
            strokeWidth="6"
            strokeLinecap="round"
          />
        </svg>

        {/* Center marker */}
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-20">
          <div className="w-5 h-5 bg-[#5478FF] rounded-full border-4 border-white shadow-[0_4px_10px_rgba(84,120,255,0.4)] relative">
            <div className="absolute inset-0 bg-[#5478FF] rounded-full animate-ping opacity-75" />
          </div>
        </div>

        {/* Location badge */}
        <div className="absolute top-4 left-1/2 -translate-x-1/2 bg-white/95 backdrop-blur-md px-4 py-2 rounded-full shadow-md border border-gray-100 z-30">
          <span
            className="text-[13px] text-[#1A1A2E]"
            style={{
              fontFamily: "'Pretendard', sans-serif",
              fontWeight: 700,
            }}
          >
            <MapPin size={12} className="text-[#5478FF] inline mr-1" />
            Jongno
          </span>
        </div>

        {/* TODO banner */}
        <div className="absolute bottom-6 left-4 right-4 bg-white/95 backdrop-blur-md rounded-2xl p-4 shadow-lg border border-gray-100 z-30">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-[#F0F4FF] flex items-center justify-center">
              <MapIcon size={20} color="#5478FF" strokeWidth={2} />
            </div>
            <div>
              <div
                style={{
                  fontFamily: "'Pretendard', sans-serif",
                  fontSize: 14,
                  fontWeight: 700,
                  color: "#1A1A2E",
                }}
              >
                Map View
              </div>
              <div
                style={{
                  fontFamily: "'Pretendard', sans-serif",
                  fontSize: 12,
                  color: "#8892A4",
                }}
              >
                Google / Kakao Map integration coming soon
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}