import { useState, useEffect, useRef, useCallback } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import { RefreshCw, Heart, Flame, BarChart3, Sparkles, Star, UtensilsCrossed, Camera, ShoppingBag, Zap, Leaf, Moon, Compass, MapPin, Target, MessageCircle, RotateCcw } from "lucide-react";

interface MainChatScreenProps {
  selectedCategories: string[];
  onPlaceSelect: (placeId: string) => void;
  onReset?: () => void;
}

type MessageType = {
  id: string;
  sender: "bot" | "user";
  text?: string;
  isTyping?: boolean;
  type?: "mixed-recommendations" | "detailed-recommendations" | "phrase-recommendations" | "survey-q1" | "survey-q2";
  categoryIndex?: number;
  hasAnswered?: boolean;
};

// --- Mock Data ---

const COMMON_CATEGORIES = [
  // 0: Couples
  [
    { id: "p3", image: "https://images.unsplash.com/photo-1617577367443-2d778fedeef4?q=80&w=600&auto=format&fit=crop", name: "Hyundai Card Music Library", nameKr: "현대카드 뮤직 라이브러리", rating: 4.7, distance: "2.5km", tag: "Culture", color: "#5478FF" },
    { id: "p4", image: "https://images.unsplash.com/photo-1517154421773-0529f29ea451?q=80&w=600&auto=format&fit=crop", name: "Seoul Forest", nameKr: "서울숲", rating: 4.8, distance: "4.1km", tag: "Nature", color: "#4CAF50" },
    { id: "p5", image: "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?q=80&w=600&auto=format&fit=crop", name: "Onion Anguk", nameKr: "어니언 안국", rating: 4.6, distance: "800m", tag: "Café", color: "#795548" },
  ],
  // 1: Men 20s
  [
    { id: "dm3", image: "https://images.unsplash.com/photo-1540138279543-b3728f037467?q=80&w=600&auto=format&fit=crop", name: "Gwangjang Market", nameKr: "광장시장", rating: 4.8, distance: "1.5km", tag: "Food", color: "#FF9800" },
    { id: "cm_m1", image: "https://images.unsplash.com/photo-1682090369590-c4c82f3cc065?q=80&w=600&auto=format&fit=crop", name: "Kasina Hannam", nameKr: "카시나 한남", rating: 4.7, distance: "2.8km", tag: "Streetwear", color: "#5478FF" },
    { id: "cm_m2", image: "https://images.unsplash.com/photo-1617577367443-2d778fedeef4?q=80&w=600&auto=format&fit=crop", name: "Anthracite Coffee", nameKr: "앤트러사이트", rating: 4.6, distance: "1.2km", tag: "Café", color: "#795548" },
  ]
];

