/**
 * fixtures/index.ts — Mock data for UI development
 * Mirrors: Android > fixtures/Fixtures.kt
 *
 * TODO: Replace with real API data when backend is ready.
 *       Delete this file when all mock data is removed.
 */

// ── Shared Place type ──
export interface Place {
  id: string;
  image: string;
  name: string;
  nameKr: string;
  rating: number;
  distance: string;
  tag: string;
  color: string;
}

export interface PlaceDetail extends Place {
  desc: string;
  hours: string;
  fee: string;
  walkTime: string;
}

export interface KoreanPhrase {
  kr: string;
  pron: string;
  en: string;
}

export interface PhraseSection {
  title: string;
  subtitle: string;
  emoji: string;
  color: string;
  phrases: KoreanPhrase[];
}

export interface Theme {
  id: string;
  emoji: string;
  name: string;
  subtitle: string;
  color: string;
  gradient: string;
  places: number;
  featured: string[];
}

export interface StoryPage {
  emoji?: string;
  text: string;
  image?: string;
  imageCaption?: string;
  ambientLabel?: string;
}

// ── Shared constants ──
export const CATEGORIES = [
  { id: "attraction", emoji: "\uD83C\uDFDE\uFE0F", name: "Attraction", sub: "Landmarks & nature", color: "#4CAF50" },
  { id: "culture", emoji: "\uD83C\uDFDB\uFE0F", name: "Culture", sub: "Museums & history", color: "#5478FF" },
  { id: "festival", emoji: "\uD83C\uDF86", name: "Festival", sub: "Events & performances", color: "#E91E63" },
  { id: "course", emoji: "\uD83D\uDDFA\uFE0F", name: "Course", sub: "Travel routes", color: "#00BCD4" },
  { id: "leports", emoji: "\uD83C\uDFC4\u200D\u2642\uFE0F", name: "Leports", sub: "Leisure & sports", color: "#FF9800" },
  { id: "cafe", emoji: "\u2615", name: "Cafes", sub: "Coffee & bakeries", color: "#8D6E63" },
  { id: "shopping", emoji: "\uD83D\uDECD\uFE0F", name: "Shopping", sub: "Markets & malls", color: "#9C27B0" },
  { id: "food", emoji: "\uD83C\uDF5C", name: "Food", sub: "Dining & street eats", color: "#FF6B6B" },
];
