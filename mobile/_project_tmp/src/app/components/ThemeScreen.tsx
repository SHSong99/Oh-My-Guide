import { useState } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import {
  Mountain, Landmark, Sparkles, Route, Dumbbell,
  Coffee, ShoppingBag, UtensilsCrossed
} from "lucide-react";

const THEME_ICONS: Record<string, React.ElementType> = {
  attraction: Mountain,
  culture: Landmark,
  festival: Sparkles,
  course: Route,
  leports: Dumbbell,
  cafe: Coffee,
  shopping: ShoppingBag,
  food: UtensilsCrossed,
};

interface ThemeScreenProps {
  selectedCategories: string[];
}

const THEMES = [
  {
    id: "attraction",
    name: "Attraction",
    subtitle: "Landmarks & nature",
    color: "#4CAF50",
    gradient: "linear-gradient(135deg, #4CAF5033, #4CAF5011)",
    places: 42,
    featured: ["N Seoul Tower", "Haeundae Beach", "Seoraksan"],
  },
  {
    id: "culture",
    name: "Culture",
    subtitle: "Museums & history",
    color: "#5478FF",
    gradient: "linear-gradient(135deg, #5478FF33, #5478FF11)",
    places: 28,
    featured: ["Gyeongbokgung", "National Museum", "Bukchon Hanok Village"],
  },
  {
    id: "festival",
    name: "Festival",
    subtitle: "Events & performances",
    color: "#E91E63",
    gradient: "linear-gradient(135deg, #E91E6333, #E91E6311)",
    places: 15,
    featured: ["Seoul Lantern Festival", "Water Bomb", "K-Pop Concerts"],
  },
  {
    id: "course",
    name: "Course",
    subtitle: "Travel routes",
    color: "#00BCD4",
    gradient: "linear-gradient(135deg, #00BCD433, #00BCD411)",
    places: 12,
    featured: ["Seoul City Tour", "Han River Route", "Night View Course"],
  },
  {
    id: "leports",
    name: "Leports",
    subtitle: "Leisure & sports",
    color: "#FF9800",
    gradient: "linear-gradient(135deg, #FF980033, #FF980011)",
    places: 24,
    featured: ["Han River Kayaking", "Ski Resorts", "Paragliding"],
  },
  {
    id: "cafe",
    name: "Cafes",
    subtitle: "Coffee & bakeries",
    color: "#8D6E63",
    gradient: "linear-gradient(135deg, #8D6E6333, #8D6E6311)",
    places: 64,
    featured: ["Seongsu Cafes", "Yeonnam-dong", "Hanok Teahouses"],
  },
  {
    id: "shopping",
    name: "Shopping",
    subtitle: "Markets & malls",
    color: "#9C27B0",
    gradient: "linear-gradient(135deg, #9C27B033, #9C27B011)",
    places: 38,
    featured: ["Myeongdong", "The Hyundai", "Dongdaemun Market"],
  },
  {
    id: "food",
    name: "Food",
    subtitle: "Dining & street eats",
    color: "#FF6B6B",
    gradient: "linear-gradient(135deg, #FF6B6B33, #FF6B6B11)",
    places: 85,
    featured: ["Gwangjang Market", "Gangnam BBQ", "Myeongdong Kyoja"],
  },
];