const DETAILED_CATEGORIES = [
  {
    id: "couple",
    title: "Perfect Dates for Couples",
    btnText: "Show more romantic spots",
    iconType: "heart" as const,
    items: [
      { id: "c1", image: "https://images.unsplash.com/photo-1698210876771-36cb7af6e852?q=80&w=600&auto=format&fit=crop", name: "N Seoul Tower", nameKr: "N서울타워", rating: 4.8, distance: "3.2km", tag: "View", color: "#E91E63" },
      { id: "c2", image: "https://images.unsplash.com/photo-1617577367443-2d778fedeef4?q=80&w=600&auto=format&fit=crop", name: "L'Amant Secret", nameKr: "라망 시크레", rating: 4.9, distance: "1.5km", tag: "Dining", color: "#9C27B0" },
      { id: "cm1", image: "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?q=80&w=600&auto=format&fit=crop", name: "Bukchon Hanok Village", nameKr: "북촌한옥마을", rating: 4.7, distance: "2.1km", tag: "Culture", color: "#FF9800" },
      { id: "cm2", image: "https://images.unsplash.com/photo-1517154421773-0529f29ea451?q=80&w=600&auto=format&fit=crop", name: "Seokchon Lake Park", nameKr: "석촌호수", rating: 4.8, distance: "9.5km", tag: "Nature", color: "#4CAF50" }
    ]
  },
  {
    id: "demo",
    title: "Top Spots for Men in 20s",
    btnText: "Show more for Men in 20s",
    iconType: "flame" as const,
    items: [
      { id: "p1", image: "https://images.unsplash.com/photo-1682090369590-c4c82f3cc065?q=80&w=600&auto=format&fit=crop", name: "Euljiro Brewing", nameKr: "을지로 브루잉", rating: 4.8, distance: "400m", tag: "Nightlife", color: "#9C27B0" },
      { id: "p2", image: "https://images.unsplash.com/photo-1540138279543-b3728f037467?q=80&w=600&auto=format&fit=crop", name: "Myeongdong Kyoja", nameKr: "명동교자", rating: 4.9, distance: "1.2km", tag: "Food", color: "#FF6B6B" },
      { id: "dm2", image: "https://images.unsplash.com/photo-1588668214407-6ea9a6d8c272?q=80&w=600&auto=format&fit=crop", name: "Hongdae Street", nameKr: "홍대거리", rating: 4.4, distance: "900m", tag: "Entertainment", color: "#3F51B5" }
    ]
  }
];

const KOREAN_PHRASES = [
  { id: "kr1", ko: "안녕하세요", en: "Hello", pronunciation: "An-nyeong-ha-se-yo", image: "https://images.unsplash.com/photo-1698210876771-36cb7af6e852?q=80&w=600&auto=format&fit=crop" },
  { id: "kr2", ko: "감사합니다", en: "Thank you", pronunciation: "Gam-sa-ham-ni-da", image: "https://images.unsplash.com/photo-1617577367443-2d778fedeef4?q=80&w=600&auto=format&fit=crop" },
  { id: "kr3", ko: "얼마예요?", en: "How much is it?", pronunciation: "Eol-ma-ye-yo?", image: "https://images.unsplash.com/photo-1540138279543-b3728f037467?q=80&w=600&auto=format&fit=crop" }
];

const delay = (ms: number) => new Promise(res => setTimeout(res, ms));

/* ── Snap breakpoints (% from top) ── */
const SNAP_MAP = 78;
const SNAP_SPLIT = 32;
const SNAP_CHAT = 8;
function clamp(v: number, min: number, max: number) { return Math.min(Math.max(v, min), max); }
function nearestSnap(pct: number): number {
  const snaps = [SNAP_CHAT, SNAP_SPLIT, SNAP_MAP];
  let best = snaps[0], bestDist = Math.abs(pct - snaps[0]);
  for (const s of snaps) { const d = Math.abs(pct - s); if (d < bestDist) { best = s; bestDist = d; } }
  return best;
}

const CAT_ICONS: Record<string, React.ReactNode> = {
  heart: <Heart size={14} className="text-[#E91E63]" />,
  flame: <Flame size={14} className="text-[#FF6B00]" />,
};

function PlaceCard({ place, onClick }: { place: any, onClick: () => void }) {
  return (
    <div
      onClick={onClick}
      className="flex-shrink-0 w-[160px] snap-start cursor-pointer bg-white rounded-[16px] shadow-[0_4px_12px_rgba(84,120,255,0.08)] border border-[rgba(84,120,255,0.1)] overflow-hidden transition-transform hover:scale-[1.02]"
    >
      <div className="w-full aspect-square relative bg-gray-100">
        <img src={place.image} alt={place.name} className="w-full h-full object-cover" />
        <div className="absolute top-2 right-2 bg-black/60 backdrop-blur-sm text-white text-[11px] font-bold px-2 py-1 rounded-full flex items-center gap-1 shadow-sm">
          <Star size={10} fill="#FFDE42" className="text-[#FFDE42]" /> {place.rating}
        </div>
      </div>
      <div className="p-3">
        <div className="font-bold text-[14px] text-[#1A1A2E] truncate">
          {place.name}
        </div>
        <div className="text-[12px] text-[#8892A4] mt-0.5 truncate">
          {place.nameKr}
        </div>
        <div className="flex items-center justify-between mt-2.5">
          <span className="text-[11px] font-semibold text-[#5478FF] bg-[#F0F4FF] px-2 py-1 rounded-md">
            #{place.tag}
          </span>
          <span className="text-[12px] text-[#8892A4] font-medium flex items-center gap-1">
            <MapPin size={10} strokeWidth={2.5} />
            {place.distance}
          </span>
        </div>
      </div>
    </div>
  );
}

