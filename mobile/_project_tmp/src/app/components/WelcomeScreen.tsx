import mascotImg from "figma:asset/5bdd44afde0a6eb361f7fb3070e075566dc1d816.png";

interface WelcomeScreenProps {
  onGetStarted: () => void;
  onSignIn: () => void;
}

export function WelcomeScreen({ onGetStarted, onSignIn }: WelcomeScreenProps) {
  return (
    <div
      className="flex-1 flex flex-col animate-screenSwitch"
      style={{
        background: "linear-gradient(180deg, #FFFFFF 0%, #F0F4FF 100%)",
      }}
    >
      {/* Content area */}
      <div className="flex-1 flex flex-col items-center justify-center px-7" style={{ paddingTop: 40 }}>
        {/* Floating mascot */}
        <div className="animate-float mb-10">
          <img
            src={mascotImg}
            alt="Oh My Guide mascot"
            style={{
              width: 180,
              height: 180,
              objectFit: "contain",
              filter: "drop-shadow(0 16px 32px rgba(84,120,255,0.25))",
            }}
          />
        </div>

        {/* Title */}
        <div className="text-center mb-6">
          <div
            className="animate-fadeUp"
            style={{
              fontFamily: "'Pretendard', sans-serif",
              fontSize: 32,
              fontWeight: 700,
              color: "#1A1A2E",
              animationDelay: "0.1s",
              lineHeight: 1.2,
            }}
          >
            Annyeong! 👋
          </div>
          <div
            className="animate-fadeUp"
            style={{
              fontFamily: "'Pretendard', sans-serif",
              fontSize: 24,
              fontWeight: 700,
              color: "#5478FF",
              animationDelay: "0.2s",
              marginTop: 4,
            }}
          >
            I'm your Korea guide
          </div>
        </div>

        {/* Description */}
        <div
          className="text-center animate-fadeUp space-y-2"
          style={{
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 16,
            fontWeight: 450,
            color: "#6B7280",
            lineHeight: 1.5,
            maxWidth: 260,
            animationDelay: "0.3s",
          }}
        >
          <p>Find amazing places,</p>
          <p>navigate like a local,</p>
          <p>and get personalized guides.</p>
        </div>
      </div>

      {/* Bottom CTA */}
      <div style={{ padding: "0 28px 60px" }}>
        <button
          onClick={onSignIn}
          className="w-full animate-fadeUp flex items-center justify-center gap-3"
          style={{
            padding: "18px 24px",
            background: "#5478FF",
            color: "#fff",
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 17,
            fontWeight: 600,
            borderRadius: 20,
            border: "none",
            cursor: "pointer",
            boxShadow: "0 12px 30px rgba(84,120,255,0.35)",
            animationDelay: "0.4s",
          }}
        >
          <div
            style={{
              width: 24,
              height: 24,
              background: "#fff",
              borderRadius: "50%",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexShrink: 0,
            }}
          >
            <svg width="14" height="14" viewBox="0 0 24 24">
              <path
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                fill="#4285F4"
              />
              <path
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                fill="#34A853"
              />
              <path
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"
                fill="#FBBC05"
              />
              <path
                d="M12 5.38c1.62 0 3.06.56 4.21 1.66l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                fill="#EA4335"
              />
            </svg>
          </div>
          Sign in with Google
        </button>
        <p
          className="text-center animate-fadeUp"
          style={{
            marginTop: 16,
            fontFamily: "'Pretendard', sans-serif",
            fontSize: 12,
            color: "#A0AABF",
            animationDelay: "0.5s",
          }}
        >
          One-tap sign in to get your personalized guide
        </p>
      </div>

    </div>
  );
}