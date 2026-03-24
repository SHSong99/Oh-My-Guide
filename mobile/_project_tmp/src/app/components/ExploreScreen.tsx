/**
 * ExploreScreen.tsx — K-Culture Explore
 * Browse local life + K-Pop/K-Drama themed courses,
 * then GPS-navigate spot by spot.
 */
import { useState, useEffect, useRef, useCallback } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import { ImageWithFallback } from "./figma/ImageWithFallback";
import {
  Search, MapPin, ChevronRight, ChevronLeft, Clock, Footprints,
  Star, Navigation, CircleCheckBig, Tv, Music, Store, Clapperboard,
  Utensils, Coffee, Sparkles, Eye, Compass, Play, X, Route,
  Flame, TrendingUp, ArrowRight, Heart,
} from "lucide-react";

/* ══════════════════════════════════════
   DATA
   ══════════════════════════════════════ */
const REGIONS = [
  { id: "all", name: "All" },
  { id: "seoul", name: "Seoul" },
  { id: "busan", name: "Busan" },
  { id: "jeju", name: "Jeju" },
  { id: "gyeongju", name: "Gyeongju" },
  { id: "incheon", name: "Incheon" },
  { id: "jeonju", name: "Jeonju" },
];

interface Spot {
  id: string;
  name: string;
  nameKr: string;
  desc: string;
  image: string;
  lat: number;
  lng: number;
  walkMin: number;
}

interface Course {
  id: string;
  title: string;
  subtitle: string;
  category: "local" | "kpop" | "kdrama";
  region: string;
  image: string;
  duration: string;
  spotCount: number;
  rating: number;
  tags: string[];
  spots: Spot[];
}

