"""
GMS (SSAFY Gen AI Management System) API 클라이언트.

GMS는 OpenAI/Gemini API의 프록시. 엔드포인트만 GMS로 변경하고
Authorization 헤더에 GMS_KEY를 사용.
"""

import json
import os
import re
from datetime import datetime, timezone, timedelta

import requests
from dotenv import load_dotenv

load_dotenv()

GMS_KEY = os.getenv("GMS_KEY", "")
GMS_BASE_URL = os.getenv("GMS_BASE_URL", "https://gms.ssafy.io/gmsapi")
OPENAI_URL = f"{GMS_BASE_URL}/api.openai.com/v1/responses"


def _call_openai(
    model: str,
    system: str,
    user: str,
    temperature: float = 0.3,
    max_tokens: int = 800,
) -> str:
    """GMS OpenAI Responses API 호출. 응답 텍스트 반환."""
    resp = requests.post(
        OPENAI_URL,
        headers={
            "Authorization": f"Bearer {GMS_KEY}",
            "Content-Type": "application/json",
        },
        json={
            "model": model,
            "instructions": system,
            "input": user,
            "temperature": temperature,
            "max_output_tokens": max_tokens,
        },
        timeout=30,
    )
    resp.raise_for_status()
    return resp.json()["output"][0]["content"][0]["text"]


def _parse_json(text: str) -> dict:
    """LLM 응답에서 JSON 파싱. 직접 파싱 실패 시 regex 폴백."""
    try:
        return json.loads(text.strip())
    except json.JSONDecodeError:
        m = re.search(r'\{.*\}', text, re.DOTALL)
        if m:
            return json.loads(m.group())
        return {}


# ── 날씨 ────────────────────────────────────────────────────────────────────

# WMO Weather interpretation codes → 한국어
_WMO_DESC = {
    0: "맑음", 1: "대체로 맑음", 2: "부분적 흐림", 3: "흐림",
    45: "안개", 48: "안개(서리)", 51: "약한 이슬비", 53: "이슬비", 55: "강한 이슬비",
    61: "약한 비", 63: "비", 65: "강한 비", 66: "약한 진눈깨비", 67: "강한 진눈깨비",
    71: "약한 눈", 73: "눈", 75: "강한 눈", 77: "싸락눈",
    80: "약한 소나기", 81: "소나기", 82: "강한 소나기",
    85: "약한 눈소나기", 86: "강한 눈소나기",
    95: "뇌우", 96: "뇌우(약한 우박)", 99: "뇌우(강한 우박)",
}


def get_weather(lat: float, lng: float) -> dict | None:
    """
    Open-Meteo API로 현재 날씨 조회. API 키 불필요.
    반환: {"temperature": 18.5, "description": "맑음", "windspeed": 5.2}
    실패 시 None.
    """
    try:
        resp = requests.get(
            "https://api.open-meteo.com/v1/forecast",
            params={"latitude": lat, "longitude": lng, "current_weather": "true"},
            timeout=5,
        )
        resp.raise_for_status()
        cw = resp.json()["current_weather"]
        return {
            "temperature": cw["temperature"],
            "description": _WMO_DESC.get(cw["weathercode"], f"코드{cw['weathercode']}"),
            "windspeed": cw["windspeed"],
        }
    except Exception:
        return None


def get_time_context() -> dict:
    """한국 시간 기준 현재 시각 + 시간대 라벨 반환."""
    kst = datetime.now(timezone(timedelta(hours=9)))
    hour = kst.hour
    if 5 <= hour < 12:
        period = "아침/오전"
    elif 12 <= hour < 14:
        period = "점심"
    elif 14 <= hour < 17:
        period = "오후"
    elif 17 <= hour < 21:
        period = "저녁"
    else:
        period = "밤/야간"
    return {"hour": hour, "period": period, "time_str": kst.strftime("%H:%M")}


# ── 리랭킹 ──────────────────────────────────────────────────────────────────

