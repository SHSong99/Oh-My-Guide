import { useState, useEffect, ReactNode, useRef } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import { Users, UsersRound, Backpack, Heart, Globe } from "lucide-react";

interface GpsPermissionScreenProps {
  onAllow: () => void;
  onSkip: () => void;
}

function GuideBubble({ children, delay = 0 }: { children: ReactNode; delay?: number }) {
  return (
    <div className="flex mb-4 animate-fadeUp" style={{ animationDelay: `${delay}s` }}>
      <div
        style={{
          width: 36,
          height: 36,
          borderRadius: "50%",
          marginRight: 10,
          background: "#E2E8F0",
          overflow: "hidden",
          flexShrink: 0,
          border: "2px solid #FFF",
          boxShadow: "0 2px 8px rgba(0,0,0,0.05)"
        }}
      >
        <img src={mascotImg} alt="Guide" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
      </div>
      <div style={{
        background: "#FFFFFF",
        borderRadius: "18px 18px 18px 4px",
        padding: "14px 18px",
        boxShadow: "0 4px 12px rgba(0,0,0,0.03)",
        maxWidth: "80%",
        fontFamily: "'Pretendard', sans-serif",
        fontSize: 14,
        color: "#1A1A2E",
        lineHeight: 1.5,
      }}>
        {children}
      </div>
    </div>
  );
}

function UserBubble({ children }: { children: ReactNode }) {
  return (
    <div className="flex justify-end mb-4 animate-fadeUp">
      <div style={{
        background: "#5478FF",
        borderRadius: "18px 18px 4px 18px",
        padding: "12px 16px",
        boxShadow: "0 4px 12px rgba(84,120,255,0.2)",
        maxWidth: "80%",
        fontFamily: "'Pretendard', sans-serif",
        fontSize: 14,
        fontWeight: 500,
        color: "#FFFFFF",
      }}>
        {children}
      </div>
    </div>
  );
}

