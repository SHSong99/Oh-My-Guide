import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";
import { ImageWithFallback } from "./figma/ImageWithFallback";
import { Clock, Coins, MapPin, Map } from "lucide-react";

interface PlaceDetailScreenProps {
  placeId: string;
  onBack: () => void;
  onGo: () => void;
}

const PLACE_DATA: Record<
  string,
  {
    name: string;
    nameKr: string;
    rating: number;
    distance: string;
    tag: string;
    color: string;
    image: string;
    desc: string;
    hours: string;
    fee: string;
    walkTime: string;
  }
> = {
  dm3: {
    name: "Gwangjang Market",
    nameKr: "광장시장",
    rating: 4.8,
    distance: "1.5km",
    tag: "Food",
    color: "#FF9800",
    image:
      "https://images.unsplash.com/photo-1628532429788-c35922b5e6c1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxHd2FuZ2phbmclMjBNYXJrZXQlMjBTZW91bCUyMEtvcmVhbiUyMHN0cmVldCUyMGZvb2R8ZW58MXx8fHwxNzczMTIxMzM5fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    desc: "One of Korea's oldest and largest traditional markets, famous for its incredible street food including bindaetteok (mung bean pancakes) and mayak gimbap.",
    hours: "09:00 - 23:00",
    fee: "Free",
    walkTime: "1.5km · 22 min walk",
  },
  dm4: {
    name: "Musinsa Standard",
    nameKr: "무신사 스탠다드",
    rating: 4.6,
    distance: "1.8km",
    tag: "Shopping",
    color: "#4CAF50",
    image:
      "https://images.unsplash.com/photo-1760447572298-bd7ed4b131e1?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxrb3JlYSUyMGNsb3RoaW5nJTIwc3RvcmV8ZW58MXx8fHwxNzczMjgwMTc2fDA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    desc: "The flagship store of Korea's leading fashion platform. Offers trendy, high-quality, and affordable clothing with a sleek and modern shopping experience.",
    hours: "11:00 - 21:00",
    fee: "Free to enter",
    walkTime: "1.8km · 25 min walk",
  },
  bukchon: {
    name: "Bukchon Hanok Village",
    nameKr: "북촌한옥마을",
    rating: 4.6,
    distance: "1.2km",
    tag: "Culture",
    color: "#5478FF",
    image:
      "https://images.unsplash.com/photo-1704240699154-da9e9c690373?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxCdWtjaG9uJTIwSGFub2slMjBWaWxsYWdlJTIwU2VvdWwlMjB0cmFkaXRpb25hbCUyMGhvdXNlc3xlbnwxfHx8fDE3NzMxMjEzMzl8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    desc: "A charming neighborhood with hundreds of traditional Korean houses (hanok) dating back to the Joseon Dynasty, nestled between Gyeongbok and Changdeok palaces.",
    hours: "10:00 - 17:00",
    fee: "Free",
    walkTime: "1.2km · 15 min walk",
  },
  namsan: {
    name: "Namsan Tower",
    nameKr: "남산타워",
    rating: 4.7,
    distance: "2.1km",
    tag: "Nature",
    color: "#4CAF50",
    image:
      "https://images.unsplash.com/photo-1768006273763-85c9ff25e5fa?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxOYW1zYW4lMjBUb3dlciUyMFNlb3VsJTIwc2t5bGluZSUyMG5pZ2h0fGVufDF8fHx8MTc3MzEyMTMzOXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral",
    desc: "Iconic landmark offering panoramic views of Seoul. Take the cable car or hike through Namsan Park to reach the observation deck and famous love lock fence.",
    hours: "10:00 - 23:00",
    fee: "₩16,000",
    walkTime: "2.1km · 30 min walk",
  },
};

