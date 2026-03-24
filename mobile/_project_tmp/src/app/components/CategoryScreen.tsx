import { useState, useEffect, useRef } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import {
  Mountain, Landmark, Sparkles, Route, Dumbbell,
  Coffee, ShoppingBag, UtensilsCrossed
} from "lucide-react";

interface CategoryScreenProps {
  onConfirm: (selected: string[]) => void;
}

const CATEGORY_ICONS: Record<string, React.ElementType> = {
  attraction: Mountain,
  culture: Landmark,
  festival: Sparkles,
  course: Route,
  leports: Dumbbell,
  cafe: Coffee,
  shopping: ShoppingBag,
  food: UtensilsCrossed,
};

const CATEGORIES = [
  { id: "attraction", name: "Attraction", sub: "Landmarks & nature", color: "#4CAF50" },
  { id: "culture", name: "Culture", sub: "Museums & history", color: "#5478FF" },
  { id: "festival", name: "Festival", sub: "Events & performances", color: "#E91E63" },
  { id: "course", name: "Course", sub: "Travel routes", color: "#00BCD4" },
  { id: "leports", name: "Leports", sub: "Leisure & sports", color: "#FF9800" },
  { id: "cafe", name: "Cafes", sub: "Coffee & bakeries", color: "#8D6E63" },
  { id: "shopping", name: "Shopping", sub: "Markets & malls", color: "#9C27B0" },
  { id: "food", name: "Food", sub: "Dining & street eats", color: "#FF6B6B" },
];

type MessageType = {
  id: string;
  sender: 'bot' | 'user';
  text?: string;
  isTyping?: boolean;
  hasCategories?: boolean;
};

const delay = (ms: number) => new Promise(res => setTimeout(res, ms));