def rerank_places(
    places: list[dict],
    user_context: dict,
    top_n: int = 5,
    generate_reason: bool = True,
) -> list[dict]:
    """
    코사인 유사도 상위 places를 LLM(gpt-4o-mini)으로 리랭킹하여
    top_n개 선정.

    generate_reason=True 이면 추천 이유도 함께 생성.
    LLM 호출 실패 시 코사인 유사도 순 top_n개 반환 (reason=None).

    user_context 키:
      companion  : "couple" / "family" / "friends" / "solo"
      age        : int
      language   : "ko" / "en" / "zh" / "ja"
      refine_text: str (재추천 시 사용자 요청사항)
      weather    : dict (get_weather 반환값)
      time       : dict (get_time_context 반환값)
    """
    companion_map = {"couple": "커플", "family": "가족", "friends": "친구", "solo": "혼자"}
    lang_map = {"en": "영어권", "zh": "중국어권", "ja": "일본어권"}

    # --- 사용자 맥락 ---
    context_parts = []
    if user_context.get("companion"):
        context_parts.append(f"동행유형: {companion_map.get(user_context['companion'], user_context['companion'])}")
    if user_context.get("age"):
        context_parts.append(f"나이: {user_context['age']}세")
    if user_context.get("language") and user_context["language"] != "ko":
        context_parts.append(f"언어: {lang_map.get(user_context['language'], user_context['language'])}")
    if user_context.get("refine_text"):
        context_parts.append(f"요청사항: {user_context['refine_text']}")

    # --- 시간 맥락 ---
    time_ctx = user_context.get("time")
    if time_ctx:
        context_parts.append(f"현재시각: {time_ctx['time_str']} ({time_ctx['period']})")

    # --- 날씨 맥락 ---
    weather = user_context.get("weather")
    if weather:
        context_parts.append(
            f"날씨: {weather['description']}, {weather['temperature']}°C, "
            f"풍속 {weather['windspeed']}km/h"
        )

    context_str = " / ".join(context_parts) if context_parts else "일반 관광객"

    # --- 장소 목록 (overview 포함) ---
    place_lines = []
    for i, p in enumerate(places):
        line = (
            f"{i+1}. [ID:{p['attr_id']}] {p['title']} | {p.get('addr1', '주소미상')} | "
            f"거리 {p['distance_km']}km | 유사도 {p['similarity_score']}"
        )
        overview = p.get("overview")
        if overview:
            # overview가 길면 앞 100자만
            short = overview[:100] + "..." if len(overview) > 100 else overview
            line += f" | 설명: {short}"
        place_lines.append(line)
    places_text = "\n".join(place_lines)

    # --- 프롬프트 ---
    if generate_reason:
        json_format = '{"recommendations": [{"attr_id": 숫자, "reason": "추천 이유"}, ...]}'
        reason_instruction = (
            "reason 필드에 해당 장소를 선정한 이유를 1~2문장으로 작성하세요. "
            "사용자가 공감할 수 있도록 맥락(날씨, 시간, 동행 등)을 자연스럽게 녹여주세요.\n"
            '좋은 예: "비 오는 오후에 딱 맞는 실내 미술관이에요. 커플이 함께 감상하기 좋은 전시가 열리고 있습니다."\n'
            '나쁜 예: "유사도가 높고 실내이므로 추천합니다."'
        )
    else:
        json_format = '{"recommendations": [{"attr_id": 숫자}, ...]}'
        reason_instruction = ""

    system = (
        # 1. Persona — 역할 부여
        "당신은 한국 관광지를 10년간 안내해온 현지 여행 가이드입니다. "
        "관광객의 상황(동행, 나이, 날씨, 시간대)을 파악해 "
        "지금 이 순간 가장 만족도가 높을 장소를 골라주는 것이 당신의 역할입니다.\n\n"

        # 2. Context — 배경 설명
        "## 배경\n"
        "AI 추천 시스템이 코사인 유사도로 사전 필터링한 후보 20개를 당신에게 전달합니다. "
        f"당신은 이 중에서 사용자에게 가장 적합한 장소 {top_n}개를 최종 선정해야 합니다. "
        "유사도 점수는 참고용이며, 맥락 판단이 더 중요합니다.\n\n"

        # 3. 판단 기준 — 구체적 규칙
        "## 판단 기준 (우선순위 순)\n"
        "1. **날씨 적합성** (가장 중요)\n"
        "   - 비/눈/강풍(풍속 40km/h+) → 실내 장소 강하게 우선\n"
        "   - 폭염(30°C+) → 실내, 수변, 그늘 있는 장소 우선\n"
        "   - 혹한(0°C-) → 실내, 온천, 따뜻한 음식점 우선\n"
        "   - 맑음/흐림 → 날씨 제약 없음, 다른 기준으로 판단\n"
        "2. **시간대 적합성**\n"
        "   - 아침/오전 → 산책, 자연, 시장, 브런치\n"
        "   - 점심 → 맛집, 음식점 하나 포함 권장\n"
        "   - 오후 → 관광지, 체험, 쇼핑\n"
        "   - 저녁 → 야경, 야간 운영 장소, 맛집 우선\n"
        "   - 밤/야간 → 야경, 바, 야시장 (야간 비운영 장소는 제외)\n"
        "3. **동행 유형 적합성**\n"
        "   - 커플 → 로맨틱, 감성, 야경, 카페\n"
        "   - 가족 → 아이 체험, 넓은 공간, 편의시설\n"
        "   - 친구 → 액티비티, 맛집, 인스타 감성\n"
        "   - 혼자 → 힐링, 자연, 박물관, 카페\n"
        f"4. **다양성**: {top_n}개가 전부 같은 유형이 되지 않도록 카테고리를 섞어주세요.\n"
        "5. **거리**: 유사한 장소가 여러 개면 가까운 쪽을 우선하세요.\n\n"

        # 4. Constraints — 제약 사항
        "## 제약 사항\n"
        "- 후보 목록에 있는 ID만 사용하세요. 목록에 없는 장소를 만들어내지 마세요.\n"
        f"- 정확히 {top_n}개를 선정하세요.\n"
        "- JSON 외의 텍스트를 출력하지 마세요.\n"
        "- 추천 이유에 '유사도', '코사인', '알고리즘' 같은 시스템 용어를 사용하지 마세요.\n\n"

        # 5. Format — 출력 형식
        "## 출력 형식\n"
        f"{json_format}"
    )

    # 5. Examples — 멀티샷 (user 메시지에 예시 포함)
    example_block = ""
    if generate_reason:
        example_block = (
            "\n\n## 출력 예시\n"
            '{"recommendations": ['
            '{"attr_id": 1234, "reason": "비 오는 저녁, 따뜻한 국밥 한 그릇이 생각나는 날씨예요. 40년 전통 노포라 맛도 보장됩니다."}, '
            '{"attr_id": 5678, "reason": "식사 후 바로 옆 미술관에서 여유롭게 전시를 감상하기 좋아요. 실내라 비 걱정 없습니다."}'
            "]}\n"
            "(위는 형식 예시이며, 실제로는 후보 목록의 ID와 정보를 기반으로 작성하세요.)"
        )

    user_msg = (
        f"## 사용자 맥락\n{context_str}\n\n"
        f"## 후보 장소 ({len(places)}개, 코사인 유사도 내림차순)\n{places_text}\n\n"
        f"위 정보를 종합하여 가장 적합한 {top_n}개를 선정해주세요."
        f"{' ' + reason_instruction if reason_instruction else ''}"
        f"{example_block}"
    )

    try:
        text = _call_openai("gpt-4o-mini", system, user_msg, temperature=0.5, max_tokens=900)
        result = _parse_json(text)
        recs = result.get("recommendations", [])

        place_map = {p["attr_id"]: p for p in places}
        top_places = []
        for r in recs:
            attr_id = int(r.get("attr_id", 0))
            if attr_id in place_map:
                p = place_map[attr_id].copy()
                p["reason"] = r.get("reason") if generate_reason else None
                top_places.append(p)
            if len(top_places) >= top_n:
                break

        # LLM이 top_n 미만 반환 시 유사도 순으로 채움
        if len(top_places) < top_n:
            selected = {p["attr_id"] for p in top_places}
            for p in places:
                if p["attr_id"] not in selected:
                    cp = p.copy()
                    cp["reason"] = None
                    top_places.append(cp)
                if len(top_places) >= top_n:
                    break

        return top_places[:top_n]

    except Exception:
        # LLM 실패 시 유사도 순 top_n 반환
        return [{**p, "reason": None} for p in places[:top_n]]


