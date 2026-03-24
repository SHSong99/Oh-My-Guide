/**
 * MyPageScreen.tsx — User Profile & Settings
 * Mirrors: Android > ui/screen/mypage/MyPageScreen.kt
 */

import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import {
  MapPin, Bookmark, Headphones, Globe, Bell, Palette,
  UtensilsCrossed, Landmark, ChevronRight, LogOut,
} from "lucide-react";

const MENU_ITEMS = [
  { icon: MapPin, color: "#5478FF", bg: "#EEF2FF", label: "Visit History", desc: "Places you've been", count: 0 },
  { icon: Bookmark, color: "#F59E0B", bg: "#FFFBEB", label: "Bookmarks", desc: "Saved places & phrases", count: 0 },
  { icon: Headphones, color: "#8B5CF6", bg: "#F5F3FF", label: "Story Archive", desc: "Stories you've listened", count: 0 },
  { icon: Globe, color: "#06B6D4", bg: "#ECFEFF", label: "Language", desc: "English", action: true },
  { icon: Bell, color: "#EF4444", bg: "#FEF2F2", label: "Notifications", desc: "On", action: true },
  { icon: Palette, color: "#EC4899", bg: "#FDF2F8", label: "Theme", desc: "Light", action: true },
];

const INTEREST_TAGS = [
  { icon: UtensilsCrossed, label: "Food", color: "#FF6B6B" },
  { icon: Landmark, label: "Culture", color: "#5478FF" },
];

export function MyPageScreen() {
  return (
    <div
      className="flex-1 flex flex-col overflow-hidden animate-screenSwitch"
      style={{ background: "#F8FAFF" }}
    >
      {/* Header */}
      <div style={{ padding: "16px 20px 12px", background: "#fff", borderBottom: "1px solid #F0F2F5" }}>
        <div style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 18, fontWeight: 700, color: "#1A1A2E" }}>
          My Page
        </div>
      </div>

      <div className="flex-1 overflow-y-auto hide-scrollbar" style={{ padding: "20px 16px" }}>
        {/* Profile Card */}
        <div
          style={{
            background: "#fff",
            borderRadius: 20,
            padding: "24px 20px",
            boxShadow: "0 2px 12px rgba(0,0,0,0.04)",
            border: "1px solid #F0F2F5",
            marginBottom: 16,
          }}
        >
          <div className="flex items-center gap-4">
            <img
              src={mascotImg}
              alt="Profile"
              style={{
                width: 60,
                height: 60,
                borderRadius: "50%",
                border: "3px solid #5478FF",
                objectFit: "cover",
                background: "#EEF2FF",
              }}
            />
            <div>
              <div style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 18, fontWeight: 700, color: "#1A1A2E" }}>
                Traveler
              </div>
              <div style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 13, color: "#8892A4", marginTop: 2 }}>
                Seoul, Jongno area
              </div>
              <div className="flex gap-1.5 mt-2">
                {INTEREST_TAGS.map(tag => (
                  <span
                    key={tag.label}
                    className="flex items-center gap-1"
                    style={{
                      fontFamily: "'Pretendard', sans-serif",
                      fontSize: 11,
                      fontWeight: 600,
                      color: "#5478FF",
                      background: "#F0F4FF",
                      borderRadius: 8,
                      padding: "2px 8px",
                    }}
                  >
                    <tag.icon size={10} color={tag.color} strokeWidth={2.5} />
                    {tag.label}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Menu Items */}
        <div
          style={{
            background: "#fff",
            borderRadius: 20,
            overflow: "hidden",
            boxShadow: "0 2px 12px rgba(0,0,0,0.04)",
            border: "1px solid #F0F2F5",
          }}
        >
          {MENU_ITEMS.map((item, i) => {
            const Icon = item.icon;
            return (
              <div
                key={item.label}
                className="flex items-center gap-3 cursor-pointer"
                style={{
                  padding: "14px 16px",
                  borderBottom: i < MENU_ITEMS.length - 1 ? "1px solid #F8F9FB" : "none",
                }}
              >
                <div
                  style={{
                    width: 36,
                    height: 36,
                    borderRadius: 10,
                    background: item.bg,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    flexShrink: 0,
                  }}
                >
                  <Icon size={17} color={item.color} strokeWidth={2.2} />
                </div>
                <div className="flex-1 min-w-0">
                  <div style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 14, fontWeight: 600, color: "#1A1A2E" }}>
                    {item.label}
                  </div>
                  <div style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 11, color: "#8892A4" }}>
                    {item.desc}
                  </div>
                </div>
                {item.count !== undefined && !item.action && (
                  <span style={{ fontFamily: "'Pretendard', sans-serif", fontSize: 13, fontWeight: 700, color: "#5478FF" }}>
                    {item.count}
                  </span>
                )}
                <ChevronRight size={14} color="#C0C6D4" strokeWidth={2.5} />
              </div>
            );
          })}
        </div>

        {/* Logout */}
        <button
          className="w-full mt-4 cursor-pointer flex items-center justify-center gap-2"
          style={{
            padding: 14,
            background: "#fff",
            borderRadius: 16,
            border: "1px solid #F0F2F5",
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 14,
            fontWeight: 600,
            color: "#E53E3E",
            textAlign: "center",
            boxShadow: "0 2px 12px rgba(0,0,0,0.04)",
          }}
        >
          <LogOut size={15} color="#E53E3E" strokeWidth={2.2} />
          Sign Out
        </button>

        <div style={{ height: 16 }} />
      </div>
    </div>
  );
}
