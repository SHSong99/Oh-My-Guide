/**
 * App.tsx — Entry point
 * Mirrors: Android > MainActivity.kt + NavGraph.kt
 */
import { useState, useCallback, useRef } from "react";

// ── Routes & types ──
import type { NavTab, Screen } from "./ui/navi/routes";
import { getActiveTab, getCategories, SHOW_NAV_SCREENS } from "./ui/navi/routes";

// ── Auth screens ──
import { SplashScreen } from "./ui/screen/auth/SplashScreen";
import { WelcomeScreen } from "./ui/screen/auth/WelcomeScreen";
import { GpsPermissionScreen } from "./ui/screen/auth/GpsPermissionScreen";
import { CategoryScreen } from "./ui/screen/auth/CategoryScreen";
import { LoadingScreen } from "./ui/screen/auth/LoadingScreen";

// ── Main tab screens ──
import { HomeScreen } from "./ui/screen/home/HomeScreen";
import { ExploreScreen } from "./ui/screen/explore/ExploreScreen";
import { PhrasesScreen } from "./ui/screen/phrases/PhrasesScreen";
import { MapScreen } from "./ui/screen/map/MapScreen";
import { MyPageScreen } from "./ui/screen/mypage/MyPageScreen";

// ── Detail / Navigation screens ──
import { PlaceScreen } from "./ui/screen/place/PlaceScreen";
import { TransportPickerScreen } from "./components/TransportPickerScreen";
import { TransitDetailScreen } from "./components/TransitDetailScreen";
import { NaviScreen } from "./ui/screen/navi/NaviScreen";

// ── Common UI ──
import { BottomNav } from "./ui/common/BottomNav";
import { FloatingNavButton } from "./components/FloatingNavButton";

import "./app-animations.css";

/* ── Place name lookup (shared with NavigationScreen) ── */
const PLACE_NAMES: Record<string, string> = {
  dm3: "Gwangjang Market",
  dm4: "Bukchon Hanok Village",
  dm5: "Namsan Tower",
  dm6: "Ikseon-dong",
  dm7: "Cheonggyecheon Stream",
};