function PhraseCard({ phrase }: { phrase: any }) {
  return (
    <div className="flex-shrink-0 w-[160px] snap-start bg-white rounded-[16px] shadow-[0_4px_12px_rgba(84,120,255,0.08)] border border-[rgba(84,120,255,0.1)] overflow-hidden relative group">
      <div className="w-full aspect-square relative">
        <img src={phrase.image} alt={phrase.ko} className="w-full h-full object-cover opacity-80 brightness-50 transition-all group-hover:scale-105" />
        <div className="absolute inset-0 flex flex-col items-center justify-center p-3 text-center">
          <div className="text-[24px] font-bold text-white drop-shadow-md mb-1">
            {phrase.ko}
          </div>
          <div className="text-[13px] font-medium text-white/90 drop-shadow-sm bg-black/30 px-2 py-0.5 rounded-full">
            {phrase.pronunciation}
          </div>
        </div>
      </div>
      <div className="p-3 bg-white text-center">
        <div className="text-[14px] font-bold text-[#1A1A2E]">
          {phrase.en}
        </div>
        <button className="mt-2 w-full flex items-center justify-center gap-1.5 py-1.5 bg-[#F0F4FF] text-[#5478FF] rounded-lg text-[12px] font-bold hover:bg-[#E0E8FF] transition-colors">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
            <polygon points="5 3 19 12 5 21 5 3"></polygon>
          </svg>
          Listen
        </button>
      </div>
    </div>
  );
}

function SurveyCard({
  msg,
  options,
  onAnswer
}: {
  msg: MessageType;
  options: string[];
  onAnswer: (msgId: string, answer: string) => void;
}) {
  return (
    <div className="w-fit max-w-[92%] mt-1 flex flex-wrap gap-2">
      {options.map(opt => (
        <button
          key={opt}
          onClick={() => !msg.hasAnswered && onAnswer(msg.id, opt)}
          className={`py-2 px-3.5 rounded-full text-[13px] font-semibold transition-all border ${
            msg.hasAnswered 
              ? "opacity-50 cursor-default border-transparent bg-[#F5F7FA] text-[#8892A4]" 
              : "cursor-pointer border-[rgba(84,120,255,0.3)] bg-white text-[#5478FF] hover:bg-[#5478FF] hover:text-white shadow-sm"
          }`}
        >
          {opt}
        </button>
      ))}
    </div>
  );
}