# ── refine 텍스트 벡터화 ─────────────────────────────────────────────────────

def vectorize_refine_text(text: str, dim_order: list[str]) -> dict:
    """
    사용자 자유 입력 텍스트를 24차원 벡터 델타로 변환. (2 Credit/회)
    반환: {dim_name: delta} where delta ∈ [-1.0, 1.0]
    LLM 실패 시 모두 0.0인 dict 반환 (벡터 업데이트 없음).

    dim_order: DIM_ORDER 리스트 (vector_utils.DIM_ORDER)
    """
    dims_str = ", ".join(dim_order)
    system = (
        "한국 관광지 추천 시스템입니다. 사용자 입력을 분석하여 "
        "24개 취향 차원의 조정값을 JSON으로 반환합니다.\n"
        f"차원 목록: {dims_str}\n"
        "값 범위: -1.0(매우 비선호) ~ 0.0(중립) ~ 1.0(매우 선호)\n"
        "관련 없는 차원은 0.0으로 설정하세요. JSON만 응답하세요."
    )

    try:
        resp_text = _call_openai("gpt-4o-mini", system, f"사용자 입력: {text}", temperature=0.2, max_tokens=400)
        raw = _parse_json(resp_text)
        return {dim: max(-1.0, min(1.0, float(raw.get(dim, 0.0)))) for dim in dim_order}
    except Exception:
        return {dim: 0.0 for dim in dim_order}
