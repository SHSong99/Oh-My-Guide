/**
 * FloatingNavButton.tsx — Draggable floating navigation indicator
 * Shows when navigation is minimized. Tap to restore, long-press + drag to reposition.
 */
import { useState, useRef, useCallback, useEffect } from "react";
import { Navigation, X } from "lucide-react";

interface FloatingNavButtonProps {
  destinationName: string;
  progress: number;
  onRestore: () => void;
  onStop: () => void;
}

export function FloatingNavButton({
  destinationName,
  progress,
  onRestore,
  onStop,
}: FloatingNavButtonProps) {
  const [pos, setPos] = useState({ x: 307, y: 560 });
  const [isDragging, setIsDragging] = useState(false);
  const [showEntry, setShowEntry] = useState(false);

  const dragRef = useRef({
    startX: 0,
    startY: 0,
    startPosX: 0,
    startPosY: 0,
    hasMoved: false,
    longPressTimer: null as ReturnType<typeof setTimeout> | null,
    isLongPressed: false,
  });

  const containerRef = useRef<HTMLDivElement>(null);

  // Entry animation
  useEffect(() => {
    const t = setTimeout(() => setShowEntry(true), 50);
    return () => clearTimeout(t);
  }, []);

  // Snap to edge after drag
  const snapToEdge = useCallback((x: number, y: number) => {
    const parent = containerRef.current?.parentElement;
    if (!parent) return { x, y };
    const bounds = parent.getBoundingClientRect();
    const btnSize = 52;
    const clampedY = Math.max(8, Math.min(y, bounds.height - btnSize - 8));
    const midX = bounds.width / 2;
    if (x + btnSize / 2 < midX) {
      return { x: 12, y: clampedY };
    } else {
      return { x: bounds.width - btnSize - 12, y: clampedY };
    }
  }, []);

  const handlePointerDown = useCallback(
    (e: React.PointerEvent) => {
      e.preventDefault();
      (e.target as HTMLElement).setPointerCapture?.(e.pointerId);
      const ref = dragRef.current;
      ref.startX = e.clientX;
      ref.startY = e.clientY;
      ref.startPosX = pos.x;
      ref.startPosY = pos.y;
      ref.hasMoved = false;
      ref.isLongPressed = false;

      // Long press (300ms) activates drag mode
      ref.longPressTimer = setTimeout(() => {
        ref.isLongPressed = true;
        setIsDragging(true);
      }, 300);
    },
    [pos]
  );

  const handlePointerMove = useCallback((e: React.PointerEvent) => {
    const ref = dragRef.current;
    const dx = e.clientX - ref.startX;
    const dy = e.clientY - ref.startY;
    const dist = Math.sqrt(dx * dx + dy * dy);

    if (dist > 5) {
      ref.hasMoved = true;
      if (!ref.isLongPressed && ref.longPressTimer) {
        clearTimeout(ref.longPressTimer);
        ref.longPressTimer = null;
      }
    }

    if (ref.isLongPressed) {
      setPos({
        x: ref.startPosX + dx,
        y: ref.startPosY + dy,
      });
    }
  }, []);

  const handlePointerUp = useCallback(
    (e: React.PointerEvent) => {
      const ref = dragRef.current;
      if (ref.longPressTimer) {
        clearTimeout(ref.longPressTimer);
        ref.longPressTimer = null;
      }

      if (ref.isLongPressed) {
        // Was dragging → snap to nearest edge
        setIsDragging(false);
        const snapped = snapToEdge(pos.x, pos.y);
        setPos(snapped);
      } else if (!ref.hasMoved) {
        // Simple tap → restore navigation
        onRestore();
      }

      ref.isLongPressed = false;
      ref.hasMoved = false;
    },
    [pos, snapToEdge, onRestore]
  );

  const isArrived = progress >= 100;
  const circumference = 2 * Math.PI * 23;

  return (
    <div
      ref={containerRef}
      className="absolute z-50 select-none touch-none"
      style={{
        left: pos.x,
        top: pos.y,
        transition: isDragging
          ? "none"
          : "left 0.35s cubic-bezier(0.32,0.72,0,1), top 0.35s cubic-bezier(0.32,0.72,0,1), opacity 0.3s, transform 0.4s cubic-bezier(0.34,1.4,0.64,1)",
        opacity: showEntry ? 1 : 0,
        transform: showEntry
          ? isDragging
            ? "scale(1.15)"
            : "scale(1)"
          : "scale(0.3)",
      }}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
    >
      {/* Pulse ring (background) */}
      {!isDragging && !isArrived && (
        <div
          className="absolute rounded-full pointer-events-none"
          style={{
            inset: -4,
            background: "rgba(84,120,255,0.12)",
            animation: "navFabPulse 2.5s ease-out infinite",
          }}
        />
      )}

      {/* Main FAB body */}
      <div
        className="relative w-[52px] h-[52px] rounded-full flex items-center justify-center cursor-pointer"
        style={{
          background: isArrived
            ? "linear-gradient(135deg, #16A34A, #22C55E)"
            : "linear-gradient(135deg, #3D5AF1, #5478FF)",
          boxShadow: isDragging
            ? "0 14px 44px rgba(84,120,255,0.55), 0 0 0 4px rgba(84,120,255,0.15)"
            : isArrived
            ? "0 6px 24px rgba(22,163,74,0.45)"
            : "0 6px 24px rgba(84,120,255,0.45)",
          transition: "box-shadow 0.3s, transform 0.2s",
        }}
      >
        {/* Navigation icon */}
        <Navigation
          size={20}
          color="white"
          strokeWidth={2.5}
          fill="white"
          fillOpacity={0.3}
          style={{
            transform: "rotate(45deg)",
            filter: "drop-shadow(0 1px 2px rgba(0,0,0,0.15))",
          }}
        />

        {/* Progress ring overlay */}
        <svg
          className="absolute inset-0 pointer-events-none"
          width="52"
          height="52"
          viewBox="0 0 52 52"
          style={{ transform: "rotate(-90deg)" }}
        >
          <circle
            cx="26"
            cy="26"
            r="23"
            fill="none"
            stroke="rgba(255,255,255,0.2)"
            strokeWidth="3"
          />
          <circle
            cx="26"
            cy="26"
            r="23"
            fill="none"
            stroke="white"
            strokeWidth="3"
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={circumference * (1 - progress / 100)}
            style={{ transition: "stroke-dashoffset 1s ease-out" }}
          />
        </svg>
      </div>


    </div>
  );
}