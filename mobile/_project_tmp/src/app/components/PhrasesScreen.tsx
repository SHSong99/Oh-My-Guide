import { useState } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import {
  Hand, UtensilsCrossed, MapIcon, ShoppingBag, ShieldAlert
} from "lucide-react";

interface Phrase {
  kr: string;
  pron: string;
  en: string;
}

interface PhraseSection {
  title: string;
  subtitle: string;
  icon: React.ElementType;
  color: string;
  phrases: Phrase[];
}

const PHRASE_SECTIONS: PhraseSection[] = [
  {
    title: "기본 표현",
    subtitle: "Basic Expressions",
    icon: Hand,
    color: "#5478FF",
    phrases: [
      { kr: "안녕하세요", pron: "an-nyeong-ha-se-yo", en: "Hello / Hi" },
      { kr: "감사합니다", pron: "gam-sa-ham-ni-da", en: "Thank you" },
      { kr: "죄송합니다", pron: "joe-song-ham-ni-da", en: "I'm sorry" },
      { kr: "괜찮아요", pron: "gwaen-cha-na-yo", en: "It's okay / I'm fine" },
    ],
  },
  {
    title: "식당에서",
    subtitle: "At a Restaurant",
    icon: UtensilsCrossed,
    color: "#FF6B6B",
    phrases: [
      { kr: "이거 주세요", pron: "i-geo ju-se-yo", en: "This one, please" },
      { kr: "얼마예요?", pron: "eol-ma-ye-yo", en: "How much is it?" },
      { kr: "맛있어요!", pron: "mas-i-sseo-yo", en: "It's delicious!" },
      { kr: "메뉴 주세요", pron: "me-nyu ju-se-yo", en: "Menu, please" },
      { kr: "물 주세요", pron: "mul ju-se-yo", en: "Water, please" },
    ],
  },
  {
    title: "길 찾을 때",
    subtitle: "Getting Around",
    icon: MapIcon,
    color: "#4CAF50",
    phrases: [
      { kr: "어디예요?", pron: "eo-di-ye-yo", en: "Where is it?" },
      { kr: "지하철역 어디예요?", pron: "ji-ha-cheol-yeok eo-di-ye-yo", en: "Where's the subway?" },
      { kr: "택시 불러주세요", pron: "taek-si bul-leo-ju-se-yo", en: "Please call a taxi" },
      { kr: "여기 가주세요", pron: "yeo-gi ga-ju-se-yo", en: "Please go here" },
    ],
  },
  {
    title: "쇼핑할 때",
    subtitle: "While Shopping",
    icon: ShoppingBag,
    color: "#FF9800",
    phrases: [
      { kr: "이거 있어요?", pron: "i-geo i-sseo-yo", en: "Do you have this?" },
      { kr: "깎아주세요", pron: "kka-kka-ju-se-yo", en: "Please give me a discount" },
      { kr: "카드 돼요?", pron: "ka-deu dwae-yo", en: "Can I pay by card?" },
      { kr: "입어봐도 돼요?", pron: "i-beo-bwa-do dwae-yo", en: "May I try it on?" },
    ],
  },
  {
    title: "응급 상황",
    subtitle: "Emergency",
    icon: ShieldAlert,
    color: "#E91E63",
    phrases: [
      { kr: "도와주세요!", pron: "do-wa-ju-se-yo", en: "Please help me!" },
      { kr: "병원 어디예요?", pron: "byeong-won eo-di-ye-yo", en: "Where's the hospital?" },
      { kr: "경찰 불러주세요", pron: "gyeong-chal bul-leo-ju-se-yo", en: "Please call the police" },
    ],
  },
];

