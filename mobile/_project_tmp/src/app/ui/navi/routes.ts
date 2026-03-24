/**
 * routes.ts — Screen types & navigation helpers
 * Mirrors: Android > ui/navi/Routes.kt
 *
 * Auth flow:  Splash -> Welcome -> Login -> GpsPermission -> InterestSelect
 * Main tabs:  Home, Map, Explore, Phrases, MyPage
 * Detail:     Place/{placeId}, Navi/{placeId}, Story/{placeId}
 */

export type NavTab = "main" | "explore" | "phrases";

export type Screen =
  | { type: "splash" }
  | { type: "welcome" }
  | { type: "gps" }
  | { type: "category" }
  | { type: "loading"; categories: string[] }
  | { type: "main"; categories: string[] }
  | { type: "detail"; categories: string[]; placeId: string }
  | { type: "transport"; categories: string[]; placeId: string }
  | { type: "transit-detail"; categories: string[]; placeId: string }
  | { type: "navigation"; categories: string[]; placeId: string; transportMode?: string; transitRouteId?: string }
  | { type: "explore"; categories: string[] }
  | { type: "phrases"; categories: string[] }
  | { type: "map"; categories: string[] }
  | { type: "mypage"; categories: string[] };

export function getActiveTab(screen: Screen): NavTab {
  if (screen.type === "explore") return "explore";
  if (screen.type === "phrases") return "phrases";
  return "main";
}

export function getCategories(screen: Screen): string[] {
  if ("categories" in screen) return screen.categories;
  return [];
}

export const SHOW_NAV_SCREENS = [
  "main",
  "detail",
  "transport",
  "transit-detail",
  "navigation",
  "explore",
  "phrases",
  "map",
  "mypage",
];