export function ThemeScreen({ selectedCategories }: ThemeScreenProps) {
  const [expandedTheme, setExpandedTheme] = useState<string | null>(null);

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
        <div
          style={{
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 18,
            fontWeight: 700,
            color: "#1A1A2E",
          }}
        >
          Explore Themes
        </div>
        <div
          style={{
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 12,
            color: "#8892A4",
            marginTop: 2,
          }}
        >
          Browse nearby spots by what you love
        </div>
      </div>

      {/* Mascot suggestion */}
      <div
        style={{
          padding: "12px 16px",
          background: "linear-gradient(135deg, #F0F4FF, #FAFBFF)",
          borderBottom: "1px solid #F0F2F5",
        }}
      >
        <div className="flex items-center gap-3">
          <img
            src={mascotImg}
            alt=""
            style={{
              width: 36,
              height: 36,
              borderRadius: "50%",
              border: "2px solid #5478FF",
              objectFit: "cover",
              flexShrink: 0,
            }}
          />
          <div
            style={{
              background: "#fff",
              borderRadius: "4px 16px 16px 16px",
              padding: "10px 14px",
              boxShadow: "0 2px 8px rgba(84,120,255,0.08)",
              fontFamily: "'Pretendard', sans-serif",
              fontSize: 13,
              color: "#1A1A2E",
              lineHeight: 1.5,
            }}
          >
            These are the hottest themes near 종로 right now! Tap any to see top picks.
          </div>
        </div>
      </div>

      {/* Theme Grid */}
      <div
        className="flex-1 overflow-y-auto hide-scrollbar"
        style={{ padding: "14px 16px", background: "#F8FAFF" }}
      >
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: 10,
          }}
        >
          {THEMES.map((theme, i) => {
            const isUserInterest = selectedCategories.includes(theme.id);
            const isExpanded = expandedTheme === theme.id;

            return (
              <div
                key={theme.id}
                onClick={() => setExpandedTheme(isExpanded ? null : theme.id)}
                className="animate-slideUp cursor-pointer"
                style={{
                  borderRadius: 18,
                  overflow: "hidden",
                  background: "#fff",
                  border: `1.5px solid ${isUserInterest ? theme.color + "40" : "#E8ECF4"}`,
                  boxShadow: isUserInterest
                    ? `0 4px 16px ${theme.color}20`
                    : "0 2px 8px rgba(0,0,0,0.04)",
                  transition: "all 0.25s",
                  animationDelay: `${i * 0.07}s`,
                  transform: isExpanded ? "scale(1.02)" : "scale(1)",
                }}
              >
                {/* Card top */}
                <div
                  style={{
                    background: theme.gradient,
                    padding: "16px 14px 12px",
                    position: "relative",
                  }}
                >
                  {isUserInterest && (
                    <div
                      style={{
                        position: "absolute",
                        top: 8,
                        right: 8,
                        background: theme.color,
                        borderRadius: 20,
                        padding: "2px 6px",
                        fontFamily: "'Pretendard', sans-serif",
                        fontSize: 9,
                        fontWeight: 700,
                        color: "#fff",
                      }}
                    >
                      MY ★
                    </div>
                  )}
                  <div style={{ width: 44, height: 44, borderRadius: 14, background: `${theme.color}18`, display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 6 }}>
                    {(() => { const Icon = THEME_ICONS[theme.id]; return Icon ? <Icon size={24} color={theme.color} strokeWidth={2} fill={theme.color} fillOpacity={0.15} /> : null; })()}
                  </div>
                  <div
                    style={{
                      fontFamily: "'Pretendard', sans-serif",
                      fontSize: 14,
                      fontWeight: 600,
                      color: "#1A1A2E",
                    }}
                  >
                    {theme.name}
                  </div>
                  <div
                    style={{
                      fontFamily: "'Pretendard', sans-serif",
                      fontSize: 10,
                      color: "#8892A4",
                      marginTop: 1,
                    }}
                  >
                    {theme.subtitle}
                  </div>
                </div>

                {/* Card bottom */}
                <div style={{ padding: "10px 14px" }}>
                  <div className="flex items-center justify-between">
                    <span
                      style={{
                        fontFamily: "'Pretendard', sans-serif",
                        fontSize: 11,
                        fontWeight: 600,
                        color: theme.color,
                      }}
                    >
                      {theme.places} spots
                    </span>
                    <svg
                      width="14"
                      height="14"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke={isExpanded ? theme.color : "#C0C6D4"}
                      strokeWidth="2.5"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      style={{ transition: "all 0.2s", transform: isExpanded ? "rotate(90deg)" : "none" }}
                    >
                      <path d="M9 18l6-6-6-6" />
                    </svg>
                  </div>

                  {isExpanded && (
                    <div style={{ marginTop: 8, borderTop: "1px solid #F0F2F5", paddingTop: 8 }}>
                      {theme.featured.map((place, j) => (
                        <div
                          key={j}
                          className="flex items-center gap-2"
                          style={{ marginBottom: j < theme.featured.length - 1 ? 6 : 0 }}
                        >
                          <div
                            style={{
                              width: 5,
                              height: 5,
                              borderRadius: "50%",
                              background: theme.color,
                              flexShrink: 0,
                            }}
                          />
                          <span
                            style={{
                              fontFamily: "'Pretendard', sans-serif",
                              fontSize: 11,
                              color: "#4A5568",
                            }}
                          >
                            {place}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        {/* Bottom suggestion */}
        <div
          style={{
            marginTop: 14,
            padding: "14px 16px",
            borderRadius: 16,
            background: "linear-gradient(135deg, #5478FF15, #7C98FF10)",
            border: "1px dashed rgba(84,120,255,0.25)",
            display: "flex",
            alignItems: "center",
            gap: 10,
          }}
        >
          <span style={{ width: 32, height: 32, borderRadius: 10, background: "#DBEAFE", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#2563EB" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" /></svg>
          </span>
          <div>
            <div
              style={{
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 13,
                fontWeight: 600,
                color: "#5478FF",
              }}
            >
              Live GPS recommendations active
            </div>
            <div
              style={{
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 11,
                color: "#8892A4",
                marginTop: 1,
              }}
            >
              종로 (Jongno) · Showing spots within 2km
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}