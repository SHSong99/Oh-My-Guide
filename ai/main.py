from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional

import numpy as np

from database import get_user_vector, get_places_in_radius, save_user_vector
from vector_utils import (
    rank_places, build_cold_start_vector, DIM_ORDER,
    apply_vector_delta, apply_vector_choices,
)
from gms_client import rerank_places, vectorize_refine_text, get_weather, get_time_context

app = FastAPI(title="Oh! My Guide AI Server")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 추천 이유 생성 여부 (리랭킹은 항상 실행, reason 텍스트만 이 플래그로 제어)
REASON_ENABLED = False


# ── 스키마 ────────────────────────────────────────────────────────────────────

class UserProfile(BaseModel):
    """백엔드에서 전달하는 사용자 메타데이터 (cold-start 벡터 생성에 사용)."""
    companion: Optional[str] = Field(default=None, description="동행 유형: couple / family / friends / solo")
    age: Optional[int]       = Field(default=None, description="나이")
    gender: Optional[str]    = Field(default=None, description="성별: M / F")
    language: Optional[str]  = Field(default=None, description="언어: ko / en / zh / ja")
    country: Optional[str]   = Field(default=None, description="국가 코드 (예: KR, US)")


class RecommendRequest(BaseModel):
    user_id: int
    latitude: float
    longitude: float
    content_type_ids: list[int] = Field(description="카테고리 필터 (필수, 예: [12, 14])")
    radius_km: float             = Field(default=10.0, ge=0.1, le=50.0)
    user_profile: Optional[UserProfile] = Field(default=None, description="cold-start용 사용자 메타데이터")


class RefineRequest(BaseModel):
    user_id: int
    latitude: float
    longitude: float
    content_type_ids: list[int]  = Field(description="카테고리 필터 (필수)")
    radius_km: float              = Field(default=10.0, ge=0.1, le=50.0)
    excluded_attr_ids: list[int]  = Field(default=[], description="이미 추천된 장소 attr_id 목록 (중복 제외)")
    refine_text: Optional[str]    = Field(default=None, description="자유 입력 텍스트 (학습률 0.25)")
    refine_choices: list[str]     = Field(default=[], description="선택지 차원명 목록 (학습률 0.20)")
    user_profile: Optional[UserProfile] = Field(default=None, description="사용자 맥락 (리랭킹에 활용)")


class PlaceResult(BaseModel):
    attr_id: int
    content_id: Optional[int]
    title: Optional[str]
    addr1: Optional[str]
    latitude: float
    longitude: float
    content_type_id: Optional[int]
    first_image1: Optional[str]
    distance_km: float
    similarity_score: float
    reason: Optional[str] = None


class RecommendResponse(BaseModel):
    recommendations: list[PlaceResult]
    cold_start: bool = Field(description="True = 사용자 벡터 없어서 cold-start 처리됨")


# ── 헬퍼 ─────────────────────────────────────────────────────────────────────

def _build_user_context(
    user_profile: Optional[UserProfile],
    lat: float,
    lng: float,
    refine_text: Optional[str] = None,
) -> dict:
    """리랭킹에 전달할 사용자 맥락 dict 생성 (날씨 + 시간 포함)."""
    ctx = {}
    if user_profile:
        if user_profile.companion:
            ctx["companion"] = user_profile.companion
        if user_profile.age:
            ctx["age"] = user_profile.age
        if user_profile.language:
            ctx["language"] = user_profile.language
    if refine_text:
        ctx["refine_text"] = refine_text
    ctx["time"] = get_time_context()
    weather = get_weather(lat, lng)
    if weather:
        ctx["weather"] = weather
    return ctx


# ── 엔드포인트 ────────────────────────────────────────────────────────────────

@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/api/ai/recommend", response_model=RecommendResponse)
def recommend(req: RecommendRequest):
    # 1. 사용자 벡터 조회
    user_vec, cold_start = get_user_vector(req.user_id)

    # 2. 반경 내 장소 + 벡터 조회
    places = get_places_in_radius(
        lat=req.latitude,
        lng=req.longitude,
        radius_km=req.radius_km,
        content_type_ids=req.content_type_ids,
    )

    if not places:
        return RecommendResponse(recommendations=[], cold_start=cold_start)

    # 3. cold-start 처리: 초기 벡터 생성 후 DB 저장
    if cold_start:
        p = req.user_profile
        if p:
            user_vec = build_cold_start_vector(
                companion=p.companion,
                age=p.age,
                gender=p.gender,
                language=p.language,
                country=p.country,
                content_type_ids=req.content_type_ids,
            )
        else:
            user_vec = np.zeros(len(DIM_ORDER), dtype=np.float32)

        try:
            save_user_vector(req.user_id, user_vec)
        except Exception:
            pass

    # 4. 코사인 유사도 → 상위 20개
    top20 = rank_places(places, user_vec, top_n=20)

    # 5. LLM 리랭킹 → 상위 5개 (항상 실행, reason은 REASON_ENABLED로 제어)
    user_context = _build_user_context(req.user_profile, req.latitude, req.longitude)
    top5 = rerank_places(top20, user_context, top_n=5, generate_reason=REASON_ENABLED)

    return RecommendResponse(
        recommendations=[PlaceResult(**p) for p in top5],
        cold_start=cold_start,
    )


@app.post("/api/ai/refine", response_model=RecommendResponse)
def refine(req: RefineRequest):
    # 1. 사용자 벡터 조회
    user_vec, cold_start = get_user_vector(req.user_id)

    if user_vec is None:
        user_vec = np.zeros(len(DIM_ORDER), dtype=np.float32)

    # 2. refine_text → LLM 벡터화 → 사용자 벡터 업데이트 (학습률 0.25)
    if req.refine_text:
        delta = vectorize_refine_text(req.refine_text, DIM_ORDER)
        user_vec = apply_vector_delta(user_vec, delta, lr=0.25)

    # 3. refine_choices → 해당 차원 직접 상향 (학습률 0.20)
    if req.refine_choices:
        user_vec = apply_vector_choices(user_vec, req.refine_choices, lr=0.20)

    # 4. 업데이트된 벡터 DB 저장
    try:
        save_user_vector(req.user_id, user_vec)
    except Exception:
        pass

    # 5. 이미 본 장소 제외한 반경 내 장소 조회
    places = get_places_in_radius(
        lat=req.latitude,
        lng=req.longitude,
        radius_km=req.radius_km,
        content_type_ids=req.content_type_ids,
        excluded_attr_ids=req.excluded_attr_ids or None,
    )

    if not places:
        return RecommendResponse(recommendations=[], cold_start=False)

    # 6. 코사인 유사도 → 상위 20개
    top20 = rank_places(places, user_vec, top_n=20)

    # 7. LLM 리랭킹 → 상위 5개 (항상 실행, reason은 REASON_ENABLED로 제어)
    user_context = _build_user_context(req.user_profile, req.latitude, req.longitude, req.refine_text)
    top5 = rerank_places(top20, user_context, top_n=5, generate_reason=REASON_ENABLED)

    return RecommendResponse(
        recommendations=[PlaceResult(**p) for p in top5],
        cold_start=False,
    )
