import { useEffect } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";

interface SplashScreenProps {
  onFinish: () => void;
}

export function SplashScreen({ onFinish }: SplashScreenProps) {
  useEffect(() => {
    const timer = setTimeout(onFinish, 2500);
    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <div
      className="flex-1 flex flex-col items-center justify-center relative"
      style={{
        background: "linear-gradient(180deg, #FFFFFF 0%, #F0F4FF 100%)",
      }}
    >
      {/* Mascot with bounce-in */}
      <div className="animate-bounceIn mb-6">
        <img
          src={mascotImg}
          alt="Oh My Guide mascot"
          style={{
            width: 140,
            height: 140,
            objectFit: "contain",
            filter: "drop-shadow(0 12px 24px rgba(84,120,255,0.2))",
          }}
        />
      </div>

      {/* Logo */}
      <div
        className="animate-fadeUp"
        style={{
          fontFamily: "'Pretendard', sans-serif",
          fontSize: 28,
          fontWeight: 700,
          background: "linear-gradient(135deg, #325BFF 0%, #5478FF 50%, #7C98FF 100%)",
          WebkitBackgroundClip: "text",
          WebkitTextFillColor: "transparent",
          animationDelay: "0.3s",
        }}
      >
        Oh My Guide!
      </div>

      {/* Subtitle */}
      <p
        className="animate-fadeUp"
        style={{
          fontFamily: "'Pretendard', sans-serif",
          fontSize: 13,
          fontWeight: 400,
          color: "#8892A4",
          marginTop: 8,
          animationDelay: "0.5s",
        }}
      >
        Your personal Korea travel buddy
      </p>

      {/* Loading Spinner */}
      <div
        className="animate-fadeUp"
        style={{ marginTop: 32, animationDelay: "0.7s" }}
      >
        <div
          className="animate-spin"
          style={{
            width: 32,
            height: 32,
            borderRadius: "50%",
            border: "3px solid #E8ECF4",
            borderTopColor: "#5478FF",
          }}
        />
      </div>
    </div>
  );
}