const COURSES: Course[] = [
  {
    id: "demon-hunters",
    title: "K-Pop Demon Hunters Course",
    subtitle: "Follow the hottest idol spots in Hongdae & Mapo",
    category: "kpop",
    region: "seoul",
    image: "https://images.unsplash.com/photo-1765375783706-05aeeaf59e5f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMEhvbmdkYWUlMjBzdHJlZXQlMjBuaWdodCUyMG5lb258ZW58MXx8fHwxNzczNjM1MTEzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    duration: "3-4h",
    spotCount: 5,
    rating: 4.8,
    tags: ["K-Pop", "Hongdae", "Idol"],
    spots: [
      { id: "dh1", name: "HYBE Insight", nameKr: "하이브 인사이트", desc: "The official museum of HYBE entertainment. Explore interactive exhibits showcasing BTS, TXT, and more. Photo zones and exclusive merch await.", image: "https://images.unsplash.com/photo-1713816821469-6af8114275c5?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxCVFMlMjBjb25jZXJ0JTIwc3RhZ2UlMjBwdXJwbGUlMjBsaWdodHN8ZW58MXx8fHwxNzczNjM1MTE2fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5260, lng: 127.0107, walkMin: 0 },
      { id: "dh2", name: "Hongdae Busking Stage", nameKr: "홍대 버스킹 무대", desc: "The legendary busking area where many K-Pop idols were first discovered. Watch live street performances every evening.", image: "https://images.unsplash.com/photo-1765375783706-05aeeaf59e5f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMEhvbmdkYWUlMjBzdHJlZXQlMjBuaWdodCUyMG5lb258ZW58MXx8fHwxNzczNjM1MTEzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5563, lng: 126.9237, walkMin: 15 },
      { id: "dh3", name: "SM Entertainment Café", nameKr: "SM 엔터 카페", desc: "Official SM café with themed drinks for EXO, aespa, NCT fans. Seasonal menus change with comebacks.", image: "https://images.unsplash.com/photo-1603685568162-67024e818bec?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMEl0YWV3b24lMjBjYWZlJTIwYWxsZXklMjB0cmVuZHl8ZW58MXx8fHwxNzczNjM1MTQ3fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5500, lng: 126.9222, walkMin: 10 },
      { id: "dh4", name: "MBC World K-Pop Museum", nameKr: "MBC 월드", desc: "Step into the world of Korean music TV. Try the virtual singing stage and see costumes from legendary performances.", image: "https://images.unsplash.com/photo-1768711699153-bd696267e52f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyME15ZW9uZ2RvbmclMjBzaG9wcGluZyUyMG5lb24lMjBzaWduc3xlbnwxfHx8fDE3NzM2MzUxMzl8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5775, lng: 126.8915, walkMin: 20 },
      { id: "dh5", name: "K-Star Road Gangnam", nameKr: "K스타 로드", desc: "Walk among GangnamDol bear statues representing top idol groups. A must-visit photo spot for every K-Pop fan.", image: "https://images.unsplash.com/photo-1768711699153-bd696267e52f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyME15ZW9uZ2RvbmclMjBzaG9wcGluZyUyMG5lb24lMjBzaWduc3xlbnwxfHx8fDE3NzM2MzUxMzl8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5120, lng: 127.0590, walkMin: 25 },
    ],
  },
  {
    id: "bts-busan",
    title: "BTS Busan Course",
    subtitle: "Visit the places where BTS members grew up",
    category: "kpop",
    region: "busan",
    image: "https://images.unsplash.com/photo-1762440775708-7dbfe9e10842?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxCdXNhbiUyMEdhbWNoZW9uJTIwY3VsdHVyZSUyMHZpbGxhZ2UlMjBjb2xvcmZ1bHxlbnwxfHx8fDE3NzM2MzUxMzN8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    duration: "4-5h",
    spotCount: 4,
    rating: 4.9,
    tags: ["BTS", "Busan", "ARMY"],
    spots: [
      { id: "bts1", name: "Jimin's Dance School", nameKr: "지민 댄스 스쿨", desc: "The Just Dance Academy where BTS Jimin trained as a student. Fans often leave post-it notes on the entrance.", image: "https://images.unsplash.com/photo-1762440775708-7dbfe9e10842?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxCdXNhbiUyMEdhbWNoZW9uJTIwY3VsdHVyZSUyMHZpbGxhZ2UlMjBjb2xvcmZ1bHxlbnwxfHx8fDE3NzM2MzUxMzN8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 35.1467, lng: 129.0322, walkMin: 0 },
      { id: "bts2", name: "Gamcheon Culture Village", nameKr: "감천문화마을", desc: "The colorful hillside village with BTS-themed murals and the iconic Little Prince statue. Stunning photo spots.", image: "https://images.unsplash.com/photo-1762440775708-7dbfe9e10842?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxCdXNhbiUyMEdhbWNoZW9uJTIwY3VsdHVyZSUyMHZpbGxhZ2UlMjBjb2xvcmZ1bHxlbnwxfHx8fDE3NzM2MzUxMzN8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 35.0978, lng: 129.0105, walkMin: 25 },
      { id: "bts3", name: "Haeundae Beach", nameKr: "해운대 해변", desc: "The famous beach featured in BTS's 'Yet to Come' MV teaser. Walk along the shoreline for the ultimate ARMY pilgrimage.", image: "https://images.unsplash.com/photo-1768081977305-b5db21d91ec1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxKZWp1JTIwaXNsYW5kJTIwY29hc3QlMjB2b2xjYW5pYyUyMHJvY2tzJTIwb2NlYW58ZW58MXx8fHwxNzczNjM1MTQzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 35.1587, lng: 129.1604, walkMin: 30 },
      { id: "bts4", name: "BIFF Square", nameKr: "BIFF 광장", desc: "The Busan International Film Festival square. Enjoy hotteok and street food while walking where Jungkook used to play.", image: "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBzdHJlZXQlMjBmb29kJTIwbWFya2V0JTIwR3dhbmdqYW5nfGVufDF8fHx8MTc3MzYzNTEzNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 35.0983, lng: 129.0287, walkMin: 15 },
    ],
  },
  {
    id: "cvs-mukbang",
    title: "Convenience Store Mukbang",
    subtitle: "Eat like a local — the ultimate K-CVS food tour",
    category: "local",
    region: "seoul",
    image: "https://images.unsplash.com/photo-1760020890915-ca605575b93b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBjb252ZW5pZW5jZSUyMHN0b3JlJTIwc25hY2tzJTIwbmlnaHR8ZW58MXx8fHwxNzczNjM1MTE5fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    duration: "2-3h",
    spotCount: 4,
    rating: 4.6,
    tags: ["Local", "Food", "Mukbang"],
    spots: [
      { id: "cvs1", name: "CU Flagship Seongsu", nameKr: "CU 성수 플래그십", desc: "Korea's trendiest CU store. Try the famous Triangle Kimbap, corn dogs, and seasonal limited items. Instagrammable interiors!", image: "https://images.unsplash.com/photo-1760020890915-ca605575b93b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBjb252ZW5pZW5jZSUyMHN0b3JlJTIwc25hY2tzJTIwbmlnaHR8ZW58MXx8fHwxNzczNjM1MTE5fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5445, lng: 127.0563, walkMin: 0 },
      { id: "cvs2", name: "GS25 Hangang Park", nameKr: "GS25 한강공원점", desc: "The most scenic convenience store in Seoul. Grab ramyeon and eat by the Han River — a true local experience.", image: "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBzdHJlZXQlMjBmb29kJTIwbWFya2V0JTIwR3dhbmdqYW5nfGVufDF8fHx8MTc3MzYzNTEzNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5275, lng: 126.9340, walkMin: 20 },
      { id: "cvs3", name: "Emart24 Myeongdong", nameKr: "이마트24 명동점", desc: "Tourist-favorite spot with K-snack tasting corner. Local staff picks change weekly. Don't miss the honey butter chips!", image: "https://images.unsplash.com/photo-1768711699153-bd696267e52f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyME15ZW9uZ2RvbmclMjBzaG9wcGluZyUyMG5lb24lMjBzaWduc3xlbnwxfHx8fDE3NzM2MzUxMzl8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5636, lng: 126.9850, walkMin: 15 },
      { id: "cvs4", name: "7-Eleven Itaewon Rooftop", nameKr: "세븐일레븐 이태원 루프탑", desc: "Korea's first rooftop convenience store. Enjoy tteokbokki cups with a view of Seoul's skyline at night.", image: "https://images.unsplash.com/photo-1603685568162-67024e818bec?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMEl0YWV3b24lMjBjYWZlJTIwYWxsZXklMjB0cmVuZHl8ZW58MXx8fHwxNzczNjM1MTQ3fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5345, lng: 126.9946, walkMin: 20 },
    ],
  },
  {
    id: "goblin-filming",
    title: "Goblin Filming Course",
    subtitle: "Walk through the iconic scenes of the legendary K-Drama",
    category: "kdrama",
    region: "seoul",
    image: "https://images.unsplash.com/photo-1748835600895-8ff48c51c37f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMHBhbGFjZSUyMGF1dHVtbiUyMG1hcGxlJTIwYmVhdXRpZnVsfGVufDF8fHx8MTc3MzYzNTEyNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    duration: "3-4h",
    spotCount: 5,
    rating: 4.7,
    tags: ["K-Drama", "Goblin", "Filming"],
    spots: [
      { id: "gb1", name: "Deoksugung Stone Wall Road", nameKr: "덕수궁 돌담길", desc: "The romantic stone wall road where Kim Shin and Eun-tak walked together. One of Seoul's most beloved autumn paths.", image: "https://images.unsplash.com/photo-1748835600895-8ff48c51c37f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMHBhbGFjZSUyMGF1dHVtbiUyMG1hcGxlJTIwYmVhdXRpZnVsfGVufDF8fHx8MTc3MzYzNTEyNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5658, lng: 126.9752, walkMin: 0 },
      { id: "gb2", name: "Bukchon Hanok Village", nameKr: "북촌한옥마을", desc: "The traditional village that appeared as the Goblin's mystical neighborhood. Wind through 600-year-old alleyways.", image: "https://images.unsplash.com/photo-1704240699154-da9e9c690373?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMEJ1a2Nob24lMjBoYW5vayUyMHZpbGxhZ2UlMjB0cmFkaXRpb25hbHxlbnwxfHx8fDE3NzM2MzUxMzB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5826, lng: 126.9831, walkMin: 20 },
      { id: "gb3", name: "Incheon Open Port Area", nameKr: "인천 개항장", desc: "The Grim Reaper's tea shop was filmed here. Beautiful early-20th century colonial architecture lines the streets.", image: "https://images.unsplash.com/photo-1768711699153-bd696267e52f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyME15ZW9uZ2RvbmclMjBzaG9wcGluZyUyMG5lb24lMjBzaWduc3xlbnwxfHx8fDE3NzM2MzUxMzl8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.4737, lng: 126.6217, walkMin: 30 },
      { id: "gb4", name: "Jumunjin Beach", nameKr: "주문진 해변", desc: "Where Eun-tak first summoned the Goblin. The red door on the breakwater is the most famous photo spot from the drama.", image: "https://images.unsplash.com/photo-1768081977305-b5db21d91ec1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxKZWp1JTIwaXNsYW5kJTIwY29hc3QlMjB2b2xjYW5pYyUyMHJvY2tzJTIwb2NlYW58ZW58MXx8fHwxNzczNjM1MTQzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.8894, lng: 128.8306, walkMin: 25 },
      { id: "gb5", name: "BBQ Chicken Goblin Branch", nameKr: "BBQ 치킨 도깨비점", desc: "The actual chicken shop from the drama. Order the same menu as the Goblin — golden olive chicken and beer combo.", image: "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBzdHJlZXQlMjBmb29kJTIwbWFya2V0JTIwR3dhbmdqYW5nfGVufDF8fHx8MTc3MzYzNTEzNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5563, lng: 126.9239, walkMin: 15 },
    ],
  },
  {
    id: "local-market",
    title: "Seoul Local Market Hopping",
    subtitle: "Experience the real Korea at traditional markets",
    category: "local",
    region: "seoul",
    image: "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBzdHJlZXQlMjBmb29kJTIwbWFya2V0JTIwR3dhbmdqYW5nfGVufDF8fHx8MTc3MzYzNTEzNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    duration: "3-4h",
    spotCount: 4,
    rating: 4.7,
    tags: ["Local", "Market", "Street Food"],
    spots: [
      { id: "lm1", name: "Gwangjang Market", nameKr: "광장시장", desc: "Seoul's oldest and most vibrant market. Famous for bindaetteok, mayak gimbap, and the legendary tteokbokki alley.", image: "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBzdHJlZXQlMjBmb29kJTIwbWFya2V0JTIwR3dhbmdqYW5nfGVufDF8fHx8MTc3MzYzNTEzNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5701, lng: 126.9990, walkMin: 0 },
      { id: "lm2", name: "Tongin Market", nameKr: "통인시장", desc: "Use old Korean coins (yeopjeon) to build your own dosirak lunch box. A unique market experience near Gyeongbokgung.", image: "https://images.unsplash.com/photo-1704240699154-da9e9c690373?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMEJ1a2Nob24lMjBoYW5vayUyMHZpbGxhZ2UlMjB0cmFkaXRpb25hbHxlbnwxfHx8fDE3NzM2MzUxMzB8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5790, lng: 126.9690, walkMin: 20 },
      { id: "lm3", name: "Mangwon Market", nameKr: "망원시장", desc: "The hipsters' market. Amazing churros, fresh juice, and locally-made rice cakes. Less touristy and more authentic.", image: "https://images.unsplash.com/photo-1760020890915-ca605575b93b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBjb252ZW5pZW5jZSUyMHN0b3JlJTIwc25hY2tzJTIwbmlnaHR8ZW58MXx8fHwxNzczNjM1MTE5fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5567, lng: 126.9089, walkMin: 25 },
      { id: "lm4", name: "Namdaemun Market", nameKr: "남대문시장", desc: "Korea's largest traditional market with 10,000+ shops. Try the galchi jorim (braised cutlassfish) in the food alley.", image: "https://images.unsplash.com/photo-1768711699153-bd696267e52f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyME15ZW9uZ2RvbmclMjBzaG9wcGluZyUyMG5lb24lMjBzaWduc3xlbnwxfHx8fDE3NzM2MzUxMzl8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 37.5592, lng: 126.9774, walkMin: 15 },
    ],
  },
  {
    id: "jeju-drama",
    title: "Jeju K-Drama Locations",
    subtitle: "Visit famous drama filming spots on Jeju Island",
    category: "kdrama",
    region: "jeju",
    image: "https://images.unsplash.com/photo-1768081977305-b5db21d91ec1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxKZWp1JTIwaXNsYW5kJTIwY29hc3QlMjB2b2xjYW5pYyUyMHJvY2tzJTIwb2NlYW58ZW58MXx8fHwxNzczNjM1MTQzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    duration: "5-6h",
    spotCount: 4,
    rating: 4.5,
    tags: ["K-Drama", "Jeju", "Nature"],
    spots: [
      { id: "jd1", name: "Seopjikoji", nameKr: "섭지코지", desc: "Featured in 'All In' and countless K-Dramas. Dramatic cliffs, lighthouse, and canola flower fields in spring.", image: "https://images.unsplash.com/photo-1768081977305-b5db21d91ec1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxKZWp1JTIwaXNsYW5kJTIwY29hc3QlMjB2b2xjYW5pYyUyMHJvY2tzJTIwb2NlYW58ZW58MXx8fHwxNzczNjM1MTQzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 33.4237, lng: 126.9308, walkMin: 0 },
      { id: "jd2", name: "Camellia Hill", nameKr: "카멜리아힐", desc: "A beautiful garden with 6,000 camellia trees, used in the drama 'Warm and Cozy'. Photogenic in every season.", image: "https://images.unsplash.com/photo-1748835600895-8ff48c51c37f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMHBhbGFjZSUyMGF1dHVtbiUyMG1hcGxlJTIwYmVhdXRpZnVsfGVufDF8fHx8MTc3MzYzNTEyNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 33.2903, lng: 126.3685, walkMin: 30 },
      { id: "jd3", name: "Udo Island", nameKr: "우도", desc: "The small island off Jeju featured in 'Twenty-Five Twenty-One'. Crystal clear waters and peanut ice cream paradise.", image: "https://images.unsplash.com/photo-1768081977305-b5db21d91ec1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxKZWp1JTIwaXNsYW5kJTIwY29hc3QlMjB2b2xjYW5pYyUyMHJvY2tzJTIwb2NlYW58ZW58MXx8fHwxNzczNjM1MTQzfDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 33.5040, lng: 126.9536, walkMin: 45 },
      { id: "jd4", name: "Hallim Park", nameKr: "한림공원", desc: "Subtropical gardens and lava caves. Used in 'Welcome to Waikiki' filming. The palm tree road is iconic.", image: "https://images.unsplash.com/photo-1748835600895-8ff48c51c37f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMHBhbGFjZSUyMGF1dHVtbiUyMG1hcGxlJTIwYmVhdXRpZnVsfGVufDF8fHx8MTc3MzYzNTEyNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", lat: 33.3465, lng: 126.2397, walkMin: 35 },
    ],
  },
];

const CATEGORY_META: Record<string, { icon: React.ElementType; color: string; bg: string; label: string }> = {
  local:  { icon: Store,        color: "#16A34A", bg: "#F0FDF4", label: "Local Life" },
  kpop:   { icon: Music,        color: "#7C3AED", bg: "#F5F3FF", label: "K-Pop" },
  kdrama: { icon: Clapperboard, color: "#EA580C", bg: "#FFF7ED", label: "K-Drama" },
};

/* ── Haversine distance ── */
function haversineDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371000;
  const toRad = (deg: number) => (deg * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLng = toRad(lng2 - lng1);
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

const delay = (ms: number) => new Promise(r => setTimeout(r, ms));

/* ── Hero banner images ── */
const HERO_BANNERS = [
  { image: "https://images.unsplash.com/photo-1762440127280-3d9a764d3821?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxTZW91bCUyMG5lb24lMjBuaWdodCUyMHN0cmVldCUyMHZpYnJhbnR8ZW58MXx8fHwxNzczNzA3MjI3fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", title: "Dive into\nK-Culture", sub: "Curated courses by locals & K-fans", accent: "#FF6B6B" },
  { image: "https://images.unsplash.com/photo-1760539618919-5516b979bab4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLcG9wJTIwY29uY2VydCUyMHN0YWdlJTIwY29sb3JmdWwlMjBsaWdodHN8ZW58MXx8fHwxNzczNzA3MjI4fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", title: "Follow\nYour Idols", sub: "K-Pop hotspots across Korea", accent: "#A855F7" },
  { image: "https://images.unsplash.com/photo-1768006379575-f0b096db6289?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjB0cmFkaXRpb25hbCUyMHBhbGFjZSUyMGF1dHVtbiUyMGJlYXV0aWZ1bHxlbnwxfHx8fDE3NzM3MDcyMjh8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral", title: "Walk the\nDrama Scenes", sub: "Iconic K-Drama filming locations", accent: "#F59E0B" },
];

const CAT_HERO_IMAGES: Record<string, string> = {
  local: "https://images.unsplash.com/photo-1628532431030-3b6d433ed166?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjBzdHJlZXQlMjBmb29kJTIwbWFya2V0JTIwR3dhbmdqYW5nfGVufDF8fHx8MTc3MzYzNTEzNnww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
  kpop: "https://images.unsplash.com/photo-1760539618919-5516b979bab4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLcG9wJTIwY29uY2VydCUyMHN0YWdlJTIwY29sb3JmdWwlMjBsaWdodHN8ZW58MXx8fHwxNzczNzA3MjI4fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
  kdrama: "https://images.unsplash.com/photo-1768006379575-f0b096db6289?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxLb3JlYW4lMjB0cmFkaXRpb25hbCUyMHBhbGFjZSUyMGF1dHVtbiUyMGJlYXV0aWZ1bHxlbnwxfHx8fDE3NzM3MDcyMjh8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
};

/* ══════════════════════════════════════
   COURSE CARD (immersive full-bleed)
   ══════════════════════════════════════ */
function CourseCard({ course, onSelect, featured }: { course: Course; onSelect: () => void; featured?: boolean }) {
  const cat = CATEGORY_META[course.category];
  const CatIcon = cat.icon;
  const h = featured ? 200 : 160;
  return (
    <div onClick={onSelect} className="cursor-pointer animate-fadeUp active:scale-[0.98] transition-transform" style={{ borderRadius: 22, overflow: "hidden", boxShadow: "0 8px 32px rgba(0,0,0,0.12)" }}>
      <div className="relative" style={{ height: h }}>
        <ImageWithFallback src={course.image} alt={course.title} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
        <div className="absolute inset-0" style={{ background: "linear-gradient(to top, rgba(0,0,0,0.75) 0%, rgba(0,0,0,0.15) 40%, transparent 70%)" }} />
        {/* Category badge */}
        <div className="absolute top-3 left-3 flex items-center gap-1.5 px-2.5 py-1 rounded-xl" style={{ background: `${cat.color}E6`, backdropFilter: "blur(8px)" }}>
          <CatIcon size={11} color="#fff" strokeWidth={2.5} />
          <span className="text-[10px] text-white" style={{ fontWeight: 700 }}>{cat.label}</span>
        </div>
        {/* Rating + heart */}
        <div className="absolute top-3 right-3 flex items-center gap-1.5">
          <div className="flex items-center gap-1 px-2 py-1 rounded-xl" style={{ background: "rgba(0,0,0,0.45)", backdropFilter: "blur(8px)" }}>
            <Star size={10} color="#FFDE42" strokeWidth={0} fill="#FFDE42" />
            <span className="text-[10px] text-white" style={{ fontWeight: 700 }}>{course.rating}</span>
          </div>
        </div>
        {/* Tags floating */}
        <div className="absolute top-3 left-[110px] flex gap-1.5">
          {course.tags.slice(0, 2).map(t => (
            <span key={t} className="px-2 py-0.5 rounded-md text-[9px] text-white/90" style={{ fontWeight: 600, background: "rgba(255,255,255,0.18)", backdropFilter: "blur(4px)" }}>#{t}</span>
          ))}
        </div>
        {/* Bottom content */}
        <div className="absolute bottom-0 left-0 right-0 p-4">
          <div className="text-[18px] text-white mb-1" style={{ fontWeight: 800, lineHeight: 1.2, textShadow: "0 2px 12px rgba(0,0,0,0.4)" }}>{course.title}</div>
          <div className="text-[12px] text-white/75 mb-3" style={{ fontWeight: 500, lineHeight: 1.4 }}>{course.subtitle}</div>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] text-white" style={{ fontWeight: 600, background: "rgba(255,255,255,0.15)", backdropFilter: "blur(4px)" }}>
                <MapPin size={10} strokeWidth={2.5} /> {course.spotCount} spots
              </span>
              <span className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-[10px] text-white/80" style={{ fontWeight: 500, background: "rgba(255,255,255,0.1)" }}>
                <Clock size={10} strokeWidth={2} /> {course.duration}
              </span>
            </div>
            <div className="w-8 h-8 rounded-full flex items-center justify-center" style={{ background: "rgba(255,255,255,0.2)", backdropFilter: "blur(6px)" }}>
              <ArrowRight size={14} color="#fff" strokeWidth={2.5} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════
   COURSE DETAIL VIEW
   ══════════════════════════════════════ */
function CourseDetailView({ course, onBack, onStartNavi }: { course: Course; onBack: () => void; onStartNavi: () => void }) {
  const cat = CATEGORY_META[course.category];
  const CatIcon = cat.icon;
  return (
    <div className="flex-1 flex flex-col overflow-hidden animate-screenSwitch">
      {/* Hero */}
      <div className="relative flex-shrink-0" style={{ height: 180 }}>
        <ImageWithFallback src={course.image} alt={course.title} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
        <div className="absolute inset-0" style={{ background: "linear-gradient(to top, #fff 0%, rgba(255,255,255,0.3) 30%, transparent 60%)" }} />
        <button onClick={onBack} className="absolute top-3 left-3 w-8 h-8 bg-white/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-sm border border-gray-100">
          <ChevronLeft size={18} color="#1A1A2E" strokeWidth={2.5} />
        </button>
        <div className="absolute top-3 right-3 flex items-center gap-1 px-2.5 py-1 rounded-lg" style={{ background: cat.bg, border: `1px solid ${cat.color}22` }}>
          <CatIcon size={12} color={cat.color} strokeWidth={2.5} />
          <span className="text-[11px]" style={{ fontWeight: 700, color: cat.color }}>{cat.label}</span>
        </div>
      </div>

      {/* Info */}
      <div className="px-5 -mt-4 relative z-10">
        <h1 className="text-[20px] text-[#1A1A2E]" style={{ fontWeight: 800, lineHeight: 1.2 }}>{course.title}</h1>
        <p className="text-[12px] text-[#8892A4] mt-1.5" style={{ lineHeight: 1.5 }}>{course.subtitle}</p>
        <div className="flex items-center gap-4 mt-3">
          <span className="flex items-center gap-1.5 text-[11px] text-[#5478FF]" style={{ fontWeight: 600 }}>
            <MapPin size={12} strokeWidth={2.5} /> {course.spotCount} spots
          </span>
          <span className="flex items-center gap-1.5 text-[11px] text-[#8892A4]" style={{ fontWeight: 500 }}>
            <Clock size={12} strokeWidth={2} /> {course.duration}
          </span>
          <span className="flex items-center gap-1.5 text-[11px] text-[#8892A4]" style={{ fontWeight: 500 }}>
            <Star size={12} color="#FFDE42" strokeWidth={0} fill="#FFDE42" /> {course.rating}
          </span>
        </div>
      </div>

      {/* Tags */}
      <div className="flex gap-1.5 px-5 mt-3 mb-3">
        {course.tags.map(t => (
          <span key={t} className="px-2.5 py-1 rounded-full text-[10px] bg-[#F0F4FF] text-[#5478FF]" style={{ fontWeight: 600 }}>#{t}</span>
        ))}
      </div>

      <div className="h-[1px] bg-gray-100 mx-5" />

      {/* Spot list */}
      <div className="flex-1 overflow-y-auto hide-scrollbar px-5 pt-4 pb-24">
        <div className="text-[12px] text-[#8892A4] mb-3" style={{ fontWeight: 600, letterSpacing: "0.05em" }}>COURSE ROUTE</div>
        {course.spots.map((spot, i) => (
          <div key={spot.id} className="flex gap-3 mb-4">
            {/* Timeline */}
            <div className="flex flex-col items-center flex-shrink-0" style={{ width: 28 }}>
              <div className="w-7 h-7 rounded-full flex items-center justify-center text-white text-[11px]" style={{ fontWeight: 800, background: i === 0 ? "linear-gradient(135deg, #5478FF, #7C98FF)" : "#D6DCEB" }}>
                {i + 1}
              </div>
              {i < course.spots.length - 1 && (
                <div className="flex-1 flex flex-col items-center py-1">
                  <div className="w-[1.5px] flex-1 bg-[#E8ECF4]" />
                  <span className="text-[8px] text-[#A0AABF] my-1 flex items-center gap-0.5" style={{ fontWeight: 600 }}>
                    <Footprints size={7} strokeWidth={2.5} /> {course.spots[i + 1].walkMin}m
                  </span>
                  <div className="w-[1.5px] flex-1 bg-[#E8ECF4]" />
                </div>
              )}
            </div>
            {/* Card */}
            <div className="flex-1 bg-[#F8FAFF] rounded-2xl overflow-hidden border border-[#F0F2F5]">
              <div className="relative" style={{ height: 80 }}>
                <ImageWithFallback src={spot.image} alt={spot.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                <div className="absolute inset-0" style={{ background: "linear-gradient(to top, rgba(0,0,0,0.4) 0%, transparent 50%)" }} />
                <div className="absolute bottom-2 left-2.5 right-2.5">
                  <div className="text-[13px] text-white" style={{ fontWeight: 700 }}>{spot.name}</div>
                  <div className="text-[10px] text-white/80" style={{ fontWeight: 500 }}>{spot.nameKr}</div>
                </div>
              </div>
              <div className="px-3 py-2.5">
                <p className="text-[11px] text-[#4A5568]" style={{ lineHeight: 1.5 }}>
                  {spot.desc.length > 80 ? spot.desc.slice(0, 80) + "..." : spot.desc}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Start button */}
      <div className="absolute bottom-0 left-0 right-0 px-5 pb-4 pt-8" style={{ background: "linear-gradient(to top, #fff 60%, transparent)" }}>
        <button
          onClick={onStartNavi}
          className="w-full flex items-center justify-center gap-2 py-4 rounded-2xl text-white text-[15px] transition-transform active:scale-[0.98]"
          style={{ fontWeight: 700, background: "linear-gradient(135deg, #325BFF, #5478FF, #7C98FF)", boxShadow: "0 8px 24px rgba(84,120,255,0.35)" }}
        >
          <Navigation size={17} strokeWidth={2.5} fill="white" fillOpacity={0.2} />
          Start Course Navigation
        </button>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════
   COURSE MAP CANVAS
   ══════════════════════════════════════ */
function CourseMapCanvas({ spots, currentIdx, arrivedSet, mapRatio }: { spots: Spot[]; currentIdx: number; arrivedSet: Set<number>; mapRatio: number }) {
  const isDetailed = mapRatio > 0.55;
  const positions = spots.map((_, i) => {
    const t = i / Math.max(spots.length - 1, 1);
    const x = 60 + (i % 2 === 0 ? t * 200 + 40 : t * 200 + 120);
    const y = 360 - t * 290;
    return { x, y };
  });
  const pathD = positions.map((p, i) => `${i === 0 ? "M" : "L"} ${p.x} ${p.y}`).join(" ");

  return (
    <div className="absolute inset-0 overflow-hidden" style={{ background: "#EDF2F7" }}>
      <svg width="100%" height="100%" viewBox="0 0 400 500" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid slice">
        <path d="M -20 180 Q 80 160 160 200 Q 240 240 320 210 Q 380 190 440 220" fill="none" stroke="#B3D9F2" strokeWidth="18" strokeLinecap="round" />
        <path d="M -20 180 Q 80 160 160 200 Q 240 240 320 210 Q 380 190 440 220" fill="none" stroke="#9ECAE1" strokeWidth="10" strokeLinecap="round" />
        <ellipse cx="280" cy="120" rx="50" ry="30" fill="#C6E5B3" opacity="0.5" />
        <ellipse cx="100" cy="350" rx="35" ry="22" fill="#C6E5B3" opacity="0.4" />
        <ellipse cx="340" cy="380" rx="40" ry="25" fill="#C6E5B3" opacity="0.45" />
        <rect x="30" y="30" width="70" height="55" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="120" y="20" width="55" height="65" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="195" y="30" width="80" height="50" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="300" y="25" width="60" height="60" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="20" y="110" width="60" height="50" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="100" y="100" width="45" height="60" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="170" y="95" width="70" height="55" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="30" y="240" width="80" height="55" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="130" y="250" width="60" height="50" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="210" y="240" width="75" height="60" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="310" y="250" width="55" height="50" rx="4" fill="#DDE3EC" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="40" y="380" width="50" height="60" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <rect x="220" y="375" width="65" height="55" rx="4" fill="#E2E8F0" stroke="#CBD2DC" strokeWidth="0.8" />
        <line x1="0" y1="90" x2="400" y2="90" stroke="#FFFFFF" strokeWidth="7" />
        <line x1="0" y1="310" x2="400" y2="310" stroke="#FFFFFF" strokeWidth="7" />
        <line x1="0" y1="170" x2="400" y2="170" stroke="#FFFFFF" strokeWidth="5" opacity="0.8" />
        <line x1="0" y1="450" x2="400" y2="450" stroke="#FFFFFF" strokeWidth="5" opacity="0.8" />
        <line x1="160" y1="0" x2="160" y2="500" stroke="#FFFFFF" strokeWidth="6" />
        <line x1="300" y1="0" x2="300" y2="500" stroke="#FFFFFF" strokeWidth="6" />
        <line x1="90" y1="0" x2="90" y2="500" stroke="#FFFFFF" strokeWidth="4" opacity="0.7" />
        <line x1="220" y1="0" x2="220" y2="500" stroke="#FFFFFF" strokeWidth="4" opacity="0.7" />
        {isDetailed && (
          <>
            <text x="200" y="85" textAnchor="middle" fill="#A0AEC0" fontSize="7" fontWeight="600" fontFamily="Pretendard, sans-serif">Jongno-daero</text>
            <text x="200" y="305" textAnchor="middle" fill="#A0AEC0" fontSize="7" fontWeight="600" fontFamily="Pretendard, sans-serif">Eulji-ro</text>
          </>
        )}
        {/* Course route */}
        <path d={pathD} fill="none" stroke="#5478FF" strokeWidth="5" strokeLinecap="round" strokeLinejoin="round" opacity="0.25" />
        <path d={pathD} fill="none" stroke="#5478FF" strokeWidth="5" strokeDasharray="8 10" strokeLinecap="round" strokeLinejoin="round" opacity="0.8" style={{ animation: "dash 1.5s linear infinite" }} />
        {/* Spot markers */}
        {positions.map((p, i) => {
          const arrived = arrivedSet.has(i);
          const isCurrent = i === currentIdx;
          const markerColor = arrived ? "#16A34A" : isCurrent ? "#FF5252" : "#5478FF";
          return (
            <g key={spots[i].id}>
              {isCurrent ? (
                <>
                  <path d={`M${p.x} ${p.y - 30}C${p.x - 9} ${p.y - 30} ${p.x - 14} ${p.y - 22} ${p.x - 14} ${p.y - 16}c0 10 14 22 14 22s14-12 14-22c0-6-5-14-14-14z`} fill="#FF5252" />
                  <circle cx={p.x} cy={p.y - 17} r="4.5" fill="white" />
                </>
              ) : (
                <>
                  <circle cx={p.x} cy={p.y} r={arrived ? 9 : 8} fill="white" stroke={markerColor} strokeWidth="2.5" />
                  <text x={p.x} y={p.y + 3.5} textAnchor="middle" fill={markerColor} fontSize="8" fontWeight="800" fontFamily="Pretendard, sans-serif">
                    {arrived ? "\u2713" : i + 1}
                  </text>
                </>
              )}
              {isDetailed && (
                <>
                  <rect x={p.x - 30} y={isCurrent ? p.y + 4 : p.y + 12} width="60" height="14" rx="4" fill="white" filter="url(#cSh)" />
                  <text x={p.x} y={isCurrent ? p.y + 14 : p.y + 22} textAnchor="middle" fill="#1A1A2E" fontSize="6" fontWeight="700" fontFamily="Pretendard, sans-serif">
                    {spots[i].nameKr.length > 7 ? spots[i].nameKr.slice(0, 7) + ".." : spots[i].nameKr}
                  </text>
                </>
              )}
            </g>
          );
        })}
        <defs><filter id="cSh" x="-20%" y="-20%" width="140%" height="140%"><feDropShadow dx="0" dy="1" stdDeviation="2" floodColor="#000" floodOpacity="0.1" /></filter></defs>
      </svg>
      {/* User pulse */}
      <div className="absolute z-20" style={{ top: `${(positions[currentIdx]?.y / 500) * 100 + 3}%`, left: `${(positions[currentIdx]?.x / 400) * 100 - 2}%`, transform: "translate(-50%, -50%)" }}>
        <div className="w-4 h-4 bg-[#5478FF] rounded-full border-[2.5px] border-white shadow-[0_2px_10px_rgba(84,120,255,0.6)] relative">
          <div className="absolute inset-0 bg-[#5478FF] rounded-full animate-ping opacity-40" />
        </div>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════
   COURSE NAVI VIEW (Map + Bottom Sheet)
   ══════════════════════════════════════ */
const CSNAP_MAP = 78;
const CSNAP_SPLIT = 40;
const CSNAP_CHAT = 8;
function cNearestSnap(pct: number): number {
  const snaps = [CSNAP_CHAT, CSNAP_SPLIT, CSNAP_MAP];
  let best = snaps[0], bestDist = Math.abs(pct - snaps[0]);
  for (const s of snaps) { const d = Math.abs(pct - s); if (d < bestDist) { best = s; bestDist = d; } }
  return best;
}

function CourseNaviView({ course, onBack, onComplete }: { course: Course; onBack: () => void; onComplete: () => void }) {
  const containerRef = useRef<HTMLDivElement>(null);
  const scrollRef2 = useRef<HTMLDivElement>(null);
  const dragRef = useRef<{ startY: number; startPct: number; isDragging: boolean }>({ startY: 0, startPct: CSNAP_SPLIT, isDragging: false });

  const [currentSpotIdx, setCurrentSpotIdx] = useState(0);
  const [arrivedSpots, setArrivedSpots] = useState<Set<number>>(new Set());
  const [distance, setDistance] = useState<number | null>(null);
  const [gpsStatus, setGpsStatus] = useState<"watching" | "arrived" | "unavailable">("watching");
  const [sheetPct, setSheetPct] = useState(CSNAP_SPLIT);
  const [isSnapping, setIsSnapping] = useState(false);
  const arrivedRef = useRef(false);

  const spot = course.spots[currentSpotIdx];
  const isLastSpot = currentSpotIdx >= course.spots.length - 1;
  const allDone = arrivedSpots.size === course.spots.length;
  const progress = Math.round((arrivedSpots.size / course.spots.length) * 100);
  const mapRatio = sheetPct / 100;
  const isChatMinimised = sheetPct >= CSNAP_MAP - 5;
  const cat = CATEGORY_META[course.category];

  useEffect(() => {
    if (scrollRef2.current) setTimeout(() => { scrollRef2.current && (scrollRef2.current.scrollTop = scrollRef2.current.scrollHeight); }, 100);
  }, [currentSpotIdx, gpsStatus]);

  /* GPS watcher */
  useEffect(() => {
    arrivedRef.current = false;
    setGpsStatus("watching");
    setDistance(null);
    if (!navigator.geolocation) { setGpsStatus("unavailable"); return; }
    const wid = navigator.geolocation.watchPosition(
      (pos) => {
        const d = haversineDistance(pos.coords.latitude, pos.coords.longitude, spot.lat, spot.lng);
        setDistance(Math.round(d));
        if (d <= 100 && pos.coords.accuracy < 200 && !arrivedRef.current) {
          arrivedRef.current = true;
          setGpsStatus("arrived");
          setArrivedSpots(prev => new Set([...prev, currentSpotIdx]));
        }
      },
      () => setGpsStatus("unavailable"),
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 5000 }
    );
    return () => navigator.geolocation.clearWatch(wid);
  }, [currentSpotIdx, spot.lat, spot.lng]);

  /* Drag handlers */
  const getH = () => containerRef.current?.getBoundingClientRect().height ?? 1;
  const onDS = useCallback((y: number) => { dragRef.current = { startY: y, startPct: sheetPct, isDragging: true }; setIsSnapping(false); }, [sheetPct]);
  const onDM = useCallback((y: number) => { if (!dragRef.current.isDragging) return; const dPct = ((y - dragRef.current.startY) / getH()) * 100; setSheetPct(Math.min(Math.max(dragRef.current.startPct + dPct, CSNAP_CHAT - 2), CSNAP_MAP + 2)); }, []);
  const onDE = useCallback(() => { if (!dragRef.current.isDragging) return; dragRef.current.isDragging = false; setIsSnapping(true); setSheetPct(p => cNearestSnap(p)); }, []);
  const hTS = useCallback((e: React.TouchEvent) => onDS(e.touches[0].clientY), [onDS]);
  const hTM = useCallback((e: React.TouchEvent) => onDM(e.touches[0].clientY), [onDM]);
  const hTE = useCallback(() => onDE(), [onDE]);
  const hMD = useCallback((e: React.MouseEvent) => {
    e.preventDefault(); onDS(e.clientY);
    const mv = (ev: MouseEvent) => onDM(ev.clientY);
    const up = () => { onDE(); window.removeEventListener("mousemove", mv); window.removeEventListener("mouseup", up); };
    window.addEventListener("mousemove", mv); window.addEventListener("mouseup", up);
  }, [onDS, onDM, onDE]);

  const handleManualArrive = () => { arrivedRef.current = true; setGpsStatus("arrived"); setArrivedSpots(prev => new Set([...prev, currentSpotIdx])); };
  const handleNextSpot = () => { if (isLastSpot) { onComplete(); return; } setCurrentSpotIdx(i => i + 1); };

  const distStr = distance !== null ? (distance >= 1000 ? `${(distance / 1000).toFixed(1)}km` : `${distance}m`) : "1.2km";
  const etaStr = distance !== null ? `~${Math.max(1, Math.round(distance / 80))} min` : "~15 min";

  return (
    <div ref={containerRef} className="w-full h-full relative bg-[#EDF2F7] font-['Pretendard'] overflow-hidden animate-screenSwitch">

      {/* ═══ Map ═══ */}
      <div className="absolute inset-0 z-0">
        <CourseMapCanvas spots={course.spots} currentIdx={currentSpotIdx} arrivedSet={arrivedSpots} mapRatio={mapRatio} />
      </div>

      {/* ═══ Header ═══ */}
      <div className="absolute top-0 left-0 right-0 z-40 bg-gradient-to-b from-white/80 via-white/40 to-transparent pt-3 pb-6 px-4">
        <div className="flex items-center justify-between">
          <button onClick={onBack} className="w-9 h-9 bg-white/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-sm border border-gray-100">
            <ChevronLeft size={18} color="#1A1A2E" strokeWidth={2.5} />
          </button>
          <div className="bg-white/95 backdrop-blur-md px-4 py-2 rounded-full shadow-sm border border-gray-100 flex items-center gap-2">
            {gpsStatus === "arrived" ? (
              <span className="text-[13px] text-[#16A34A] flex items-center gap-1.5" style={{ fontWeight: 800 }}>
                <CircleCheckBig size={14} color="#16A34A" strokeWidth={2.5} /> Arrived!
              </span>
            ) : (
              <>
                <Navigation size={13} color="#5478FF" strokeWidth={2.5} />
                <span className="text-[13px] text-[#5478FF]" style={{ fontWeight: 800 }}>{distStr}</span>
                <span className="text-[11px] text-[#8892A4]" style={{ fontWeight: 600 }}>&middot; {etaStr}</span>
              </>
            )}
          </div>
          <button onClick={onComplete} className="w-9 h-9 bg-white/90 backdrop-blur-md rounded-full flex items-center justify-center shadow-sm border border-gray-100">
            <X size={16} color="#1A1A2E" strokeWidth={2.5} />
          </button>
        </div>
      </div>

      {/* ═══ ETA card (map maximized) ═══ */}
      {isChatMinimised && (
        <div className="absolute z-30 animate-fadeUp" style={{ top: `${sheetPct - 18}%`, left: 16, right: 16 }}>
          <div className="bg-white/95 backdrop-blur-xl rounded-2xl p-4 shadow-lg border border-gray-100">
            <div className="flex items-center justify-between mb-2.5">
              <div>
                <div className="text-[10px] text-[#8892A4] uppercase tracking-wider" style={{ fontWeight: 600 }}>Spot {currentSpotIdx + 1} of {course.spots.length}</div>
                <div className="text-[15px] text-[#1A1A2E] mt-0.5" style={{ fontWeight: 700 }}>{spot.name}</div>
              </div>
              <div className="text-right">
                {gpsStatus === "arrived" ? (
                  <div className="text-[18px] text-[#16A34A] flex items-center justify-end gap-1" style={{ fontWeight: 800 }}><CircleCheckBig size={16} color="#16A34A" strokeWidth={2.5} /> Arrived</div>
                ) : (
                  <>
                    <div className="text-[18px] text-[#5478FF]" style={{ fontWeight: 800 }}>{distStr}</div>
                    <div className="text-[10px] text-[#8892A4]" style={{ fontWeight: 500 }}>{etaStr}</div>
                  </>
                )}
              </div>
            </div>
            <div className="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
              <div className={`h-full rounded-full transition-all duration-1000 ease-out ${allDone ? "bg-[#16A34A]" : "bg-gradient-to-r from-[#5478FF] to-[#7C98FF]"}`} style={{ width: `${progress}%` }} />
            </div>
          </div>
        </div>
      )}

      {/* ═══ Bottom Sheet ═══ */}
      <div
        className={`absolute left-0 right-0 bottom-0 z-30 flex flex-col bg-white rounded-t-[24px] shadow-[0_-8px_30px_rgba(0,0,0,0.12)] ${isSnapping ? "transition-[top] duration-400 ease-[cubic-bezier(0.32,0.72,0,1)]" : ""}`}
        style={{ top: `${sheetPct}%` }}
      >
        {/* Drag Handle */}
        <div className="flex-shrink-0 cursor-grab active:cursor-grabbing select-none touch-none" onTouchStart={hTS} onTouchMove={hTM} onTouchEnd={hTE} onMouseDown={hMD}>
          <div className="flex justify-center pt-3 pb-1"><div className="w-10 h-1 bg-gray-300 rounded-full" /></div>
          <div className="flex items-center justify-between px-5 pb-3 pt-1">
            <div className="flex items-center gap-2.5">
              <div className="relative w-8 h-8 rounded-full bg-[#F0F4FF] flex items-center justify-center">
                <MapPin size={14} color="#5478FF" strokeWidth={2.5} />
                <span className={`absolute -top-0.5 -right-0.5 w-2.5 h-2.5 rounded-full border-[1.5px] border-white ${gpsStatus === "arrived" ? "bg-[#16A34A]" : distance !== null ? "bg-[#16A34A] animate-pulse" : "bg-[#F59E0B]"}`} />
              </div>
              <div>
                <div className="text-[13px] text-[#1A1A2E]" style={{ fontWeight: 700 }}>{spot.name}</div>
                <div className="text-[11px] text-[#8892A4]" style={{ fontWeight: 500 }}>{spot.nameKr}</div>
              </div>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-16 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div className={`h-full rounded-full transition-all duration-700 ${allDone ? "bg-[#16A34A]" : "bg-[#5478FF]"}`} style={{ width: `${progress}%` }} />
              </div>
              <span className="text-[10px] text-[#8892A4]" style={{ fontWeight: 600 }}>{progress}%</span>
            </div>
          </div>
        </div>

        <div className="h-[1px] bg-gray-100 mx-4" />

        {/* Sheet content */}
        <div ref={scrollRef2} className="flex-1 overflow-y-auto overflow-x-hidden hide-scrollbar px-4 pt-4 pb-6">

          {/* POI Hero Card */}
          <div className="w-full mt-1 mb-3 rounded-[20px] overflow-hidden shadow-[0_8px_30px_rgba(0,0,0,0.12)] animate-fadeUp">
            <div className="relative w-full aspect-[16/10]">
              <ImageWithFallback src={spot.image} alt={spot.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
              <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
              {/* Tags */}
              <div className="absolute top-3 left-3 flex items-center gap-2">
                {(() => { const CI = cat.icon; return (
                  <span className="px-2.5 py-1 rounded-lg text-[10px] flex items-center gap-1" style={{ background: "rgba(255,255,255,0.9)", fontWeight: 700, color: cat.color }}><CI size={11} strokeWidth={2.5} /> {cat.label}</span>
                ); })()}
                <span className="bg-white/20 backdrop-blur-md text-white px-2.5 py-1 rounded-lg text-[10px]" style={{ fontWeight: 700 }}>#{course.tags[0]}</span>
              </div>
              {/* Rating */}
              <div className="absolute top-3 right-3 bg-black/50 backdrop-blur-sm text-white px-2.5 py-1 rounded-lg text-[11px] flex items-center gap-1" style={{ fontWeight: 700 }}>
                <Star size={10} color="#FFDE42" strokeWidth={0} fill="#FFDE42" /> {course.rating}
              </div>
              {/* Title + Right here */}
              <div className="absolute bottom-0 left-0 right-0 p-4">
                <div className="flex items-end justify-between">
                  <div>
                    <h2 className="text-[24px] text-white drop-shadow-lg" style={{ fontWeight: 800, lineHeight: 1.15 }}>{spot.name}</h2>
                    <p className="text-[13px] text-white/80 mt-1" style={{ fontWeight: 500 }}>{spot.nameKr}</p>
                  </div>
                  {gpsStatus !== "arrived" && (
                    <button onClick={handleManualArrive} className="flex items-center gap-1 bg-white/20 backdrop-blur-sm px-2.5 py-1.5 rounded-lg">
                      <MapPin size={12} color="white" strokeWidth={2.5} />
                      <span className="text-[11px] text-white" style={{ fontWeight: 700 }}>Right here</span>
                    </button>
                  )}
                </div>
              </div>
            </div>
            {/* Description */}
            <div className="bg-white px-4 py-3.5">
              <p className="text-[13px] text-[#4A5568] leading-[1.7]" style={{ fontWeight: 500 }}>{spot.desc}</p>
            </div>
          </div>

          {/* Spot timeline chips */}
          <div className="mb-3">
            <div className="text-[11px] text-[#8892A4] mb-2" style={{ fontWeight: 600, letterSpacing: "0.05em" }}>COURSE PROGRESS</div>
            <div className="flex gap-2 overflow-x-auto hide-scrollbar pb-2">
              {course.spots.map((s, i) => (
                <div key={s.id} onClick={() => { if (i <= currentSpotIdx || arrivedSpots.has(i)) setCurrentSpotIdx(i); }}
                  className="flex-shrink-0 flex items-center gap-2 px-3 py-2 rounded-xl border cursor-pointer"
                  style={{ background: arrivedSpots.has(i) ? "#F0FDF4" : i === currentSpotIdx ? "#EEF2FF" : "#F8FAFF", borderColor: arrivedSpots.has(i) ? "#16A34A22" : i === currentSpotIdx ? "#5478FF33" : "#F0F2F5", opacity: i > currentSpotIdx && !arrivedSpots.has(i) ? 0.5 : 1 }}
                >
                  <div className="w-5 h-5 rounded-full flex items-center justify-center text-[9px] text-white" style={{ fontWeight: 800, background: arrivedSpots.has(i) ? "#16A34A" : i === currentSpotIdx ? "#5478FF" : "#D6DCEB" }}>
                    {arrivedSpots.has(i) ? <CircleCheckBig size={11} strokeWidth={3} /> : i + 1}
                  </div>
                  <span className="text-[10px] text-[#4A5568] whitespace-nowrap" style={{ fontWeight: 600 }}>{s.name.length > 12 ? s.name.slice(0, 12) + ".." : s.name}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Action button */}
          {gpsStatus === "arrived" && (
            <div className="mb-4 animate-fadeUp">
              <button onClick={handleNextSpot}
                className="w-full flex items-center justify-center gap-2 py-3.5 rounded-2xl text-white text-[14px] transition-transform active:scale-[0.98]"
                style={{ fontWeight: 700, background: isLastSpot ? "linear-gradient(135deg, #16A34A, #4ADE80)" : "linear-gradient(135deg, #325BFF, #5478FF, #7C98FF)", boxShadow: isLastSpot ? "0 8px 24px rgba(22,163,74,0.35)" : "0 8px 24px rgba(84,120,255,0.35)" }}
              >
                {isLastSpot ? (<><CircleCheckBig size={17} strokeWidth={2.5} /> Complete Course</>) : (<><ChevronRight size={17} strokeWidth={2.5} /> Next: {course.spots[currentSpotIdx + 1]?.name}</>)}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════
   MAIN EXPLORE SCREEN
   ══════════════════════════════════════ */
interface ExploreScreenProps {
  selectedCategories: string[];
  onCourseNavigate?: (courseId: string) => void;
}

export function ExploreScreen({ selectedCategories }: ExploreScreenProps) {
  const [view, setView] = useState<"browse" | "detail" | "navi">("browse");
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [region, setRegion] = useState("all");
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState<string | null>(null);
  const [heroIdx, setHeroIdx] = useState(0);
  const [searchFocused, setSearchFocused] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const timer = setInterval(() => setHeroIdx(i => (i + 1) % HERO_BANNERS.length), 5000);
    return () => clearInterval(timer);
  }, []);

  const filteredCourses = COURSES.filter(c => {
    if (region !== "all" && c.region !== region) return false;
    if (categoryFilter && c.category !== categoryFilter) return false;
    if (search) {
      const q = search.toLowerCase();
      return c.title.toLowerCase().includes(q) || c.subtitle.toLowerCase().includes(q) || c.tags.some(t => t.toLowerCase().includes(q));
    }
    return true;
  });

  const handleSelectCourse = (course: Course) => {
    setSelectedCourse(course);
    setView("detail");
  };

  const handleStartNavi = () => setView("navi");

  const handleBack = () => {
    if (view === "navi") setView("detail");
    else if (view === "detail") setView("browse");
  };

  const handleComplete = () => {
    setView("browse");
    setSelectedCourse(null);
  };

  /* ── Detail / Navi views ── */
  if (view === "detail" && selectedCourse) {
    return <CourseDetailView course={selectedCourse} onBack={handleBack} onStartNavi={handleStartNavi} />;
  }
  if (view === "navi" && selectedCourse) {
    return <CourseNaviView course={selectedCourse} onBack={handleBack} onComplete={handleComplete} />;
  }

  /* ── Browse view ── */

  const categoryGroups = [
    { key: "local", label: "Local Life", sub: "Markets & food", icon: Store },
    { key: "kpop", label: "K-Pop", sub: "Idol hotspots", icon: Music },
    { key: "kdrama", label: "K-Drama", sub: "Filming spots", icon: Clapperboard },
  ];

  const featuredCourse = COURSES[0];
  const trendingCourses = COURSES.filter(c => c.id !== featuredCourse.id);

  return (
    <div className="flex-1 flex flex-col overflow-hidden animate-screenSwitch bg-[#0A0A14]">
      <div ref={scrollRef} className="flex-1 overflow-y-auto hide-scrollbar">

        {/* ═══ Hero Banner ═══ */}
        <div className="relative" style={{ height: 280 }}>
          {HERO_BANNERS.map((b, i) => (
            <div key={i} className="absolute inset-0 transition-opacity duration-1000" style={{ opacity: i === heroIdx ? 1 : 0 }}>
              <ImageWithFallback src={b.image} alt="" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
            </div>
          ))}
          <div className="absolute inset-0" style={{ background: "linear-gradient(to top, #0A0A14 0%, rgba(10,10,20,0.4) 40%, rgba(10,10,20,0.2) 70%, rgba(10,10,20,0.5) 100%)" }} />
          
          {/* Top bar */}
          <div className="absolute top-0 left-0 right-0 z-10 flex items-center justify-between px-5 pt-3">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-full overflow-hidden border-2 border-white/30 flex-shrink-0" style={{ boxShadow: "0 2px 12px rgba(0,0,0,0.3)" }}>
                <img src={mascotImg} alt="" className="w-full h-full object-cover" />
              </div>
              <div>
                <div className="text-[14px] text-white" style={{ fontWeight: 800 }}>K-Culture</div>
                <div className="text-[10px] text-white/60" style={{ fontWeight: 500 }}>Explore</div>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <button onClick={() => setSearchFocused(true)} className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: "rgba(255,255,255,0.12)", backdropFilter: "blur(8px)" }}>
                <Search size={16} color="#fff" strokeWidth={2} />
              </button>
            </div>
          </div>

          {/* Hero text */}
          <div className="absolute bottom-0 left-0 right-0 px-5 pb-5 z-10">
            <div className="flex items-center gap-2 mb-2.5">
              <span className="flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] text-white" style={{ fontWeight: 700, background: `${HERO_BANNERS[heroIdx].accent}CC` }}>
                <Flame size={10} strokeWidth={2.5} fill="white" fillOpacity={0.3} /> TRENDING
              </span>
            </div>
            <h1 className="text-[32px] text-white whitespace-pre-line" style={{ fontWeight: 900, lineHeight: 1.1, textShadow: "0 4px 20px rgba(0,0,0,0.4)" }}>
              {HERO_BANNERS[heroIdx].title}
            </h1>
            <p className="text-[13px] text-white/70 mt-2" style={{ fontWeight: 500 }}>
              {HERO_BANNERS[heroIdx].sub}
            </p>
            {/* Dots */}
            <div className="flex gap-1.5 mt-4">
              {HERO_BANNERS.map((_, i) => (
                <button key={i} onClick={() => setHeroIdx(i)} className="h-[3px] rounded-full transition-all duration-500" style={{ width: i === heroIdx ? 24 : 8, background: i === heroIdx ? "#fff" : "rgba(255,255,255,0.3)" }} />
              ))}
            </div>
          </div>
        </div>

        {/* ═══ Search overlay ═══ */}
        {searchFocused && (
          <div className="fixed inset-0 z-50 bg-white animate-fadeUp" style={{ animationDuration: "0.25s" }}>
            <div className="flex items-center gap-3 px-4 pt-4 pb-3">
              <button onClick={() => { setSearchFocused(false); setSearch(""); }} className="w-8 h-8 flex items-center justify-center">
                <ChevronLeft size={22} color="#1A1A2E" strokeWidth={2.5} />
              </button>
              <div className="flex-1 flex items-center gap-2 px-4 py-3 bg-[#F5F7FA] rounded-2xl border border-[#E8ECF4]">
                <Search size={16} color="#A0AABF" strokeWidth={2} />
                <input type="text" autoFocus placeholder="Search courses, places, K-Pop..." value={search} onChange={e => setSearch(e.target.value)} className="flex-1 bg-transparent text-[14px] text-[#1A1A2E] placeholder:text-[#A0AABF] outline-none" style={{ fontWeight: 500 }} />
                {search && <button onClick={() => setSearch("")} className="w-5 h-5 rounded-full bg-[#D6DCEB] flex items-center justify-center"><X size={10} color="#fff" strokeWidth={3} /></button>}
              </div>
            </div>
            {search && (
              <div className="px-4 pb-6 overflow-y-auto" style={{ maxHeight: "calc(100vh - 70px)" }}>
                <div className="flex flex-col gap-3 mt-2">
                  {filteredCourses.map(course => (
                    <CourseCard key={course.id} course={course} onSelect={() => { setSearchFocused(false); setSearch(""); handleSelectCourse(course); }} />
                  ))}
                  {filteredCourses.length === 0 && (
                    <div className="text-center py-16">
                      <div className="text-[14px] text-[#8892A4]" style={{ fontWeight: 600 }}>No courses found</div>
                      <div className="text-[12px] text-[#A0AABF] mt-1">Try a different keyword</div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        {/* ═══ Category Cards ═══ */}
        <div className="px-4 -mt-1" style={{ background: "#0A0A14" }}>
          <div className="flex gap-2.5 overflow-x-auto hide-scrollbar pb-4 pt-2">
            {categoryGroups.map(cg => {
              const meta = CATEGORY_META[cg.key];
              const Icon = cg.icon;
              const active = categoryFilter === cg.key;
              return (
                <button
                  key={cg.key}
                  onClick={() => setCategoryFilter(active ? null : cg.key)}
                  className="flex-shrink-0 relative overflow-hidden active:scale-[0.96] transition-transform"
                  style={{ width: 120, height: 72, borderRadius: 18, border: active ? `2px solid ${meta.color}` : "2px solid transparent" }}
                >
                  <ImageWithFallback src={CAT_HERO_IMAGES[cg.key]} alt="" style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: 16 }} />
                  <div className="absolute inset-0" style={{ background: active ? `linear-gradient(135deg, ${meta.color}DD, ${meta.color}88)` : "linear-gradient(135deg, rgba(0,0,0,0.65), rgba(0,0,0,0.35))", borderRadius: 16 }} />
                  <div className="absolute inset-0 flex flex-col items-center justify-center gap-1">
                    <div className="w-7 h-7 rounded-full flex items-center justify-center" style={{ background: active ? "rgba(255,255,255,0.3)" : "rgba(255,255,255,0.15)" }}>
                      <Icon size={14} color="#fff" strokeWidth={2.5} />
                    </div>
                    <span className="text-[11px] text-white" style={{ fontWeight: 700 }}>{cg.label}</span>
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* ═══ Content area ═══ */}
        <div className="rounded-t-[28px] -mt-1 relative z-10" style={{ background: "linear-gradient(180deg, #FAFBFF 0%, #F0F4FF 100%)", minHeight: 400 }}>
          {/* Region chips */}
          <div className="px-4 pt-5 pb-2">
            <div className="flex gap-1.5 overflow-x-auto hide-scrollbar">
              {REGIONS.map(r => (
                <button
                  key={r.id}
                  onClick={() => setRegion(r.id)}
                  className="flex-shrink-0 px-3.5 py-2 rounded-full text-[11px] transition-all active:scale-[0.95]"
                  style={{
                    fontWeight: 600,
                    background: region === r.id ? "#1A1A2E" : "#fff",
                    color: region === r.id ? "#fff" : "#6B7280",
                    boxShadow: region === r.id ? "0 4px 12px rgba(26,26,46,0.2)" : "0 1px 4px rgba(0,0,0,0.05)",
                    border: region === r.id ? "none" : "1px solid #E8ECF4",
                  }}
                >
                  {r.name}
                </button>
              ))}
            </div>
          </div>

          {/* Section: Featured */}
          {!categoryFilter && !search && (
            <div className="px-4 mt-3 mb-4">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <div className="w-6 h-6 rounded-lg flex items-center justify-center" style={{ background: "#FF6B6B22" }}>
                    <Flame size={13} color="#FF6B6B" strokeWidth={2.5} />
                  </div>
                  <span className="text-[14px] text-[#1A1A2E]" style={{ fontWeight: 800 }}>Hot This Week</span>
                </div>
              </div>
              <CourseCard course={featuredCourse} onSelect={() => handleSelectCourse(featuredCourse)} featured />
            </div>
          )}

          {/* Section: Trending / Filtered */}
          <div className="px-4 mt-2 pb-8">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded-lg flex items-center justify-center" style={{ background: categoryFilter ? CATEGORY_META[categoryFilter].bg : "#5478FF15" }}>
                  {categoryFilter ? (() => { const CI = CATEGORY_META[categoryFilter].icon; return <CI size={13} color={CATEGORY_META[categoryFilter].color} strokeWidth={2.5} />; })() : <TrendingUp size={13} color="#5478FF" strokeWidth={2.5} />}
                </div>
                <span className="text-[14px] text-[#1A1A2E]" style={{ fontWeight: 800 }}>
                  {categoryFilter ? CATEGORY_META[categoryFilter].label : "All Courses"}
                </span>
                <span className="text-[11px] text-[#8892A4] ml-1" style={{ fontWeight: 500 }}>
                  {(categoryFilter || search ? filteredCourses : trendingCourses).length} courses
                </span>
              </div>
            </div>

            {(categoryFilter || search ? filteredCourses : trendingCourses).length === 0 ? (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="w-14 h-14 rounded-2xl bg-[#F0F4FF] flex items-center justify-center mb-3">
                  <Search size={24} color="#5478FF" strokeWidth={1.5} />
                </div>
                <div className="text-[14px] text-[#8892A4]" style={{ fontWeight: 600 }}>No courses found</div>
                <div className="text-[12px] text-[#A0AABF] mt-1">Try a different region or category</div>
              </div>
            ) : (
              <div className="flex flex-col gap-4">
                {(categoryFilter || search ? filteredCourses : trendingCourses).map(course => (
                  <CourseCard key={course.id} course={course} onSelect={() => handleSelectCourse(course)} />
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