export function CategoryScreen({ onConfirm }: CategoryScreenProps) {
  const [gpsState, setGpsState] = useState<"detecting" | "found">("detecting");
  const [messages, setMessages] = useState<MessageType[]>([]);
  const [selected, setSelected] = useState<string[]>([]);
  const [canSelect, setCanSelect] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, selected]);

  useEffect(() => {
    const initChat = async () => {
      // Start GPS detection in parallel
      setTimeout(() => setGpsState("found"), 1800);

      await delay(800);
      setMessages([{ id: 't1', sender: 'bot', isTyping: true }]);
      
      await delay(1200);
      setMessages([
        { id: 'm1', sender: 'bot', text: "Hey! I'm Oh My Guide\nI'll find the perfect spots near you right now!" }
      ]);

      await delay(600);
      setMessages(prev => [...prev, { id: 't2', sender: 'bot', isTyping: true }]);
      
      await delay(1400);
      setMessages(prev => [
        ...prev.filter(m => !m.isTyping),
        { id: 'm2', sender: 'bot', text: "Just tell me what you're into", hasCategories: true }
      ]);
      setCanSelect(true);
    };
    initChat();
  }, []);

  const toggle = (id: string) => {
    if (!canSelect) return;
    setSelected((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);
  };

  const handleSend = async () => {
    if (selected.length === 0 || !canSelect) return;
    setCanSelect(false); // Lock selections

    // Format selected categories as a message
    const userText = selected.map(id => {
      const cat = CATEGORIES.find(c => c.id === id);
      return cat ? `${cat.name}` : "";
    }).join(",  ");

    setMessages(prev => [...prev, { id: `u-${Date.now()}`, sender: 'user', text: userText }]);

    await delay(500);
    setMessages(prev => [...prev, { id: 't3', sender: 'bot', isTyping: true }]);

    await delay(1500);
    setMessages(prev => [
      ...prev.filter(m => !m.isTyping),
      { id: 'm3', sender: 'bot', text: "Awesome picks! Let's find the best spots!" }
    ]);

    await delay(1500);
    onConfirm(selected);
  };

  return (
    <div
      className="flex-1 flex flex-col overflow-hidden animate-screenSwitch relative"
      style={{
        background: "linear-gradient(175deg, #EEF2FF 0%, #F5F8FF 35%, #FFFFFF 100%)",
      }}
    >
      {/* ── Floating GPS Header ── */}
      <div
        className="absolute top-0 w-full z-20"
        style={{ padding: "12px 20px 0", display: "flex", justifyContent: "center" }}
      >
        <div
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: 10,
            padding: "9px 18px",
            borderRadius: 100,
            background: "rgba(255,255,255,0.92)",
            backdropFilter: "blur(16px)",
            boxShadow:
              gpsState === "found"
                ? "0 6px 24px rgba(84,120,255,0.14), 0 2px 8px rgba(0,0,0,0.06)"
                : "0 4px 16px rgba(84,120,255,0.10), 0 1px 4px rgba(0,0,0,0.05)",
            border: `1px solid ${gpsState === "found" ? "rgba(84,120,255,0.16)" : "rgba(200,210,255,0.5)"}`,
            transition: "all 0.6s ease",
            minWidth: 200,
          }}
        >
          <div className="flex items-center gap-2">
            {gpsState === "detecting" ? (
              <>
                <div style={{ position: "relative", width: 10, height: 10, flexShrink: 0 }}>
                  <div
                    className="animate-ping"
                    style={{ position: "absolute", inset: 0, borderRadius: "50%", background: "rgba(84,120,255,0.35)" }}
                  />
                  <div style={{ position: "absolute", inset: 1, borderRadius: "50%", background: "#5478FF" }} />
                </div>
                <span style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 12, fontWeight: 500, color: "#6B7ECC" }}>
                  Locating you...
                </span>
              </>
            ) : (
              <>
                <svg width="13" height="15" viewBox="0 0 14 17" fill="none">
                  <path d="M7 0C4.24 0 2 2.24 2 5c0 3.75 5 10 5 10s5-6.25 5-10c0-2.76-2.24-5-5-5z" fill="#5478FF" />
                  <circle cx="7" cy="5" r="2" fill="white" />
                </svg>
                <div style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 13, fontWeight: 600, color: "#1A1A2E", lineHeight: 1 }}>
                  You're at Jongno · <span style={{ color: "#5478FF" }}>종로</span>
                </div>
              </>
            )}
          </div>
          {gpsState === "found" && (
            <>
              <div style={{ width: 1, height: 20, background: "rgba(84,120,255,0.15)", flexShrink: 0 }} />
              <div className="flex items-center gap-1.5">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#FF9800" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="5" fill="#FF9800" fillOpacity="0.2" /><line x1="12" y1="1" x2="12" y2="3" /><line x1="12" y1="21" x2="12" y2="23" /><line x1="4.22" y1="4.22" x2="5.64" y2="5.64" /><line x1="18.36" y1="18.36" x2="19.78" y2="19.78" /><line x1="1" y1="12" x2="3" y2="12" /><line x1="21" y1="12" x2="23" y2="12" /><line x1="4.22" y1="19.78" x2="5.64" y2="18.36" /><line x1="18.36" y1="5.64" x2="19.78" y2="4.22" /></svg>
                <span style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 13, fontWeight: 600, color: "#FF9800" }}>24°C</span>
              </div>
            </>
          )}
        </div>
      </div>

      {/* ── Scrollable Chat Area ── */}
      <div className="flex-1 overflow-y-auto hide-scrollbar pt-[90px] px-4 pb-6 flex flex-col gap-5">
        {messages.map((msg, idx) => {
          const isBot = msg.sender === 'bot';
          const showAvatar = isBot && (idx === 0 || messages[idx - 1].sender !== 'bot');

          if (isBot) {
            return (
              <div key={msg.id} className="flex gap-3 max-w-[95%] animate-fadeUp">
                {/* Avatar */}
                <div className="w-10 flex-shrink-0 flex justify-center">
                  {showAvatar && (
                    <div className="relative">
                      <div className="absolute inset-0 rounded-full bg-blue-100 animate-pulseRing" />
                      <img 
                        src={mascotImg} 
                        className="relative z-10 w-10 h-10 rounded-full border-[2.5px] border-[#5478FF] shadow-[0_4px_12px_rgba(84,120,255,0.25)] object-cover bg-[#EEF2FF]" 
                        alt="Bot"
                      />
                    </div>
                  )}
                </div>

                <div className="flex flex-col gap-2 min-w-0 flex-1">
                  {/* Text Bubble */}
                  <div className="bg-white rounded-[18px_18px_18px_5px] px-4 py-3 shadow-[0_4px_16px_rgba(84,120,255,0.08)] border border-[rgba(84,120,255,0.12)] self-start w-fit">
                    {msg.isTyping ? (
                      <div className="flex items-center gap-1.5 h-5 px-1">
                        <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70 [animation-delay:-0.3s]" />
                        <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70 [animation-delay:-0.15s]" />
                        <div className="w-1.5 h-1.5 bg-[#5478FF] rounded-full animate-bounce opacity-70" />
                      </div>
                    ) : (
                      <div className="font-['Pretendard'] text-[14px] text-[#1A1A2E] leading-relaxed whitespace-pre-line">
                        {msg.text}
                      </div>
                    )}
                  </div>

                  {/* Categories Grid (Inline inside Chat) */}
                  {msg.hasCategories && (
                    <div className="grid grid-cols-2 gap-2 mt-1 w-full max-w-[280px]">
                      {CATEGORIES.map((cat, i) => {
                        const isSelected = selected.includes(cat.id);
                        return (
                          <div
                            key={cat.id}
                            onClick={() => toggle(cat.id)}
                            className="animate-slideUp"
                            style={{
                              cursor: canSelect ? "pointer" : "default",
                              opacity: canSelect || isSelected ? 1 : 0.65,
                              borderRadius: 16,
                              border: `2px solid ${isSelected ? cat.color : "#E8ECF4"}`,
                              background: isSelected ? `${cat.color}0D` : "#fff",
                              boxShadow: isSelected
                                ? `0 4px 16px ${cat.color}25`
                                : "0 2px 8px rgba(0,0,0,0.03)",
                              transform: isSelected && canSelect ? "scale(1.03)" : "scale(1)",
                              transition: "all 0.25s cubic-bezier(0.34,1.2,0.64,1)",
                              animationDelay: `${i * 0.06}s`,
                              padding: "12px",
                            }}
                          >
                            {/* Top row: emoji + check */}
                            <div className="flex items-start justify-between">
                              <div
                                style={{
                                  width: 36,
                                  height: 36,
                                  borderRadius: 12,
                                  background: isSelected ? `${cat.color}22` : `${cat.color}12`,
                                  display: "flex",
                                  alignItems: "center",
                                  justifyContent: "center",
                                  fontSize: 18,
                                  transition: "background 0.2s",
                                }}
                              >
                                {(() => { const Icon = CATEGORY_ICONS[cat.id]; return Icon ? <Icon size={18} color={cat.color} strokeWidth={2.5} fill={cat.color} fillOpacity={isSelected ? 0.2 : 0.1} /> : null; })()}
                              </div>
                              <div
                                style={{
                                  width: 20,
                                  height: 20,
                                  borderRadius: "50%",
                                  border: isSelected ? "none" : "2px solid #D6DCEB",
                                  background: isSelected
                                    ? "linear-gradient(135deg, #325BFF, #7C98FF)"
                                    : "transparent",
                                  display: "flex",
                                  alignItems: "center",
                                  justifyContent: "center",
                                  flexShrink: 0,
                                  transition: "all 0.2s",
                                  boxShadow: isSelected ? "0 2px 8px rgba(84,120,255,0.35)" : "none",
                                }}
                              >
                                {isSelected && (
                                  <svg width="10" height="10" viewBox="0 0 12 12" fill="none">
                                    <path d="M2.5 6L5 8.5L9.5 3.5" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                                  </svg>
                                )}
                              </div>
                            </div>

                            {/* Text */}
                            <div style={{ marginTop: 8 }}>
                              <div
                                style={{
                                  fontFamily: "'Pretendard', sans-serif",
                                  fontSize: 13,
                                  fontWeight: 600,
                                  color: isSelected ? cat.color : "#1A1A2E",
                                  transition: "color 0.2s",
                                  lineHeight: 1.2,
                                }}
                              >
                                {cat.name}
                              </div>
                              <div
                                style={{
                                  fontFamily: "'Pretendard', sans-serif",
                                  fontSize: 10,
                                  color: "#9AA5B4",
                                  marginTop: 3,
                                  lineHeight: 1.3,
                                }}
                              >
                                {cat.sub}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              </div>
            );
          } else {
            // User Message
            return (
              <div key={msg.id} className="flex justify-end w-full animate-fadeUp">
                <div className="bg-gradient-to-r from-[#325BFF] to-[#7C98FF] rounded-[18px_18px_5px_18px] px-4 py-3.5 shadow-[0_6px_20px_rgba(84,120,255,0.25)] max-w-[85%]">
                  <div className="font-['Pretendard'] text-[14.5px] font-medium text-white leading-relaxed tracking-wide">
                    {msg.text}
                  </div>
                </div>
              </div>
            );
          }
        })}
        <div ref={messagesEndRef} className="h-2" />
      </div>

      {/* ── Chat Input Bottom Bar ── */}
      <div 
        className="flex-shrink-0 bg-white/95 backdrop-blur-md border-t border-[rgba(84,120,255,0.08)] p-3 pb-5 transition-transform duration-300"
        style={{ transform: canSelect || selected.length > 0 ? 'translateY(0)' : 'translateY(100%)', opacity: canSelect || selected.length > 0 ? 1 : 0 }}
      >
        <div className="flex items-center gap-2 bg-[#F5F8FF] p-1.5 pl-4 rounded-[26px] border border-[rgba(84,120,255,0.15)] shadow-inner">
          <div className="flex-1 overflow-x-auto hide-scrollbar flex items-center h-[38px]">
            {selected.length === 0 ? (
              <span className="text-[#A0AABF] font-['Pretendard'] text-[14.5px]">Tap categories above...</span>
            ) : (
              <div className="flex gap-1.5">
                {selected.map(id => {
                  const cat = CATEGORIES.find(c => c.id === id);
                  return (
                    <span 
                      key={id} 
                      className="bg-white px-3 py-1.5 rounded-full shadow-sm font-['Pretendard'] text-[13px] whitespace-nowrap flex items-center gap-1.5"
                      style={{ border: `1px solid ${cat ? cat.color + '25' : 'rgba(84,120,255,0.1)'}`, color: cat?.color || '#5478FF' }}
                    >
                        {(() => { const Icon = cat ? CATEGORY_ICONS[cat.id] : null; return Icon ? <Icon size={13} color={cat?.color || '#5478FF'} strokeWidth={2.5} fill={cat?.color || '#5478FF'} fillOpacity={0.15} /> : null; })()}
                      {cat?.name}
                    </span>
                  );
                })}
              </div>
            )}
          </div>
          
          <button
            onClick={handleSend}
            disabled={selected.length === 0 || !canSelect}
            className="w-[38px] h-[38px] rounded-full flex items-center justify-center flex-shrink-0 transition-all duration-300"
            style={{
              background: selected.length > 0 && canSelect ? "linear-gradient(135deg, #325BFF 0%, #5478FF 100%)" : "#DDE4F5",
              boxShadow: selected.length > 0 && canSelect ? "0 4px 12px rgba(84,120,255,0.35)" : "none",
              cursor: selected.length > 0 && canSelect ? "pointer" : "default",
              transform: selected.length > 0 && canSelect ? "scale(1)" : "scale(0.95)"
            }}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ transform: "translateX(-1px) translateY(1px)" }}>
              <path d="M22 2L11 13" />
              <path d="M22 2L15 22L11 13L2 9L22 2Z" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  );
}