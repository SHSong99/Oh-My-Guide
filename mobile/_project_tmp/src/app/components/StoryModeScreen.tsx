import { useState, useEffect, useRef, useCallback } from "react";
import { Waves, Building2, Sparkles, Leaf, Volume2 } from "lucide-react";

interface StoryPage {
  iconId?: string;
  text: string;
  image?: string;
  imageCaption?: string;
  ambientLabel?: string;
}

interface StoryModeProps {
  poiName: string;
  poiNameKr: string;
  poiIconId: string;
  pages: StoryPage[];
  onClose: () => void;
  onFinish: () => void;
}

const STORY_ICONS: Record<string, { icon: React.ElementType; color: string }> = {
  waves:    { icon: Waves,     color: "#60A5FA" },
  building: { icon: Building2, color: "#F59E0B" },
  sparkles: { icon: Sparkles,  color: "#A78BFA" },
  leaf:     { icon: Leaf,      color: "#4ADE80" },
};

function StoryIcon({ iconId, size = 24 }: { iconId: string; size?: number }) {
  const cfg = STORY_ICONS[iconId];
  if (!cfg) return null;
  const Icon = cfg.icon;
  return (
    <span
      className="inline-flex items-center justify-center rounded-[12px]"
      style={{ width: size + 16, height: size + 16, background: `${cfg.color}22` }}
    >
      <Icon size={size} color={cfg.color} strokeWidth={2} fill={cfg.color} fillOpacity={0.2} />
    </span>
  );
}

/* ── Waveform visualizer ── */
function WaveformVisualizer({ isPlaying }: { isPlaying: boolean }) {
  const bars = 24;
  return (
    <div className="flex items-end gap-[2.5px] h-[22px]">
      {Array.from({ length: bars }).map((_, i) => {
        const baseH = 4 + Math.sin(i * 0.8) * 8 + Math.random() * 6;
        return (
          <div
            key={i}
            className="rounded-full transition-all duration-300"
            style={{
              width: 2.5,
              height: isPlaying ? baseH : 3,
              background: isPlaying
                ? `linear-gradient(to top, #5478FF, #7C98FF)`
                : "#3A4A6B",
              animation: isPlaying ? `waveBar 0.8s ease-in-out ${i * 0.04}s infinite alternate` : "none",
            }}
          />
        );
      })}
    </div>
  );
}

/* ── Text that reveals word-by-word like TTS ── */
function TTSText({ text, isActive, speed = 40 }: { text: string; isActive: boolean; speed?: number }) {
  const [visibleChars, setVisibleChars] = useState(0);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (isActive) {
      setVisibleChars(0);
      intervalRef.current = setInterval(() => {
        setVisibleChars(prev => {
          if (prev >= text.length) {
            if (intervalRef.current) clearInterval(intervalRef.current);
            return text.length;
          }
          return prev + 1;
        });
      }, speed);
    } else {
      setVisibleChars(text.length);
    }
    return () => { if (intervalRef.current) clearInterval(intervalRef.current); };
  }, [text, isActive, speed]);

  const visible = text.slice(0, visibleChars);
  const hidden = text.slice(visibleChars);

  return (
    <span>
      <span className="text-[#E8ECF4]">{visible}</span>
      <span className="text-[#E8ECF4]/0">{hidden}</span>
      {visibleChars < text.length && (
        <span className="inline-block w-[2px] h-[1em] bg-[#5478FF] ml-0.5 animate-pulse align-text-bottom" />
      )}
    </span>
  );
}

