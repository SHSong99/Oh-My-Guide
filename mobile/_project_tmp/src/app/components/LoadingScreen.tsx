import { useEffect } from "react";
import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";

interface LoadingScreenProps {
  onFinish: () => void;
}

export function LoadingScreen({ onFinish }: LoadingScreenProps) {
  useEffect(() => {
    const timer = setTimeout(onFinish, 3000);
    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <div
      className="flex-1 flex flex-col items-center justify-center animate-screenSwitch"
      style={{ background: "#FFFFFF" }}
    >
      {/* Bouncing mascot */}
      <div className="animate-bounce mb-5">
        <img
          src={mascotImg}
          alt="Loading"
          style={{
            width: 120,
            height: 120,
            objectFit: "contain",
            filter: "drop-shadow(0 12px 24px rgba(84,120,255,0.2))",
          }}
        />
      </div>

      {/* Text */}
      <div
        style={{
          fontFamily: "'Pretendard', sans-serif",
          fontSize: 18,
          fontWeight: 600,
          color: "#1A1A2E",
          marginBottom: 8,
        }}
      >
        Finding amazing spots...
      </div>
      <div
        className="text-center"
        style={{
          fontFamily: "'Pretendard', sans-serif",
          fontSize: 18,
          fontWeight: 600,
          color: "#8892A4",
          maxWidth: 240,
          marginBottom: 20,
        }}
      >
        Scanning nearby places based on your interests
      </div>

      {/* Progress bar */}
      <div
        style={{
          width: 200,
          height: 4,
          borderRadius: 2,
          background: "#E8ECF4",
          overflow: "hidden",
        }}
      >
        <div
          style={{
            height: "100%",
            borderRadius: 2,
            background: "linear-gradient(135deg, #5478FF 0%, #FFDE42 100%)",
            animation: "progressFill 2.8s ease-out forwards",
          }}
        />
      </div>
    </div>
  );
}