export default function App() {
  const [screen, setScreen] = useState<Screen>({ type: "splash" });

  // ── Minimized navigation state ──
  // When user closes the navi screen via X, the NavigationScreen stays mounted but hidden,
  // and a draggable FAB appears on whatever screen the user navigates to.
  const [navMinimized, setNavMinimized] = useState<{
    placeId: string;
    categories: string[];
    transportMode?: string;
    transitRouteId?: string;
  } | null>(null);
  const [navProgress, setNavProgress] = useState(0);

  // Track the screen user was on before restoring navi
  const preNavScreenRef = useRef<Screen | null>(null);

  const goWelcome = useCallback(() => setScreen({ type: "welcome" }), []);
  const goGps = useCallback(() => setScreen({ type: "gps" }), []);
  const goCategory = useCallback(() => setScreen({ type: "category" }), []);

  const goLoading = useCallback(
    (cats: string[]) => setScreen({ type: "loading", categories: cats }),
    []
  );

  const goMain = useCallback(
    (cats: string[]) => setScreen({ type: "main", categories: cats }),
    []
  );

  const handleTabChange = useCallback(
    (tab: NavTab) => {
      const cats = getCategories(screen);
      if (tab === "main") setScreen({ type: "main", categories: cats });
      else if (tab === "explore") setScreen({ type: "explore", categories: cats });
      else if (tab === "phrases") setScreen({ type: "phrases", categories: cats });
    },
    [screen]
  );

  // ── Minimize navigation (X button on navi screen) ──
  const handleNavMinimize = useCallback(() => {
    if (screen.type === "navigation") {
      const cats = getCategories(screen);
      setNavMinimized({
        placeId: screen.placeId,
        categories: cats,
        transportMode: screen.transportMode,
        transitRouteId: screen.transitRouteId,
      });
      // Go back to main screen
      setScreen({ type: "main", categories: cats });
    }
  }, [screen]);

  // ── Restore navigation from FAB tap ──
  const handleNavRestore = useCallback(() => {
    if (!navMinimized) return;
    // Save current screen so back button works intuitively
    preNavScreenRef.current = screen;
    setScreen({
      type: "navigation",
      categories: navMinimized.categories,
      placeId: navMinimized.placeId,
      transportMode: navMinimized.transportMode,
      transitRouteId: navMinimized.transitRouteId,
    });
    setNavMinimized(null);
  }, [navMinimized, screen]);

  // ── Stop navigation completely (from FAB stop button) ──
  const handleNavStop = useCallback(() => {
    setNavMinimized(null);
    setNavProgress(0);
  }, []);

  // ── Navigation fully completes (arrival + "Back to Home") ──
  const handleNavComplete = useCallback(() => {
    setNavMinimized(null);
    setNavProgress(0);
    const cats = getCategories(screen);
    setScreen({ type: "main", categories: cats });
  }, [screen]);

  const showBottomNav = SHOW_NAV_SCREENS.includes(screen.type);
  const isDarkScreen = screen.type === "gps";
  const statusColor = isDarkScreen ? "#FFFFFF" : "#1A1A2E";

  // Should we show the FAB? Only when navigation is minimized and we're NOT on the navi screen
  const showNavFab = navMinimized !== null && screen.type !== "navigation";

  const renderScreen = () => {
    switch (screen.type) {
      case "splash":
        return <SplashScreen onFinish={goWelcome} />;
      case "welcome":
        return <WelcomeScreen onGetStarted={goGps} onSignIn={goGps} />;
      case "gps":
        return <GpsPermissionScreen onAllow={goCategory} onSkip={goCategory} />;
      case "category":
        return <CategoryScreen onConfirm={(cats) => goMain(cats)} />;
      case "loading":
        return <LoadingScreen onFinish={() => goMain(screen.categories)} />;
      case "main":
        return (
          <HomeScreen
            selectedCategories={screen.categories}
            onPlaceSelect={(id) =>
              setScreen({ type: "detail", categories: screen.categories, placeId: id })
            }
            onReset={goCategory}
          />
        );
      case "detail":
        return (
          <PlaceScreen
            placeId={screen.placeId}
            onBack={() => setScreen({ type: "main", categories: screen.categories })}
            onGo={() =>
              setScreen({
                type: "transport",
                categories: screen.categories,
                placeId: screen.placeId,
              })
            }
          />
        );
      case "transport":
        return (
          <TransportPickerScreen
            placeId={screen.placeId}
            onBack={() =>
              setScreen({
                type: "detail",
                categories: screen.categories,
                placeId: screen.placeId,
              })
            }
            onStart={(mode) =>
              setScreen({
                type: "navigation",
                categories: screen.categories,
                placeId: screen.placeId,
                transportMode: mode,
              })
            }
            onTransitDetail={() =>
              setScreen({
                type: "transit-detail",
                categories: screen.categories,
                placeId: screen.placeId,
              })
            }
          />
        );
      case "transit-detail":
        return (
          <TransitDetailScreen
            placeId={screen.placeId}
            onBack={() =>
              setScreen({
                type: "transport",
                categories: screen.categories,
                placeId: screen.placeId,
              })
            }
            onStart={(transitRouteId) =>
              setScreen({
                type: "navigation",
                categories: screen.categories,
                placeId: screen.placeId,
                transportMode: "transit",
                transitRouteId,
              })
            }
          />
        );
      case "navigation":
        return (
          <NaviScreen
            placeId={screen.placeId}
            transportMode={screen.transportMode || "walk"}
            transitRouteId={screen.transitRouteId}
            onBack={() =>
              setScreen({
                type: "detail",
                categories: screen.categories,
                placeId: screen.placeId,
              })
            }
            onComplete={handleNavComplete}
            onMinimize={handleNavMinimize}
            onProgressChange={setNavProgress}
          />
        );
      case "explore":
        return <ExploreScreen selectedCategories={screen.categories} />;
      case "phrases":
        return <PhrasesScreen />;
      case "map":
        return <MapScreen selectedCategories={screen.categories} />;
      case "mypage":
        return <MyPageScreen />;
    }
  };

  return (
    <div
      className="fixed inset-0 flex items-center justify-center overflow-hidden"
      style={{
        background: "#FFFFFF",
        padding: "24px 16px",
      }}
    >
      {/* Samsung Galaxy Phone Mockup */}
      <div style={{ position: "relative" }}>
        {/* Volume buttons (left) */}
        <div
          style={{
            position: "absolute",
            left: -5,
            top: 140,
            display: "flex",
            flexDirection: "column",
            gap: 10,
            zIndex: 10,
          }}
        >
          {/* Volume Up */}
          <div
            style={{
              width: 4,
              height: 36,
              background: "linear-gradient(90deg, #2A2A2A, #1A1A1A)",
              borderRadius: "2px 0 0 2px",
              boxShadow: "-1px 0 3px rgba(0,0,0,0.5)",
            }}
          />
          {/* Volume Down */}
          <div
            style={{
              width: 4,
              height: 36,
              background: "linear-gradient(90deg, #2A2A2A, #1A1A1A)",
              borderRadius: "2px 0 0 2px",
              boxShadow: "-1px 0 3px rgba(0,0,0,0.5)",
            }}
          />
        </div>

        {/* Power button (right) */}
        <div
          style={{
            position: "absolute",
            right: -5,
            top: 180,
            width: 4,
            height: 50,
            background: "linear-gradient(90deg, #1A1A1A, #2A2A2A)",
            borderRadius: "0 2px 2px 0",
            boxShadow: "1px 0 3px rgba(0,0,0,0.5)",
            zIndex: 10,
          }}
        />

        <div
          style={{
            width: 375,
            height: 812,
            borderRadius: 44,
            overflow: "hidden",
            boxShadow:
              "0 0 0 2px #1A1A1A, 0 0 0 4px #333, 0 0 0 6px #1A1A1A, 0 24px 80px rgba(0,0,0,0.45), 0 8px 32px rgba(0,0,0,0.3)",
            position: "relative",
            display: "flex",
            flexDirection: "column",
            background: "#FFF",
          }}
        >
          {/* Status bar */}
          <div
            className="flex items-center justify-between flex-shrink-0"
            style={{
              padding: "12px 28px 6px",
              fontFamily: "'Pretendard', sans-serif",
              fontSize: 14,
              fontWeight: 600,
              color: statusColor,
              background: isDarkScreen
                ? "linear-gradient(180deg, rgba(84,120,255,0.25), transparent)"
                : "transparent",
              transition: "all 0.5s ease",
            }}
          >
            <span>9:41</span>
            <div className="flex items-center gap-1">
              <svg width="16" height="12" viewBox="0 0 18 12" fill="none">
                <path
                  d="M1 8.5h1.5a.5.5 0 01.5.5v2a.5.5 0 01-.5.5H1a.5.5 0 01-.5-.5V9a.5.5 0 01.5-.5zM5 6h1.5a.5.5 0 01.5.5v4.5a.5.5 0 01-.5.5H5a.5.5 0 01-.5-.5V6.5A.5.5 0 015 6zM9 3.5h1.5a.5.5 0 01.5.5v7a.5.5 0 01-.5.5H9a.5.5 0 01-.5-.5V4a.5.5 0 01.5-.5zM13 1h1.5a.5.5 0 01.5.5v9.5a.5.5 0 01-.5.5H13a.5.5 0 01-.5-.5V1.5a.5.5 0 01.5-.5z"
                  fill={statusColor}
                />
              </svg>
              <svg width="16" height="12" viewBox="0 0 18 12" fill="none">
                <path
                  d="M9 2.5C6.5 2.5 4.2 3.5 2.5 5.2l1.4 1.4C5.4 5.1 7.1 4.3 9 4.3s3.6.8 5.1 2.3l1.4-1.4C13.8 3.5 11.5 2.5 9 2.5z"
                  fill={statusColor}
                  opacity="0.4"
                />
                <path
                  d="M9 6.5c-1.7 0-3.2.7-4.3 1.8L6.1 9.7c.8-.8 1.8-1.3 2.9-1.3s2.1.5 2.9 1.3l1.4-1.4c-1.1-1.1-2.6-1.8-4.3-1.8z"
                  fill={statusColor}
                  opacity="0.7"
                />
                <circle cx="9" cy="11" r="1.2" fill={statusColor} />
              </svg>
              <svg width="26" height="12" viewBox="0 0 27 13" fill="none">
                <rect
                  x="0.5"
                  y="1"
                  width="22"
                  height="11"
                  rx="2.5"
                  stroke={statusColor}
                  strokeOpacity="0.35"
                />
                <rect
                  x="1.5"
                  y="2"
                  width="18"
                  height="9"
                  rx="1.5"
                  fill={statusColor}
                />
                <path
                  d="M23.5 5v3a1.5 1.5 0 000-3z"
                  fill={statusColor}
                  fillOpacity="0.4"
                />
              </svg>
            </div>
          </div>

          {/* Screen content */}
          <div
            className="flex-1 flex flex-col overflow-hidden relative"
            style={{ minHeight: 0 }}
          >
            {renderScreen()}

            {/* Floating Navigation Button (when navi minimized) */}
            {showNavFab && navMinimized && (
              <FloatingNavButton
                destinationName={
                  PLACE_NAMES[navMinimized.placeId] || "Destination"
                }
                progress={navProgress}
                onRestore={handleNavRestore}
                onStop={handleNavStop}
              />
            )}
          </div>

          {/* Bottom Nav (conditional) */}
          {showBottomNav && (
            <BottomNav
              activeTab={getActiveTab(screen)}
              onTabChange={handleTabChange}
            />
          )}

          {/* Home indicator */}
          <div className="flex-shrink-0 flex items-center justify-center pb-2 pt-1 bg-white">
            <div
              style={{
                width: 134,
                height: 5,
                borderRadius: 100,
                background: "#1A1A2E",
                opacity: 0.2,
              }}
            />
          </div>
        </div>
      </div>
    </div>
  );
}