function MockMapBackground({ isMapMode, places }: { isMapMode: boolean; places: any[] }) {
  return (
    <div className={`absolute inset-0 w-full h-full transition-all duration-700 ease-[cubic-bezier(0.2,0.8,0.2,1)] bg-[#F8FAFF] ${isMapMode ? 'opacity-100 scale-100 blur-0' : 'opacity-60 scale-[1.02] blur-sm'}`}>
      <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg" className="absolute inset-0">
        <defs>
          <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
            <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#E2E8F0" strokeWidth="0.5" opacity="0.6" />
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#grid)" />
        <path d="M -50 300 Q 150 400 450 250 L 450 320 Q 150 450 -50 350 Z" fill="#E8F0FE" opacity="0.8" />
        <path d="M 100 -50 L 120 150 Q 140 250 280 300 L 450 330" fill="none" stroke="#FFFFFF" strokeWidth="6" strokeLinecap="round" />
        <path d="M -50 150 Q 100 200 150 200 L 250 100" fill="none" stroke="#FFFFFF" strokeWidth="4" strokeLinecap="round" />
        <path d="M 300 -50 L 250 150 Q 230 250 350 300 L 450 280" fill="none" stroke="#FFFFFF" strokeWidth="5" strokeLinecap="round" />
        <path d="M 50 850 L 150 600 Q 200 500 350 450 L 450 420" fill="none" stroke="#FFFFFF" strokeWidth="7" strokeLinecap="round" />
        {isMapMode && (
          <path d="M 150 200 Q 140 250 280 300" fill="none" stroke="#5478FF" strokeWidth="3" strokeDasharray="6 6" strokeLinecap="round" className="animate-[dash_1s_linear_infinite]" />
        )}
      </svg>
      
      {places.map((p, i) => {
        const top = `${25 + ((i * 37) % 40)}%`;
        const left = `${15 + ((i * 43) % 65)}%`;
        return (
          <div key={p.id} className="absolute flex flex-col items-center animate-bounceIn" style={{ top, left, transition: 'all 0.5s ease' }}>
            <div className={`w-8 h-8 rounded-full flex items-center justify-center bg-white shadow-[0_4px_12px_rgba(0,0,0,0.15)] border-2 z-10 transition-transform ${isMapMode ? 'scale-100' : 'scale-75'}`} style={{ borderColor: p.color }}>
               <img src={p.image} className="w-full h-full object-cover rounded-full p-0.5" alt="" />
            </div>
            {isMapMode && (
              <div className="bg-white/95 px-2.5 py-1 mt-1 rounded-lg text-[11px] font-bold text-[#1A1A2E] shadow-sm backdrop-blur-sm whitespace-nowrap border border-gray-100">
                {p.name}
              </div>
            )}
          </div>
        );
      })}
      
      <div className="absolute top-[60%] left-[50%] -translate-x-1/2 -translate-y-1/2 z-20">
        <div className="w-5 h-5 bg-[#5478FF] rounded-full border-4 border-white shadow-[0_4px_10px_rgba(84,120,255,0.4)] relative">
          <div className="absolute inset-0 bg-[#5478FF] rounded-full animate-ping opacity-75" />
        </div>
      </div>
    </div>
  );
}