export function GpsPermissionScreen({ onAllow, onSkip }: GpsPermissionScreenProps) {
  const [step, setStep] = useState<"language" | "gender" | "country" | "companion" | "gps">("language");
  const [languageId, setLanguageId] = useState<"en" | "ja" | "zh">("en");
  const [languageLabel, setLanguageLabel] = useState("");
  const [genderLabel, setGenderLabel] = useState("");
  const [countryLabel, setCountryLabel] = useState("");
  const [companionLabel, setCompanionLabel] = useState("");
  const [allowPressed, setAllowPressed] = useState(false);
  const [isCountryExpanded, setIsCountryExpanded] = useState(false);

  const COUNTRIES = [
    { id: "us", flag: "🇺🇸", name: "USA" },
    { id: "jp", flag: "🇯🇵", name: "Japan" },
    { id: "cn", flag: "🇨🇳", name: "China" },
    { id: "tw", flag: "🇹🇼", name: "Taiwan" },
    { id: "hk", flag: "🇭🇰", name: "Hong Kong" },
    { id: "gb", flag: "🇬🇧", name: "UK" },
    { id: "fr", flag: "🇫🇷", name: "France" },
    { id: "de", flag: "🇩🇪", name: "Germany" },
    { id: "it", flag: "🇮🇹", name: "Italy" },
    { id: "es", flag: "🇪🇸", name: "Spain" },
    { id: "ca", flag: "🇨🇦", name: "Canada" },
    { id: "au", flag: "🇦🇺", name: "Australia" },
    { id: "sg", flag: "🇸🇬", name: "Singapore" },
    { id: "my", flag: "🇲🇾", name: "Malaysia" },
    { id: "th", flag: "🇹🇭", name: "Thailand" },
    { id: "vn", flag: "🇻🇳", name: "Vietnam" },
    { id: "id", flag: "🇮🇩", name: "Indonesia" },
    { id: "ph", flag: "🇵🇭", name: "Philippines" },
    { id: "in", flag: "🇮🇳", name: "India" },
    { id: "br", flag: "🇧🇷", name: "Brazil" },
    { id: "other", flag: "globe", name: "Other" }
  ];

  const TRANSLATIONS = {
    en: {
      genderPrompt: "Great! And what is your gender?",
      female: "Female",
      male: "Male",
      countryPrompt: "Where are you from?",
      companionPrompt: "Awesome! Who are you traveling with?",
      friends: "Friends",
      family: "Family",
      solo: "Solo",
      partner: "Partner",
      gpsPrompt: "Perfect! Lastly, please allow location access so we can explore together.",
      privacy1: "Location is only used for exploring.",
      privacy2: "Never stored.",
      allowBtn: "Allow Location Access"
    },
    ja: {
      genderPrompt: "素晴らしい！あなたの性別を教えてください。",
      female: "女性",
      male: "男性",
      countryPrompt: "どちらの国から来られましたか？",
      companionPrompt: "いいですね！誰と一緒に旅行していますか？",
      friends: "友達",
      family: "家族",
      solo: "一人",
      partner: "恋人",
      gpsPrompt: "完璧です！最後に、一緒に探索できるように位置情報のアクセスを許可してください。",
      privacy1: "位置情報は探索のみに使用されます。",
      privacy2: "保存されることはありません。",
      allowBtn: "位置情報へのアクセスを許可"
    },
    zh: {
      genderPrompt: "太棒了！您的性别是什么？",
      female: "女",
      male: "男",
      countryPrompt: "您来自哪个国家？",
      companionPrompt: "好的！您和谁一起旅行？",
      friends: "朋友",
      family: "家人",
      solo: "独自",
      partner: "伴侣",
      gpsPrompt: "完美！最后，请允许访问位置信息，让我们一起探索吧。",
      privacy1: "位置信息仅用于探索。",
      privacy2: "绝不存储。",
      allowBtn: "允许访问位置信息"
    }
  };

  const t = TRANSLATIONS[languageId];

  const handleLanguageSelect = (id: "en" | "ja" | "zh", label: string) => {
    setLanguageId(id);
    setLanguageLabel(label);
    setStep("gender");
  };

  const handleGenderSelect = (label: string) => {
    setGenderLabel(label);
    setStep("country");
  };

  const handleCountrySelect = (label: string) => {
    setCountryLabel(label);
    setIsCountryExpanded(false);
    setStep("companion");
  };

  const handleCompanionSelect = (label: string) => {
    setCompanionLabel(label);
    setStep("gps");
  };

  const handleAllow = () => {
    setAllowPressed(true);
    setTimeout(onAllow, 300);
  };

  // Scroll to bottom when step changes
  const scrollRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [step]);

  return (
    <div
      className="flex-1 flex flex-col overflow-hidden animate-screenSwitch"
      style={{
        background: "linear-gradient(180deg, #FFFFFF 0%, #F0F4FF 100%)",
        transition: "background 0.6s ease",
      }}
    >
      <div className="flex-1 overflow-y-auto px-5 py-8 hide-scrollbar" ref={scrollRef}>
        
        {/* Step 1: Language */}
        <GuideBubble delay={0.1}>
          <div style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 16, fontWeight: 600, color: "#5478FF", marginBottom: 4 }}>
            Find the Best Spots Near You!
          </div>
          <div>To give you the best experience, what language do you prefer?</div>
        </GuideBubble>

        {step === "language" && (
          <div className="flex flex-col gap-2 pl-12 pr-4 mb-4 animate-fadeUp" style={{ animationDelay: "0.4s" }}>
            {[
              { id: "en", label: "English" },
              { id: "ja", label: "日本語" },
              { id: "zh", label: "中文" },
            ].map((l) => (
              <button
                key={l.id}
                onClick={() => handleLanguageSelect(l.id as "en" | "ja" | "zh", l.label)}
                style={{
                  padding: "12px 16px",
                  borderRadius: "14px",
                  border: "1px solid #E2E8F0",
                  background: "#FFFFFF",
                  color: "#1A1A2E",
                  fontFamily: "'Pretendard', sans-serif",
                  fontSize: 14,
                  fontWeight: 500,
                  cursor: "pointer",
                  textAlign: "left",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.02)",
                  transition: "all 0.2s ease",
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.borderColor = "#5478FF";
                  e.currentTarget.style.color = "#5478FF";
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.borderColor = "#E2E8F0";
                  e.currentTarget.style.color = "#1A1A2E";
                }}
              >
                {l.label}
              </button>
            ))}
          </div>
        )}

        {/* Step 2: Gender */}
        {step !== "language" && (
          <>
            <UserBubble>{languageLabel}</UserBubble>
            
            <GuideBubble delay={0.2}>
              {t.genderPrompt}
            </GuideBubble>

            {step === "gender" && (
              <div className="flex gap-3 pl-12 pr-4 mb-4 animate-fadeUp" style={{ animationDelay: "0.5s" }}>
                {[
                  { id: "female", label: t.female },
                  { id: "male", label: t.male },
                ].map((g) => (
                  <button
                    key={g.id}
                    onClick={() => handleGenderSelect(g.label)}
                    style={{
                      flex: 1,
                      padding: "12px 16px",
                      borderRadius: "14px",
                      border: "1px solid #E2E8F0",
                      background: "#FFFFFF",
                      color: "#1A1A2E",
                      fontFamily: "'Pretendard', sans-serif",
                      fontSize: 14,
                      fontWeight: 500,
                      cursor: "pointer",
                      textAlign: "center",
                      boxShadow: "0 2px 4px rgba(0,0,0,0.02)",
                      transition: "all 0.2s ease",
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.borderColor = "#5478FF";
                      e.currentTarget.style.color = "#5478FF";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.borderColor = "#E2E8F0";
                      e.currentTarget.style.color = "#1A1A2E";
                    }}
                  >
                    {g.label}
                  </button>
                ))}
              </div>
            )}
          </>
        )}

        {/* Step 3: Country */}
        {step !== "language" && step !== "gender" && (
          <>
            <UserBubble>{genderLabel}</UserBubble>
            
            <GuideBubble delay={0.2}>
              {t.countryPrompt}
            </GuideBubble>

            {step === "country" && (
              <div className="flex flex-col gap-2 pl-12 pr-4 mb-4 animate-fadeUp" style={{ animationDelay: "0.5s" }}>
                <button
                  onClick={() => setIsCountryExpanded(!isCountryExpanded)}
                  style={{
                    padding: "12px 16px",
                    borderRadius: "14px",
                    border: "1px solid #E2E8F0",
                    background: "#FFFFFF",
                    color: "#1A1A2E",
                    fontFamily: "'Pretendard', sans-serif",
                    fontSize: 14,
                    fontWeight: 500,
                    cursor: "pointer",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    boxShadow: "0 2px 4px rgba(0,0,0,0.02)",
                    transition: "all 0.2s ease",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = "#5478FF";
                    e.currentTarget.style.color = "#5478FF";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = "#E2E8F0";
                    e.currentTarget.style.color = "#1A1A2E";
                  }}
                >
                  <span>Select your country</span>
                  <svg 
                    width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                    style={{ 
                      transform: isCountryExpanded ? "rotate(180deg)" : "rotate(0deg)",
                      transition: "transform 0.3s ease" 
                    }}
                  >
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </button>
                
                <div 
                  className="hide-scrollbar"
                  style={{
                    maxHeight: isCountryExpanded ? "300px" : "0px",
                    overflowY: "auto",
                    overflowX: "hidden",
                    opacity: isCountryExpanded ? 1 : 0,
                    transition: "all 0.4s cubic-bezier(0.4, 0, 0.2, 1)",
                    background: "#FFFFFF",
                    borderRadius: "14px",
                    border: isCountryExpanded ? "1px solid #E2E8F0" : "none",
                    boxShadow: isCountryExpanded ? "0 4px 12px rgba(0,0,0,0.05)" : "none",
                    padding: isCountryExpanded ? "12px" : "0px",
                    marginTop: isCountryExpanded ? "4px" : "0px",
                    display: "flex",
                    flexWrap: "wrap",
                    gap: "8px"
                  }}
                >
                  {COUNTRIES.map((c) => (
                    <button
                      key={c.id}
                      onClick={() => handleCountrySelect(`${c.flag} ${c.name}`)}
                      style={{
                        padding: "8px 12px",
                        borderRadius: "20px",
                        border: "1px solid #E2E8F0",
                        background: "#F8FAFC",
                        color: "#1A1A2E",
                        fontFamily: "'Pretendard', sans-serif",
                        fontSize: 13,
                        fontWeight: 500,
                        cursor: "pointer",
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                        transition: "all 0.2s ease",
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.background = "#EFF6FF";
                        e.currentTarget.style.borderColor = "#BFDBFE";
                        e.currentTarget.style.color = "#2563EB";
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.background = "#F8FAFC";
                        e.currentTarget.style.borderColor = "#E2E8F0";
                        e.currentTarget.style.color = "#1A1A2E";
                      }}
                    >
                      <span style={{ fontSize: 16 }}>{c.flag === "globe" ? <Globe size={16} color="#5478FF" strokeWidth={2.5} /> : c.flag}</span>
                      {c.name}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </>
        )}

        {/* Step 4: Companion */}
        {step !== "language" && step !== "gender" && step !== "country" && (
          <>
            <UserBubble>{countryLabel}</UserBubble>
            
            <GuideBubble delay={0.2}>
              {t.companionPrompt}
            </GuideBubble>

            {step === "companion" && (
              <div className="flex flex-col gap-2 pl-12 pr-4 mb-4 animate-fadeUp" style={{ animationDelay: "0.5s" }}>
                {[
                  { id: "friends", label: t.friends, icon: <Users size={18} color="#5478FF" strokeWidth={2.5} fill="#5478FF" fillOpacity={0.12} />, bg: "#EEF2FF" },
                  { id: "family", label: t.family, icon: <UsersRound size={18} color="#16A34A" strokeWidth={2.5} fill="#16A34A" fillOpacity={0.12} />, bg: "#F0FDF4" },
                  { id: "solo", label: t.solo, icon: <Backpack size={18} color="#EA580C" strokeWidth={2.5} fill="#EA580C" fillOpacity={0.12} />, bg: "#FFF7ED" },
                  { id: "partner", label: t.partner, icon: <Heart size={18} color="#E11D48" strokeWidth={2.5} fill="#E11D48" fillOpacity={0.15} />, bg: "#FFF1F2" },
                ].map((c) => (
                  <button
                    key={c.id}
                    onClick={() => handleCompanionSelect(c.label)}
                    style={{
                      padding: "12px 16px",
                      borderRadius: "14px",
                      border: "1px solid #E2E8F0",
                      background: "#FFFFFF",
                      color: "#1A1A2E",
                      fontFamily: "'Pretendard', sans-serif",
                      fontSize: 14,
                      fontWeight: 500,
                      cursor: "pointer",
                      textAlign: "left",
                      boxShadow: "0 2px 4px rgba(0,0,0,0.02)",
                      transition: "all 0.2s ease",
                      display: "flex",
                      alignItems: "center",
                      gap: "10px"
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.borderColor = "#5478FF";
                      e.currentTarget.style.color = "#5478FF";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.borderColor = "#E2E8F0";
                      e.currentTarget.style.color = "#1A1A2E";
                    }}
                  >
                    <span className="w-8 h-8 rounded-[10px] flex items-center justify-center flex-shrink-0" style={{ background: c.bg }}>{c.icon}</span>
                    <span>{c.label}</span>
                  </button>
                ))}
              </div>
            )}
          </>
        )}

        {/* Step 5: GPS Permission */}
        {step === "gps" && (
          <>
            <UserBubble>{companionLabel}</UserBubble>
            
            <GuideBubble delay={0.2}>
              {t.gpsPrompt}
              
              <div
                style={{
                  marginTop: 12,
                  padding: "10px",
                  background: "#F8FAFC",
                  borderRadius: "10px",
                  fontSize: 13,
                  color: "#64748B",
                  lineHeight: 1.5,
                }}
              >
                {t.privacy1}<br />
                <span style={{ fontWeight: 600, color: "#334155" }}>{t.privacy2}</span>
              </div>
            </GuideBubble>

            <div className="pl-12 pr-4 mb-4 animate-fadeUp" style={{ animationDelay: "0.6s" }}>
              <button
                onClick={handleAllow}
                className="w-full flex items-center justify-center gap-2"
                style={{
                  padding: "16px 20px",
                  borderRadius: "16px",
                  border: "none",
                  fontFamily: "'Pretendard', sans-serif",
                  fontSize: 16,
                  fontWeight: 600,
                  color: "#fff",
                  background: allowPressed
                    ? "linear-gradient(135deg, #2240CC, #3D5EFF)"
                    : "linear-gradient(135deg, #325BFF 0%, #5478FF 55%, #7C98FF 100%)",
                  boxShadow: "0 8px 24px rgba(84,120,255,0.3)",
                  cursor: "pointer",
                  transform: allowPressed ? "scale(0.97)" : "scale(1)",
                  transition: "all 0.2s ease",
                }}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z" />
                  <circle cx="12" cy="10" r="3" fill="white" stroke="none" />
                </svg>
                {t.allowBtn}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}