export function StoryModeScreen({ poiName, poiNameKr, poiIconId, pages, onClose, onFinish }: StoryModeProps) {
  const [currentPage, setCurrentPage] = useState(0);
  const [isPlaying, setIsPlaying] = useState(true);
  const [isEntering, setIsEntering] = useState(true);
  const [isExiting, setIsExiting] = useState(false);
  const [pageTransition, setPageTransition] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  const page = pages[currentPage];
  const totalPages = pages.length;
  const isLastPage = currentPage === totalPages - 1;

  // Entry animation
  useEffect(() => {
    const t = setTimeout(() => setIsEntering(false), 50);
    return () => clearTimeout(t);
  }, []);

  // Auto-scroll to top on page change
  useEffect(() => {
    scrollRef.current?.scrollTo({ top: 0, behavior: "smooth" });
  }, [currentPage]);

  const handleClose = useCallback(() => {
    setIsExiting(true);
    setTimeout(() => onClose(), 400);
  }, [onClose]);

  const handleNext = useCallback(() => {
    if (isLastPage) {
      setIsExiting(true);
      setTimeout(() => onFinish(), 400);
      return;
    }
    setPageTransition(true);
    setTimeout(() => {
      setCurrentPage(p => p + 1);
      setPageTransition(false);
    }, 300);
  }, [isLastPage, onFinish]);

  const handlePrev = useCallback(() => {
    if (currentPage === 0) return;
    setPageTransition(true);
    setTimeout(() => {
      setCurrentPage(p => p - 1);
      setPageTransition(false);
    }, 300);
  }, [currentPage]);

  return (
    <div
      className="absolute inset-0 z-[100] flex flex-col overflow-hidden"
      style={{
        background: "linear-gradient(180deg, #0D1117 0%, #141B2D 40%, #1A1A2E 100%)",
        fontFamily: "'Pretendard', sans-serif",
        opacity: isEntering ? 0 : isExiting ? 0 : 1,
        transform: isEntering ? "translateY(20px)" : isExiting ? "translateY(20px)" : "translateY(0)",
        transition: "opacity 0.4s cubic-bezier(0.4, 0, 0.2, 1), transform 0.4s cubic-bezier(0.4, 0, 0.2, 1)",
      }}
    >
      {/* ── Top Bar ── */}
      <div className="flex-shrink-0 px-5 pt-4 pb-3 flex items-center justify-between relative z-10">
        <button
          onClick={handleClose}
          className="flex items-center gap-1.5 bg-[#1E2A3E] hover:bg-[#2A3A52] px-3.5 py-2 rounded-full transition-colors cursor-pointer"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#8892A4" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
          <span className="text-[12px] text-[#8892A4]" style={{ fontWeight: 600 }}>Map</span>
        </button>

        {/* Progress dots */}
        <div className="flex items-center gap-1.5">
          {pages.map((_, i) => (
            <div
              key={i}
              className="rounded-full transition-all duration-500"
              style={{
                width: i === currentPage ? 20 : 6,
                height: 6,
                background: i === currentPage
                  ? "linear-gradient(90deg, #5478FF, #7C98FF)"
                  : i < currentPage ? "#5478FF" : "#2A3A52",
              }}
            />
          ))}
        </div>

        <span className="text-[12px] text-[#5A6A82]" style={{ fontWeight: 600 }}>
          {currentPage + 1}/{totalPages}
        </span>
      </div>

      {/* ── Scrollable Content ── */}
      <div
        ref={scrollRef}
        className="flex-1 overflow-y-auto hide-scrollbar px-5 pb-32"
        style={{
          opacity: pageTransition ? 0 : 1,
          transform: pageTransition ? "translateX(30px)" : "translateX(0)",
          transition: "opacity 0.25s ease, transform 0.25s ease",
        }}
      >
        {/* POI Header (first page only shows full header) */}
        {currentPage === 0 && (
          <div className="mb-5 mt-2">
            <span className="text-[32px] block mb-2">{poiIconId && <StoryIcon iconId={poiIconId} size={32} />}</span>
            <h1 className="text-[28px] text-white" style={{ fontWeight: 800, lineHeight: 1.15 }}>
              {poiName}
            </h1>
            <p className="text-[14px] text-[#5478FF] mt-1" style={{ fontWeight: 600 }}>{poiNameKr}</p>
          </div>
        )}

        {/* Mini header for subsequent pages */}
        {currentPage > 0 && (
          <div className="mb-4 mt-2 flex items-center gap-2.5">
            <span className="text-[20px]">{poiIconId && <StoryIcon iconId={poiIconId} size={20} />}</span>
            <div>
              <h2 className="text-[16px] text-white" style={{ fontWeight: 700 }}>{poiName}</h2>
              <p className="text-[11px] text-[#5478FF]" style={{ fontWeight: 600 }}>{poiNameKr}</p>
            </div>
          </div>
        )}

        {/* Audio Player Bar */}
        <div className="bg-[#1E2A3E] rounded-2xl p-4 mb-5 border border-[#2A3A52]">
          <div className="flex items-center gap-3.5">
            <button
              onClick={() => setIsPlaying(!isPlaying)}
              className="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 cursor-pointer transition-transform active:scale-90"
              style={{
                background: isPlaying
                  ? "linear-gradient(135deg, #5478FF 0%, #7C98FF 100%)"
                  : "#2A3A52",
                boxShadow: isPlaying ? "0 0 20px rgba(84,120,255,0.4)" : "none",
              }}
            >
              {isPlaying ? (
                <svg width="14" height="14" viewBox="0 0 24 24" fill="white">
                  <rect x="6" y="4" width="4" height="16" rx="1" />
                  <rect x="14" y="4" width="4" height="16" rx="1" />
                </svg>
              ) : (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="white">
                  <polygon points="5 3 19 12 5 21 5 3" />
                </svg>
              )}
            </button>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1.5">
                <span className="text-[11px] text-[#5478FF] uppercase tracking-wider" style={{ fontWeight: 700 }}>
                  {isPlaying ? "Now playing…" : "Paused"}
                </span>
              </div>
              <WaveformVisualizer isPlaying={isPlaying} />
            </div>
          </div>
          {/* Ambient sound label */}
          {page.ambientLabel && (
            <div className="flex items-center gap-2 mt-3 pt-3 border-t border-[#2A3A52]">
              <span className="text-[12px]"><Volume2 size={12} color="#5A6A82" strokeWidth={2.5} /></span>
              <span className="text-[11px] text-[#5A6A82] italic" style={{ fontWeight: 500 }}>
                {page.ambientLabel}
              </span>
            </div>
          )}
        </div>

        {/* Story Image */}
        {page.image && (
          <div className="mb-5 rounded-2xl overflow-hidden shadow-[0_8px_32px_rgba(0,0,0,0.4)] relative">
            <img src={page.image} className="w-full aspect-[16/10] object-cover" alt="" />
            <div className="absolute inset-0 bg-gradient-to-t from-[#0D1117]/60 via-transparent to-transparent" />
            {page.imageCaption && (
              <div className="absolute bottom-0 left-0 right-0 px-4 py-3">
                <span className="text-[11px] text-white/80 bg-black/40 backdrop-blur-sm px-2.5 py-1 rounded-lg" style={{ fontWeight: 600 }}>
                  {page.imageCaption}
                </span>
              </div>
            )}
          </div>
        )}

        {/* Story Text — TTS reveal */}
        <div className="mb-6">
          {page.iconId && currentPage > 0 && (
            <span className="text-[24px] block mb-3">{page.iconId && <StoryIcon iconId={page.iconId} size={24} />}</span>
          )}
          <p className="text-[18px] leading-[1.8]" style={{ fontWeight: 400 }}>
            <TTSText text={page.text} isActive={isPlaying} speed={35} />
          </p>
        </div>
      </div>

      {/* ── Bottom Navigation ── */}
      <div
        className="absolute bottom-0 left-0 right-0 flex-shrink-0 px-5 pb-8 pt-12"
        style={{ background: "linear-gradient(to top, #0D1117 40%, transparent)" }}
      >
        <div className="flex gap-3">
          {currentPage > 0 && (
            <button
              onClick={handlePrev}
              className="w-12 h-12 rounded-2xl bg-[#1E2A3E] border border-[#2A3A52] flex items-center justify-center cursor-pointer transition-colors hover:bg-[#2A3A52]"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8892A4" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M19 12H5M12 19l-7-7 7-7" />
              </svg>
            </button>
          )}
          <button
            onClick={handleNext}
            className="flex-1 py-3.5 rounded-2xl flex items-center justify-center gap-2 cursor-pointer transition-all active:scale-[0.98]"
            style={{
              background: isLastPage
                ? "linear-gradient(135deg, #325BFF 0%, #5478FF 50%, #7C98FF 100%)"
                : "linear-gradient(135deg, #5478FF 0%, #7C98FF 100%)",
              boxShadow: "0 8px 28px rgba(84,120,255,0.35)",
            }}
          >
            <span className="text-[15px] text-white" style={{ fontWeight: 700 }}>
              {isLastPage ? "Back to Guide" : `Next`}
            </span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M5 12h14M12 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>

      {/* ── Keyframes ── */}
      <style>{`
        @keyframes waveBar {
          0% { transform: scaleY(0.4); }
          100% { transform: scaleY(1.2); }
        }
      `}</style>
    </div>
  );
}