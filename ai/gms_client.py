"""
GMS (SSAFY Gen AI Management System) API 클라이언트.

GMS는 OpenAI/Gemini API의 프록시. 엔드포인트만 GMS로 변경하고
Authorization 헤더에 GMS_KEY를 사용.
"""

import json
import os
import re

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


# ── 리랭킹 ──────────────────────────────────────────────────────────────────

def rerank_places(
    places: list[dict],
    user_context: dict,
    top_n: int = 5,
) -> list[dict]:
    """
    코사인 유사도 상위 places를 LLM(gpt-5-mini)으로 리랭킹하여
    top_n개 선정 + 추천 이유 생성. (5 Credit/회)

    LLM 호출 실패 시 코사인 유사도 순 top_n개 반환 (reason=None).

    user_context 키:
      companion : "couple" / "family" / "friends" / "solo"
      age       : int
      language  : "ko" / "en" / "zh" / "ja"
      refine_text: str (재추천 시 사용자 요청사항)
    """
    companion_map = {"couple": "커플", "family": "가족", "friends": "친구", "solo": "혼자"}
    lang_map = {"en": "영어권", "zh": "중국어권", "ja": "일본어권"}

    context_parts = []
    if user_context.get("companion"):
        context_parts.append(f"동행유형: {companion_map.get(user_context['companion'], user_context['companion'])}")
    if user_context.get("age"):
        context_parts.append(f"나이: {user_context['age']}세")
    if user_context.get("language") and user_context["language"] != "ko":
        context_parts.append(f"언어: {lang_map.get(user_context['language'], user_context['language'])}")
    if user_context.get("refine_text"):
        context_parts.append(f"요청사항: {user_context['refine_text']}")
    context_str = " / ".join(context_parts) if context_parts else "일반 관광객"

    places_text = "\n".join(
        f"{i+1}. [ID:{p['attr_id']}] {p['title']} | {p.get('addr1', '주소미상')} | "
        f"거리 {p['distance_km']}km | 유사도 {p['similarity_score']}"
        for i, p in enumerate(places)
    )

    system = (
        "한국 관광지 추천 전문가입니다. 사용자 맥락을 고려해 가장 적합한 장소를 선정하고 "
        "각각 1~2문장의 추천 이유를 한국어로 작성합니다.\n"
        "반드시 아래 JSON 형식으로만 응답하세요:\n"
        '{"recommendations": [{"attr_id": 숫자, "reason": "추천 이유"}, ...]}'
    )
    user_msg = (
        f"사용자 맥락: {context_str}\n\n"
        f"후보 장소 (코사인 유사도 내림차순):\n{places_text}\n\n"
        f"이 사용자에게 가장 적합한 {top_n}개를 선정하고 각 추천 이유를 작성하세요. "
        "반드시 위 목록에 있는 ID만 사용하세요."
    )

    try:
        text = _call_openai("gpt-5-mini", system, user_msg, temperature=0.5, max_tokens=900)
        result = _parse_json(text)
        recs = result.get("recommendations", [])

        place_map = {p["attr_id"]: p for p in places}
        top_places = []
        for r in recs:
            attr_id = int(r.get("attr_id", 0))
            if attr_id in place_map:
                p = place_map[attr_id].copy()
                p["reason"] = r.get("reason")
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