export function PhrasesScreen() {
  const [saved, setSaved] = useState<Set<string>>(new Set());
  const [activeSection, setActiveSection] = useState<string | null>(null);

  const toggleSave = (key: string) => {
    setSaved((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

  return (
    <div className="flex-1 flex flex-col overflow-hidden animate-screenSwitch">
      {/* Header */}
      <div
        style={{
          padding: "16px 20px 12px",
          background: "#fff",
          borderBottom: "1px solid #F0F2F5",
        }}
      >
        <div className="flex items-center justify-between">
          <div>
            <div
              style={{
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 18,
                fontWeight: 700,
                color: "#1A1A2E",
              }}
            >
              한국어 구문
            </div>
            <div
              style={{
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 12,
                color: "#8892A4",
                marginTop: 2,
              }}
            >
              Korean Phrases for Travelers
            </div>
          </div>
          {saved.size > 0 && (
            <div
              style={{
                padding: "4px 10px",
                background: "#FFF9C4",
                borderRadius: 12,
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 11,
                fontWeight: 600,
                color: "#B8960C",
              }}
            >
              ★ {saved.size} saved
            </div>
          )}
        </div>
      </div>

      {/* Mascot tip */}
      <div
        style={{
          padding: "10px 16px",
          background: "linear-gradient(135deg, #F0F4FF, #FAFBFF)",
          borderBottom: "1px solid #F0F2F5",
        }}
      >
        <div className="flex items-center gap-2">
          <img
            src={mascotImg}
            alt=""
            style={{
              width: 30,
              height: 30,
              borderRadius: "50%",
              border: "2px solid #5478FF",
              objectFit: "cover",
              flexShrink: 0,
            }}
          />
          <div
            style={{
              background: "#fff",
              borderRadius: "4px 14px 14px 14px",
              padding: "8px 12px",
              boxShadow: "0 2px 8px rgba(84,120,255,0.08)",
              fontFamily: "'Pretendard', sans-serif",
              fontSize: 12,
              color: "#1A1A2E",
              lineHeight: 1.5,
            }}
          >
            ★ Tap the bookmark to save phrases for quick access offline!
          </div>
        </div>
      </div>

      {/* Sections */}
      <div
        className="flex-1 overflow-y-auto hide-scrollbar"
        style={{ background: "#F8FAFF", padding: "12px 14px" }}
      >
        {PHRASE_SECTIONS.map((section, si) => {
          const isOpen = activeSection === section.title || activeSection === null;

          return (
            <div
              key={section.title}
              className="animate-slideUp"
              style={{
                marginBottom: 10,
                borderRadius: 16,
                overflow: "hidden",
                boxShadow: "0 2px 8px rgba(0,0,0,0.04)",
                animationDelay: `${si * 0.08}s`,
              }}
            >
              {/* Section header */}
              <div
                onClick={() =>
                  setActiveSection(
                    activeSection === section.title ? null : section.title
                  )
                }
                className="flex items-center justify-between cursor-pointer"
                style={{
                  padding: "12px 16px",
                  background: "#fff",
                  borderBottom: isOpen ? `1px solid ${section.color}20` : "none",
                }}
              >
                <div className="flex items-center gap-3">
                  <div
                    style={{
                      width: 36,
                      height: 36,
                      borderRadius: 10,
                      background: `${section.color}18`,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      fontSize: 18,
                    }}
                  >
                    <section.icon size={18} color={section.color} strokeWidth={2.5} fill={section.color} fillOpacity={0.15} />
                  </div>
                  <div>
                    <div
                      style={{
                        fontFamily: "'Pretendard', sans-serif",
                        fontSize: 14,
                        fontWeight: 600,
                        color: "#1A1A2E",
                      }}
                    >
                      {section.title}
                    </div>
                    <div
                      style={{
                        fontFamily: "'Pretendard', sans-serif",
                        fontSize: 10,
                        color: "#8892A4",
                      }}
                    >
                      {section.subtitle}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span
                    style={{
                      fontFamily: "'Pretendard', sans-serif",
                      fontSize: 11,
                      color: section.color,
                      fontWeight: 600,
                    }}
                  >
                    {section.phrases.length} phrases
                  </span>
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="#B0B8C8"
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    style={{
                      transform: isOpen ? "rotate(180deg)" : "none",
                      transition: "transform 0.2s",
                    }}
                  >
                    <path d="M6 9l6 6 6-6" />
                  </svg>
                </div>
              </div>

              {/* Phrases */}
              {isOpen && (
                <div style={{ background: "#fff" }}>
                  {section.phrases.map((phrase, pi) => {
                    const key = `${section.title}-${pi}`;
                    const isSaved = saved.has(key);

                    return (
                      <div
                        key={pi}
                        className="flex items-center gap-3"
                        style={{
                          padding: "11px 16px",
                          borderBottom:
                            pi < section.phrases.length - 1
                              ? "1px solid #F8F9FB"
                              : "none",
                          background: isSaved ? "#FFFDE7" : "#fff",
                          transition: "background 0.2s",
                        }}
                      >
                        <div className="flex-1 min-w-0">
                          <div
                            style={{
                              fontFamily: "'Pretendard', sans-serif",
                              fontSize: 16,
                              fontWeight: 700,
                              color: "#1A1A2E",
                              marginBottom: 1,
                            }}
                          >
                            {phrase.kr}
                          </div>
                          <div
                            style={{
                              fontFamily: "'Pretendard', sans-serif",
                              fontSize: 10,
                              fontStyle: "italic",
                              color: section.color,
                              marginBottom: 1,
                            }}
                          >
                            {phrase.pron}
                          </div>
                          <div
                            style={{
                              fontFamily: "'Pretendard', sans-serif",
                              fontSize: 11,
                              color: "#6B7280",
                            }}
                          >
                            {phrase.en}
                          </div>
                        </div>
                        <button
                          onClick={() => toggleSave(key)}
                          style={{
                            background: "none",
                            border: "none",
                            cursor: "pointer",
                            padding: 4,
                            flexShrink: 0,
                          }}
                        >
                          <svg
                            width="18"
                            height="18"
                            viewBox="0 0 24 24"
                            fill={isSaved ? "#FFBF00" : "none"}
                            stroke={isSaved ? "#FFBF00" : "#D0D5DD"}
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          >
                            <path d="M19 21l-7-5-7 5V5a2 2 0 012-2h10a2 2 0 012 2z" />
                          </svg>
                        </button>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          );
        })}

        <div style={{ height: 8 }} />
      </div>
    </div>
  );
}