export function MainChatScreen({ selectedCategories, onPlaceSelect, onReset }: MainChatScreenProps) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const dragRef = useRef<{ startY: number; startPct: number; isDragging: boolean }>({ startY: 0, startPct: SNAP_CHAT, isDragging: false });
  const [currentCatIndex, setCurrentCatIndex] = useState(0);
  const [sheetPct, setSheetPct] = useState(SNAP_CHAT);
  const [isSnapping, setIsSnapping] = useState(false);
  const [messages, setMessages] = useState<MessageType[]>([
    { 
      id: "1", 
      sender: "bot", 
      text: "Based on your choices, I've found perfect matches for you.", 
      type: "mixed-recommendations",
      categoryIndex: 0
    }
  ]);

  const isMapBig = sheetPct > 55;

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const activePlaces = (() => {
    const all = messages.flatMap(msg => {
      if (msg.type === "mixed-recommendations") return [...COMMON_CATEGORIES[0], ...COMMON_CATEGORIES[1]];
      if (msg.type === "detailed-recommendations") {
        return DETAILED_CATEGORIES[msg.categoryIndex || 0].items;
      }
      return [];
    });
    const seen = new Set<string>();
    return all.filter(p => { if (seen.has(p.id)) return false; seen.add(p.id); return true; });
  })();

  /* ── Drag handlers ── */
  const getH = () => containerRef.current?.getBoundingClientRect().height ?? 1;
  const onDragStart = useCallback((y: number) => { dragRef.current = { startY: y, startPct: sheetPct, isDragging: true }; setIsSnapping(false); }, [sheetPct]);
  const onDragMove = useCallback((y: number) => { if (!dragRef.current.isDragging) return; const dPct = ((y - dragRef.current.startY) / getH()) * 100; setSheetPct(clamp(dragRef.current.startPct + dPct, SNAP_CHAT - 2, SNAP_MAP + 2)); }, []);
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

  const handleAnswerQ1 = async (msgId: string, answer: string) => {
    setMessages(prev => prev.map(m => m.id === msgId ? { ...m, hasAnswered: true } : m));
    setMessages(prev => [...prev, { id: `u-${Date.now()}`, sender: "user", text: answer }]);
    await delay(600);
    setMessages(prev => [...prev, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
    await delay(1200);
    setMessages(prev => {
      const filtered = prev.filter(m => !m.isTyping);
      return [...filtered, { id: `b-${Date.now()}`, sender: "bot", text: "Got it! And what kind of vibe are you looking for?", type: "survey-q2" }];
    });
  };

  const handleAnswerQ2 = async (msgId: string, answer: string) => {
    setMessages(prev => prev.map(m => m.id === msgId ? { ...m, hasAnswered: true } : m));
    setMessages(prev => [...prev, { id: `u-${Date.now()}`, sender: "user", text: answer }]);
    await delay(600);
    setMessages(prev => [...prev, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
    await delay(1200);
    setMessages(prev => {
      const filtered = prev.filter(m => !m.isTyping);
      return [...filtered, { id: `b-${Date.now()}`, sender: "bot", text: "Updating your preference vector..." }];
    });
    await delay(1000);
    setMessages(prev => [...prev, { id: `t2-${Date.now()}`, sender: "bot", isTyping: true }]);
    await delay(1500);
    
    const nextIndex = (currentCatIndex + 1) % DETAILED_CATEGORIES.length;
    setCurrentCatIndex(nextIndex);

    setMessages(prev => {
      const filtered = prev.filter(m => !m.isTyping);
      return [...filtered, { 
        id: `b2-${Date.now()}`, 
        sender: "bot", 
        text: "All set! Based on your new preferences, here are the places that are a perfect fit for you right now:", 
        type: "mixed-recommendations",
        categoryIndex: nextIndex
      }];
    });
  };

  const handleRequestDetailed = async (index: number) => {
    const category = DETAILED_CATEGORIES[index];
    setMessages(prev => [...prev, { id: `u-${Date.now()}`, sender: "user", text: category.btnText }]);
    await delay(600);
    setMessages(prev => [...prev, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
    await delay(1200);
    setMessages(prev => {
      const filtered = prev.filter(m => !m.isTyping);
      return [...filtered, { 
        id: `b-${Date.now()}`, 
        sender: "bot", 
        text: `Here are more spots from ${category.title}:`, 
        type: "detailed-recommendations", 
        categoryIndex: index 
      }];
    });
  };

  const handleRequestPhrases = async () => {
    setMessages(prev => [...prev, { id: `u-${Date.now()}`, sender: "user", text: "Useful Korean Phrases" }]);
    await delay(600);
    setMessages(prev => [...prev, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
    await delay(1200);
    setMessages(prev => {
      const filtered = prev.filter(m => !m.isTyping);
      return [...filtered, { 
        id: `b-${Date.now()}`, 
        sender: "bot", 
        text: "Here are some essential Korean phrases you can use at these places!", 
        type: "phrase-recommendations" 
      }];
    });
  };

  const handleRefreshRecommendations = async () => {
    // Prevent multiple clicks if bot is typing
    if (messages[messages.length - 1]?.isTyping) return;

    setMessages(prev => [...prev, { id: `u-${Date.now()}`, sender: "user", text: "Find other places" }]);
    await delay(600);
    setMessages(prev => [...prev, { id: `t-${Date.now()}`, sender: "bot", isTyping: true }]);
    await delay(1200);
    setMessages(prev => {
      const filtered = prev.filter(m => !m.isTyping);
      return [...filtered, { 
        id: `b-${Date.now()}`, 
        sender: "bot", 
        text: "Let's update your preference vector!\n\nFirst, what's your main focus today?", 
        type: "survey-q1" 
      }];
    });
  };

  const renderMessageContent = (msg: MessageType) => {
    if (msg.type === "mixed-recommendations" && msg.categoryIndex !== undefined) {
      return (
        <div className="w-full mt-2 flex flex-col relative">
          {/* Section 1: Big-data / Interest-based */}
          <div className="mb-4">
            <div className="flex items-center gap-2 mb-2.5">
              <BarChart3 size={16} className="text-[#5478FF]" />
              <span className="text-[15px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>Based on Your Picks</span>
              <span className="text-[11px] text-[#8892A4] bg-[#F5F7FA] px-2 py-0.5 rounded-md" style={{ fontWeight: 600 }}>Big Data</span>
            </div>
            <div className="flex gap-3 overflow-x-auto hide-scrollbar snap-x snap-mandatory pb-3 pt-1 px-1 -mx-1">
              {COMMON_CATEGORIES[0].map((place) => (
                <PlaceCard key={place.id} place={place} onClick={() => onPlaceSelect(place.id)} />
              ))}
            </div>
            <button
              onClick={() => handleRequestDetailed(0)}
              className="mt-2 py-2 px-3.5 bg-white border border-[rgba(84,120,255,0.2)] rounded-full text-[13px] font-semibold text-[#5478FF] shadow-sm hover:bg-[#5478FF] hover:text-white transition-all flex items-center gap-1.5"
            >
              {CAT_ICONS[DETAILED_CATEGORIES[0].iconType]}
              {DETAILED_CATEGORIES[0].btnText}
            </button>
          </div>

          {/* Section 2: Personalized / Demographic-based */}
          <div className="mb-3">
            <div className="flex items-center gap-2 mb-2.5">
              <Sparkles size={16} className="text-[#FFAA00]" />
              <span className="text-[15px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>Personalized for You</span>
              <span className="text-[11px] text-[#5478FF] bg-[#F0F4FF] px-2 py-0.5 rounded-md" style={{ fontWeight: 600 }}>Male · 20s</span>
            </div>
            <div className="flex gap-3 overflow-x-auto hide-scrollbar snap-x snap-mandatory pb-3 pt-1 px-1 -mx-1">
              {COMMON_CATEGORIES[1].map((place) => (
                <PlaceCard key={place.id} place={place} onClick={() => onPlaceSelect(place.id)} />
              ))}
            </div>
            <button
              onClick={() => handleRequestDetailed(1)}
              className="mt-2 py-2 px-3.5 bg-white border border-[rgba(84,120,255,0.2)] rounded-full text-[13px] font-semibold text-[#5478FF] shadow-sm hover:bg-[#5478FF] hover:text-white transition-all flex items-center gap-1.5"
            >
              {CAT_ICONS[DETAILED_CATEGORIES[1].iconType]}
              {DETAILED_CATEGORIES[1].btnText}
            </button>
          </div>
        </div>
      );
    }

    if (msg.type === "detailed-recommendations" && msg.categoryIndex !== undefined) {
      const cat = DETAILED_CATEGORIES[msg.categoryIndex];
      return (
        <div className="w-full mt-2 relative">
          <div className="flex gap-3 overflow-x-auto hide-scrollbar snap-x snap-mandatory pb-3 pt-1 px-1 -mx-1">
            {cat.items.map((place) => (
              <PlaceCard key={place.id} place={place} onClick={() => onPlaceSelect(place.id)} />
            ))}
          </div>
          <button
            onClick={() => handleRequestDetailed(msg.categoryIndex!)}
            className="mt-2 py-2 px-3.5 bg-white border border-[rgba(84,120,255,0.2)] rounded-full text-[13px] font-semibold text-[#5478FF] shadow-sm hover:bg-[#5478FF] hover:text-white transition-all flex items-center gap-1.5"
          >
            {CAT_ICONS[cat.iconType]}
            {cat.btnText}
          </button>
        </div>
      );
    }

    if (msg.type === "phrase-recommendations") {
      return (
        <div className="w-full mt-2 relative">
          <div className="flex gap-3 overflow-x-auto hide-scrollbar snap-x snap-mandatory pb-3 pt-1 px-1 -mx-1">
            {KOREAN_PHRASES.map((phrase) => (
              <PhraseCard key={phrase.id} phrase={phrase} />
            ))}
          </div>
        </div>
      );
    }

    if (msg.type === "survey-q1") {
      return <SurveyCard msg={msg} options={["Local Food & Cafe", "Photo Spots & Landmarks", "Shopping & Trends"]} onAnswer={handleAnswerQ1} />;
    }

    if (msg.type === "survey-q2") {
      return <SurveyCard msg={msg} options={["Active & Bustling", "Calm & Healing", "Nightlife"]} onAnswer={handleAnswerQ2} />;
    }

    return null;
  };

  return (
    <div ref={containerRef} className="relative w-full h-full overflow-hidden bg-[#FAFBFF] animate-screenSwitch font-['Pretendard']">
      
      {/* ── 1. Map Layer (full bleed) ── */}
      <MockMapBackground isMapMode={isMapBig} places={activePlaces} />

      {/* ── 2. Header (always on top) ── */}
      <div className={`absolute top-0 left-0 right-0 z-50 transition-all duration-400 flex items-center justify-between px-5 py-3 ${isMapBig ? "bg-transparent" : "bg-white/80 backdrop-blur-xl border-b border-[rgba(84,120,255,0.1)]"}`}>
        <div className={`flex items-center gap-3 transition-opacity duration-300 ${isMapBig ? "opacity-0 pointer-events-none" : "opacity-100"}`}>
          <div className="relative">
            <img src={mascotImg} alt="Mascot" className="w-10 h-10 rounded-full border-2 border-[#5478FF] object-cover bg-[#EEF2FF] shadow-sm" />
            <div className="absolute bottom-0 right-0 w-3 h-3 bg-[#4CAF50] border-2 border-white rounded-full" />
          </div>
          <div>
            <div className="font-bold text-[16px] text-[#1A1A2E] leading-tight">Oh My Guide</div>
            <div className="font-medium text-[12px] text-[#8892A4]">Curating spots...</div>
          </div>
        </div>
        {/* Reset button (right side) */}
        {!isMapBig && onReset && (
          <button
            onClick={onReset}
            className="flex items-center gap-1.5 px-3 py-2 rounded-full bg-[#F5F7FA] hover:bg-[#E8ECF2] text-[#8892A4] hover:text-[#1A1A2E] transition-all active:scale-95"
            title="Change location"
          >
            <RotateCcw size={14} strokeWidth={2.5} />
            <span className="text-[12px] font-semibold">Reset</span>
          </button>
        )}
        {/* Location badge visible when map is big */}
        {isMapBig && (
          <div className="mx-auto bg-white/95 backdrop-blur-md px-4 py-2 rounded-full shadow-md border border-gray-100 flex items-center gap-1.5">
            <MapPin size={12} className="text-[#5478FF]" />
            <span className="text-[12px] font-bold text-[#1A1A2E]">Jongno</span>
          </div>
        )}
      </div>

      {/* ══ 3. Draggable Bottom Sheet ══ */}
      <div
        className={`absolute left-0 right-0 bottom-0 z-40 flex flex-col bg-white rounded-t-[24px] shadow-[0_-8px_30px_rgba(0,0,0,0.12)] ${isSnapping ? "transition-[top] duration-400 ease-[cubic-bezier(0.32,0.72,0,1)]" : ""}`}
        style={{ top: `${sheetPct}%` }}
      >
        {/* Drag handle */}
        <div
          className="flex-shrink-0 cursor-grab active:cursor-grabbing select-none touch-none"
          onTouchStart={handleTouchStart}
          onTouchMove={handleTouchMove}
          onTouchEnd={handleTouchEnd}
          onMouseDown={handleMouseDown}
        >
          <div className="flex justify-center pt-3 pb-1">
            <div className="w-10 h-1 bg-gray-300 rounded-full" />
          </div>
          <div className="flex items-center justify-between px-5 pb-3 pt-1">
            <div className="flex items-center gap-1.5">
              <MapPin size={14} className="text-[#5478FF]" />
              <span className="text-[13px] font-bold text-[#1A1A2E]">Near Jongno</span>
            </div>
            <span className="text-[11px] text-[#8892A4] font-medium">{activePlaces.length} spots</span>
          </div>
        </div>

        <div className="h-[1px] bg-gray-100 mx-4" />

        {/* Chat scroll */}
        <div
          ref={scrollRef}
          className="flex-1 overflow-y-auto overflow-x-hidden hide-scrollbar px-4 flex flex-col pb-28 pt-4"
        >
          {messages.map((msg, idx) => {
            const isBot = msg.sender === 'bot';
            const isNextSameSender = messages[idx + 1]?.sender === msg.sender;
            const marginClass = isNextSameSender ? "mb-1.5" : "mb-5";

            if (isBot) {
              const isFirst = idx === 0;
              return (
                <div key={msg.id} className={`flex flex-col w-full animate-fadeUp gap-1.5 ${marginClass}`}>
                  {isFirst && (
                    <div className="flex items-center gap-2 mb-1 ml-1">
                      <img src={mascotImg} className="w-7 h-7 rounded-full border-[1.5px] border-[#5478FF] shadow-sm object-cover bg-[#EEF2FF]" alt="Bot"/>
                      <span className="font-bold text-[13px] text-[#1A1A2E] tracking-wide">Oh My Guide</span>
                    </div>
                  )}

                  {(msg.text || msg.isTyping) && (
                    <div className="w-fit max-w-[85%] bg-white rounded-[4px_16px_16px_16px] px-4 py-3 shadow-sm border border-[rgba(84,120,255,0.15)]">
                      {msg.isTyping ? (
                        <div className="flex items-center gap-1.5 h-5 px-1">
                          <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70 [animation-delay:-0.3s]" />
                          <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70 [animation-delay:-0.15s]" />
                          <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70" />
                        </div>
                      ) : (
                        <div className="text-[14.5px] font-medium text-[#1A1A2E] leading-relaxed whitespace-pre-line">
                          {msg.text}
                        </div>
                      )}
                    </div>
                  )}

                  {msg.type && (
                    <div className="w-full">
                      {renderMessageContent(msg)}
                    </div>
                  )}
                </div>
              );
            } else {
              return (
                <div key={msg.id} className={`flex justify-end w-full animate-fadeUp ${marginClass}`}>
                  <div className="bg-[#5478FF] rounded-[16px_4px_16px_16px] px-4 py-3 shadow-md max-w-[85%]">
                    <div className="text-[14.5px] font-semibold text-white leading-relaxed tracking-wide">
                      {msg.text}
                    </div>
                  </div>
                </div>
              );
            }
          })}
        </div>

        {/* Refresh Button */}
        <div className="absolute bottom-0 left-0 right-0 px-4 pt-10 pb-6 bg-gradient-to-t from-white via-white/90 to-transparent pointer-events-none z-20">
          <button
            onClick={handleRefreshRecommendations}
            className="w-full bg-[#FFDE42] hover:bg-[#F2D030] text-[#1A1A2E] font-bold text-[15px] py-3.5 rounded-2xl shadow-[0_6px_20px_rgba(255,222,66,0.35)] flex items-center justify-center gap-2 pointer-events-auto transition-transform active:scale-[0.98]"
          >
            <Compass size={18} strokeWidth={2.5} />
            Find other places
          </button>
        </div>
      </div>
    </div>
  );
}