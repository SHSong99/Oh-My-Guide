type NavTab = "main" | "explore" | "phrases";

interface BottomNavProps {
  activeTab: NavTab;
  onTabChange: (tab: NavTab) => void;
}

const HomeIcon = ({ active }: { active: boolean }) => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={active ? "#5478FF" : "#B0B8C8"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" fill={active ? "rgba(84,120,255,0.12)" : "none"} />
    <polyline points="9 22 9 12 15 12 15 22" />
  </svg>
);

const ThemeIcon = ({ active }: { active: boolean }) => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={active ? "#5478FF" : "#B0B8C8"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" fill={active ? "rgba(84,120,255,0.12)" : "none"} />
  </svg>
);

const PhrasesIcon = ({ active }: { active: boolean }) => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={active ? "#5478FF" : "#B0B8C8"} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" fill={active ? "rgba(84,120,255,0.12)" : "none"} />
  </svg>
);

const TABS: { id: NavTab; label: string; Icon: ({ active }: { active: boolean }) => JSX.Element }[] = [
  { id: "main", label: "Home", Icon: HomeIcon },
  { id: "explore", label: "Explore", Icon: ThemeIcon },
  { id: "phrases", label: "Phrases", Icon: PhrasesIcon },
];

export function BottomNav({ activeTab, onTabChange }: BottomNavProps) {
  return (
    <div
      style={{
        display: "flex",
        flexShrink: 0,
        height: 58,
        borderTop: "1px solid #F0F2F5",
        background: "rgba(255,255,255,0.98)",
        backdropFilter: "blur(12px)",
      }}
    >
      {TABS.map(({ id, label, Icon }) => {
        const active = activeTab === id;
        return (
          <button
            key={id}
            onClick={() => onTabChange(id)}
            style={{
              flex: 1,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              justifyContent: "center",
              gap: 3,
              background: "none",
              border: "none",
              cursor: "pointer",
              position: "relative",
              padding: "6px 0",
            }}
          >
            {active && (
              <div
                style={{
                  position: "absolute",
                  top: 0,
                  left: "50%",
                  transform: "translateX(-50%)",
                  width: 32,
                  height: 2,
                  borderRadius: "0 0 3px 3px",
                  background: "linear-gradient(90deg, #325BFF, #7C98FF)",
                }}
              />
            )}
            <Icon active={active} />
            <span
              style={{
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 10,
                fontWeight: active ? 700 : 400,
                color: active ? "#5478FF" : "#B0B8C8",
                transition: "color 0.2s",
              }}
            >
              {label}
            </span>
          </button>
        );
      })}
    </div>
  );
}