export function PlaceDetailScreen({ placeId, onBack, onGo }: PlaceDetailScreenProps) {
  const place = PLACE_DATA[placeId] || PLACE_DATA.dm3;

  const INFO_ICONS: Record<string, { icon: React.ElementType; bg: string; fg: string }> = {
    "Hours":    { icon: Clock,  bg: "#EDE9FE", fg: "#7C3AED" },
    "Fee":      { icon: Coins,  bg: "#DCFCE7", fg: "#16A34A" },
    "Distance": { icon: MapPin, bg: "#FFE4E6", fg: "#E11D48" },
    "Map":      { icon: Map,    bg: "#DBEAFE", fg: "#2563EB" },
  };

  const INFO_CARDS = [
    { label: "Hours", value: place.hours },
    { label: "Fee", value: place.fee },
    { label: "Distance", value: place.walkTime },
    { label: "Map", value: "Preview" },
  ];

  return (
    <div className="flex-1 flex flex-col overflow-hidden animate-screenSwitch">
      {/* Hero Image */}
      <div className="relative flex-shrink-0" style={{ height: 220 }}>
        <ImageWithFallback
          src={place.image}
          alt={place.name}
          style={{
            width: "100%",
            height: "100%",
            objectFit: "cover",
            borderRadius: "0 0 24px 24px",
          }}
        />
        {/* Overlay gradient */}
        <div
          className="absolute inset-0"
          style={{
            borderRadius: "0 0 24px 24px",
            background: "linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.5) 100%)",
          }}
        />

        {/* Header overlay */}
        <div
          className="absolute top-0 left-0 right-0 flex items-center justify-between"
          style={{ padding: "12px 16px" }}
        >
          <div
            onClick={onBack}
            className="flex items-center justify-center cursor-pointer"
            style={{
              width: 36,
              height: 36,
              borderRadius: 12,
              background: "rgba(255,255,255,0.9)",
              backdropFilter: "blur(8px)",
            }}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#1A1A2E" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M19 12H5M12 19l-7-7 7-7" />
            </svg>
          </div>
          <div
            className="flex items-center justify-center cursor-pointer"
            style={{
              width: 36,
              height: 36,
              borderRadius: 12,
              background: "rgba(255,255,255,0.9)",
              backdropFilter: "blur(8px)",
            }}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#1A1A2E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8" />
              <polyline points="16 6 12 2 8 6" />
              <line x1="12" y1="2" x2="12" y2="15" />
            </svg>
          </div>
        </div>

        {/* Place name overlay */}
        <div className="absolute bottom-0 left-0 right-0" style={{ padding: "16px 20px" }}>
          <div
            style={{
              fontFamily: "'Pretendard', sans-serif",
              fontSize: 22,
              fontWeight: 700,
              color: "#fff",
              textShadow: "0 2px 8px rgba(0,0,0,0.3)",
            }}
          >
            {place.name}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-y-auto hide-scrollbar" style={{ padding: 20 }}>
        {/* Name & Rating */}
        <div className="mb-2">
          <div className="flex items-center gap-2 flex-wrap">
            <span
              style={{
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 13,
                fontWeight: 600,
                color: "#FFBF00",
              }}
            >
              ★ {place.rating}
            </span>
            <span
              style={{
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 12,
                fontWeight: 400,
                color: "#8892A4",
              }}
            >
              {place.distance}
            </span>
            <span
              style={{
                fontFamily: "'Pretendard', sans-serif",
                fontSize: 11,
                fontWeight: 500,
                color: place.color,
                background: `${place.color}15`,
                borderRadius: 8,
                padding: "2px 10px",
              }}
            >
              {place.tag}
            </span>
          </div>
        </div>

        {/* Description */}
        <p
          style={{
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 14,
            fontWeight: 400,
            color: "#6B7280",
            lineHeight: 1.6,
            marginBottom: 16,
          }}
        >
          {place.desc}
        </p>

        {/* Info Cards Grid */}
        <div
          className="grid gap-3"
          style={{ gridTemplateColumns: "1fr 1fr" }}
        >
          {INFO_CARDS.map((card) => (
            <div
              key={card.label}
              style={{
                padding: 16,
                background: "#F5F7FA",
                borderRadius: 16,
              }}
            >
              <div className="flex items-center gap-2 mb-2">
                {(() => {
                  const cfg = INFO_ICONS[card.label];
                  const Icon = cfg.icon;
                  return (
                    <span className="w-7 h-7 rounded-[8px] flex items-center justify-center" style={{ background: cfg.bg }}>
                      <Icon size={14} color={cfg.fg} strokeWidth={2.5} fill={cfg.fg} fillOpacity={0.15} />
                    </span>
                  );
                })()}
                <span
                  style={{
                    fontFamily: "'Pretendard', sans-serif",
                    fontSize: 12,
                    fontWeight: 600,
                    color: "#8892A4",
                  }}
                >
                  {card.label}
                </span>
              </div>
              {card.label === "Map" ? (
                <div
                  className="relative"
                  style={{
                    height: 48,
                    borderRadius: 8,
                    background: "linear-gradient(135deg, #E8ECF4, #F5F7FA)",
                    overflow: "hidden",
                  }}
                >
                  {/* Mini map placeholder */}
                  <div className="absolute" style={{ top: 12, left: 16 }}>
                    <div
                      style={{
                        width: 8,
                        height: 8,
                        borderRadius: "50%",
                        background: "#5478FF",
                        boxShadow: "0 0 0 3px rgba(84,120,255,0.2)",
                      }}
                    />
                  </div>
                  <div className="absolute" style={{ top: 20, right: 20 }}>
                    <img
                      src={mascotImg}
                      alt=""
                      style={{ width: 16, height: 16, borderRadius: "50%" }}
                    />
                  </div>
                  {/* Route line */}
                  <svg
                    className="absolute inset-0"
                    width="100%"
                    height="100%"
                    viewBox="0 0 150 48"
                  >
                    <path
                      d="M20 16 Q60 40, 120 24"
                      stroke="#5478FF"
                      strokeWidth="2"
                      strokeDasharray="4 3"
                      fill="none"
                    />
                  </svg>
                </div>
              ) : (
                <div
                  style={{
                    fontFamily: "'Pretendard', sans-serif",
                    fontSize: 14,
                    fontWeight: 600,
                    color: "#1A1A2E",
                  }}
                >
                  {card.value}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Bottom Buttons */}
      <div
        className="flex gap-3 flex-shrink-0"
        style={{
          padding: "16px 20px 28px",
          background: "#fff",
          borderTop: "1px solid #F0F2F5",
        }}
      >
        <button
          onClick={onBack}
          className="cursor-pointer"
          style={{
            flex: 1,
            padding: 16,
            background: "#F5F7FA",
            borderRadius: 16,
            border: "none",
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 15,
            fontWeight: 600,
            color: "#8892A4",
            textAlign: "center",
          }}
        >
          NO
        </button>
        <button
          onClick={onGo}
          className="cursor-pointer"
          style={{
            flex: 2,
            padding: 16,
            background: "linear-gradient(135deg, #325BFF 0%, #5478FF 50%, #7C98FF 100%)",
            borderRadius: 16,
            border: "none",
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 15,
            fontWeight: 600,
            color: "#fff",
            textAlign: "center",
            boxShadow: "0 8px 24px rgba(84,120,255,0.3)",
          }}
        >
          GO
        </button>
      </div>
    </div>
  );
}