from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional

import numpy as np

from database import get_user_vector, get_places_in_radius, save_user_vector, get_segment_vectors
from vector_utils import (
    rank_places, build_cold_start_vector, DIM_ORDER,
    apply_vector_delta, apply_vector_choices, apply_category_filter,
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
    radius_km: float             = Field(default=5.0, ge=0.1, le=50.0)
    user_profile: Optional[UserProfile] = Field(default=None, description="cold-start용 사용자 메타데이터")


class RefineRequest(BaseModel):
    user_id: int
    latitude: float
    longitude: float
    content_type_ids: list[int]  = Field(description="카테고리 필터 (필수)")
    radius_km: float              = Field(default=5.0, ge=0.1, le=50.0)
    excluded_attr_ids: list[int]  = Field(default=[], description="이미 추천된 장소 attr_id 목록 (중복 제외)")
    refine_text: Optional[str]    = Field(default=None, description="자유 입력 텍스트 (학습률 0.25)")
    refine_choices: list[str]     = Field(default=[], description="선택지 차원명 목록 (학습률 0.20)")
    user_profile: Optional[UserProfile] = Field(default=None, description="사용자 맥락 (리랭킹에 활용)")


class RefreshRequest(BaseModel):
    """재추천 요청 (GO 없이 재추천 버튼 클릭 시)."""
    user_id: int
    latitude: float
    longitude: float
    radius_km: float              = Field(default=5.0, ge=0.1, le=50.0)
    category: Optional[str]       = Field(default=None, description="카테고리 자연어 입력 (예: 'nature', '맛집')")
    mood: Optional[str]           = Field(default=None, description="분위기 자연어 입력 (예: 'calm and healing')")
    free_text: Optional[str]      = Field(default=None, description="기타 자유 텍스트")
    excluded_attr_ids: list[int]  = Field(default=[], description="이미 추천된 장소 attr_id 목록")


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


# content_type_id → 모바일 PlaceCard용 태그
_CONTENT_TYPE_TAG = {
    12: "Nature", 14: "Culture", 15: "Festival",
    28: "Activity", 32: "Lodging", 38: "Shopping", 39: "Food",
}


class PlaceCardResult(BaseModel):
    """모바일 PlaceCard에 필요한 필드만 담은 응답."""
    attr_id: int
    name: str                     = Field(description="장소명 (영어 우선, 없으면 한국어)")
    name_kr: str                  = Field(description="한국어 장소명")
    image_url: Optional[str]      = Field(default=None, description="대표 이미지 URL")
    distance: str                 = Field(description="거리 문자열 (예: '1.2km', '350m')")
    tag: str                      = Field(description="카테고리 태그 (예: 'Food', 'Nature')")
    latitude: float
    longitude: float


class RefreshResponse(BaseModel):
    recommendations: list[PlaceCardResult]


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


@app.post("/ai/recommend", response_model=RecommendResponse)
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
            seg_nationality = _COUNTRY_TO_NATIONALITY.get((p.country or "").upper(), "Other")
            seg_age = _to_segment_age(p.age)
            seg_gender = _to_segment_gender(p.gender)
            seg_vecs = get_segment_vectors(seg_nationality, seg_age, seg_gender)

            user_vec = build_cold_start_vector(
                companion=p.companion,
                age=p.age,
                gender=p.gender,
                language=p.language,
                country=p.country,
                content_type_ids=req.content_type_ids,
                segment_vectors=seg_vecs if seg_vecs else None,
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


@app.post("/ai/refine", response_model=RecommendResponse)
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


# ── 거리 포맷 헬퍼 ────────────────────────────────────────────────────────────

def _format_distance(km: float) -> str:
    """km → 사람이 읽기 좋은 거리 문자열."""
    if km < 1.0:
        return f"{int(km * 1000)}m"
    return f"{km:.1f}km"


# ── 모바일 카테고리 id → content_type_ids 매핑 ────────────────────────────────

_MOBILE_CATEGORY_MAP = {
    "attraction": [12],
    "culture": [14],
    "festival": [15],
    "course": [12, 14],
    "leports": [28],
    "cafe": [39],
    "shopping": [38],
    "food": [39],
}


_COUNTRY_TO_NATIONALITY = {
    "JP": "Japan", "CN": "China", "TW": "Taiwan", "HK": "HongKong",
    "TH": "Thailand", "SG": "Singapore", "MY": "Malaysia", "PH": "Philippines",
    "VN": "Vietnam", "ID": "Indonesia", "IN": "India",
    "US": "USA", "CA": "Canada", "GB": "UK", "AU": "Australia",
    "FR": "France", "DE": "Germany", "RU": "Russia",
}


def _to_segment_age(age: int | None) -> str | None:
    if age is None:
        return None
    if age < 20:
        return "10s"
    if age < 30:
        return "20s"
    if age < 40:
        return "30s"
    if age < 50:
        return "40s"
    if age < 60:
        return "50s"
    return "60s"


def _to_segment_gender(gender: str | None) -> str | None:
    if not gender:
        return None
    g = gender.strip().upper()
    if g in ("M", "MALE"):
        return "male"
    if g in ("F", "FEMALE"):
        return "female"
    return None


def _parse_mobile_categories(category_str: str | None) -> list[int]:
    """쉼표 구분 모바일 카테고리 → content_type_ids. 없으면 전체."""
    if not category_str:
        return [12, 14, 15, 28, 32, 38, 39]
    ids = set()
    for cat in category_str.split(","):
        cat = cat.strip().lower()
        if cat in _MOBILE_CATEGORY_MAP:
            ids.update(_MOBILE_CATEGORY_MAP[cat])
    return list(ids) if ids else [12, 14, 15, 28, 32, 38, 39]


@app.get("/userRecommend", response_model=RefreshResponse)
def get_user_recommend(
    category: str | None = None,
    currentLat: float = 37.5665,
    currentLng: float = 126.978,
    userId: int = 1,
    radiusKm: float = 5.0,
    excludedAttrIds: str | None = None,
    age: int | None = None,
    gender: str | None = None,
    companion: str | None = None,
    country: str | None = None,
):
    """초기 추천: 카테고리 선택 후 첫 추천 장소 5개."""
    # 0. excluded_attr_ids 파싱
    excluded = []
    if excludedAttrIds:
        excluded = [int(x) for x in excludedAttrIds.split(",") if x.strip().isdigit()]

    # 1. 사용자 벡터 조회
    user_vec, cold_start = get_user_vector(userId)
    if user_vec is None:
        user_vec = np.zeros(len(DIM_ORDER), dtype=np.float32)

    # 2. 카테고리 매핑
    content_type_ids = _parse_mobile_categories(category)

    # 3. cold-start 처리 (사용자 프로필 반영)
    if cold_start:
        seg_nationality = _COUNTRY_TO_NATIONALITY.get((country or "").upper(), "Other")
        seg_age = _to_segment_age(age)
        seg_gender = _to_segment_gender(gender)
        seg_vecs = get_segment_vectors(seg_nationality, seg_age, seg_gender)

        user_vec = build_cold_start_vector(
            companion=companion,
            age=age,
            gender=gender,
            country=country,
            content_type_ids=content_type_ids,
            segment_vectors=seg_vecs if seg_vecs else None,
        )
        try:
            save_user_vector(userId, user_vec)
        except Exception:
            pass

    # 3.5. 선택한 카테고리 차원 강제 (선택=1.0, 미선택=0.0, 분위기/실용은 유지)
    mobile_cats = [c.strip().lower() for c in category.split(",")] if category else None
    user_vec = apply_category_filter(user_vec, mobile_cats)

    # 4. 반경 내 장소 조회 (방문한 장소 제외)
    places = get_places_in_radius(
        lat=currentLat,
        lng=currentLng,
        radius_km=radiusKm,
        content_type_ids=content_type_ids,
        excluded_attr_ids=excluded or None,
    )

    if not places:
        return RefreshResponse(recommendations=[])

    # 5. 코사인 유사도 → 상위 20개
    top20 = rank_places(places, user_vec, top_n=20)

    # 6. LLM 리랭킹 → 상위 5개 (사용자 프로필 맥락 전달)
    user_context = {"time": get_time_context()}
    if companion:
        user_context["companion"] = companion
    if age:
        user_context["age"] = age
    if country and country not in ("KR", "kr"):
        user_context["language"] = country
    weather = get_weather(currentLat, currentLng)
    if weather:
        user_context["weather"] = weather
    top5 = rerank_places(top20, user_context, top_n=5, generate_reason=False)

    # 7. PlaceCard 형식으로 변환
    results = []
    for p in top5:
        results.append(PlaceCardResult(
            attr_id=p["attr_id"],
            name=p.get("title") or "",
            name_kr=p.get("title") or "",
            image_url=p.get("first_image1") or None,
            distance=_format_distance(p["distance_km"]),
            tag=_CONTENT_TYPE_TAG.get(p.get("content_type_id"), "Place"),
            latitude=p["latitude"],
            longitude=p["longitude"],
        ))

    return RefreshResponse(recommendations=results)


# ── 자연어 → content_type_ids 매핑 ────────────────────────────────────────────

_CATEGORY_KEYWORDS = {
    12: ["nature", "자연", "산", "바다", "공원", "경치", "관광"],
    14: ["culture", "문화", "박물관", "미술관", "전시", "공연"],
    15: ["festival", "축제", "행사", "이벤트"],
    28: ["activity", "액티비티", "레포츠", "스포츠", "체험"],
    32: ["lodging", "숙박", "호텔", "게스트하우스", "펜션"],
    38: ["shopping", "쇼핑", "시장", "마트", "몰"],
    39: ["food", "음식", "맛집", "카페", "cafe", "restaurant", "먹거리"],
}


def _parse_category_text(text: str | None) -> list[int]:
    """자연어 카테고리 → content_type_ids 목록. 매칭 없으면 전체 카테고리."""
    if not text:
        return list(_CATEGORY_KEYWORDS.keys())
    lower = text.lower()
    matched = [ct_id for ct_id, keywords in _CATEGORY_KEYWORDS.items()
               if any(kw in lower for kw in keywords)]
    return matched if matched else list(_CATEGORY_KEYWORDS.keys())


@app.post("/userRecommend/recommend/refresh", response_model=RefreshResponse)
def refresh_recommend(req: RefreshRequest):
    """GO 없이 재추천: 카테고리/분위기/텍스트를 자연어로 받아 5개 추천."""
    # 1. 사용자 벡터 조회
    user_vec, _ = get_user_vector(req.user_id)
    if user_vec is None:
        user_vec = np.zeros(len(DIM_ORDER), dtype=np.float32)

    # 2. 자연어 입력을 하나로 합쳐서 LLM 벡터화 → 사용자 벡터 업데이트
    text_parts = [t for t in [req.category, req.mood, req.free_text] if t]
    combined_text = " / ".join(text_parts) if text_parts else None

    if combined_text:
        delta = vectorize_refine_text(combined_text, DIM_ORDER)
        user_vec = apply_vector_delta(user_vec, delta, lr=0.25)
        try:
            save_user_vector(req.user_id, user_vec)
        except Exception:
            pass

    # 3. 카테고리 자연어 → content_type_ids 매핑
    content_type_ids = _parse_category_text(req.category)

    # 4. 반경 내 장소 조회 (이미 본 장소 제외)
    places = get_places_in_radius(
        lat=req.latitude,
        lng=req.longitude,
        radius_km=req.radius_km,
        content_type_ids=content_type_ids,
        excluded_attr_ids=req.excluded_attr_ids or None,
    )

    if not places:
        return RefreshResponse(recommendations=[])

    # 5. 코사인 유사도 → 상위 20개
    top20 = rank_places(places, user_vec, top_n=20)

    # 6. LLM 리랭킹 → 상위 5개
    user_context = {"time": get_time_context()}
    if combined_text:
        user_context["refine_text"] = combined_text
    weather = get_weather(req.latitude, req.longitude)
    if weather:
        user_context["weather"] = weather
    top5 = rerank_places(top20, user_context, top_n=5, generate_reason=False)

    # 7. PlaceCard 형식으로 변환
    results = []
    for p in top5:
        results.append(PlaceCardResult(
            attr_id=p["attr_id"],
            name=p.get("title") or "",
            name_kr=p.get("title") or "",
            image_url=p.get("first_image1") or None,
            distance=_format_distance(p["distance_km"]),
            tag=_CONTENT_TYPE_TAG.get(p.get("content_type_id"), "Place"),
            latitude=p["latitude"],
            longitude=p["longitude"],
        ))

    return RefreshResponse(recommendations=results)
