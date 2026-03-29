"""
벡터 파싱, 코사인 유사도 계산, cold-start 임시 벡터 생성.
"""

import numpy as np

# 24차원 순서 (DB JSON 키 기준, 양쪽 벡터가 이 순서로 정렬됨)
DIM_ORDER = [
    # 카테고리 8
    "nature", "culture", "festival", "activity", "shopping", "food", "cafe", "lodging",
    # 분위기 10
    "healing", "aesthetic", "gourmet", "learning", "heritage", "mood_nature",
    "romantic", "family", "active", "nightlife",
    # 실용 6
    "free_entry", "parking_available", "pet_friendly", "baby_friendly", "indoor", "outdoor",
]


def parse_vector(json_dict: dict) -> np.ndarray:
    """DB JSON → 24차원 numpy 배열."""
    return np.array([float(json_dict.get(dim, 0.0)) for dim in DIM_ORDER], dtype=np.float32)


def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    """두 벡터의 코사인 유사도. 한쪽이 영벡터면 0.0 반환."""
    norm_a = np.linalg.norm(a)
    norm_b = np.linalg.norm(b)
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return float(np.dot(a, b) / (norm_a * norm_b))


# ── cold-start 임시 벡터 생성 ─────────────────────────────────────────────────

# content_type_id → 카테고리 차원명
_CATEGORY_DIM = {
    12: "nature", 14: "culture", 15: "festival",
    28: "activity", 38: "shopping", 39: "food", 32: "lodging",
}

# 카테고리 차원 목록 (8개)
CATEGORY_DIMS = ["nature", "culture", "festival", "activity", "shopping", "food", "cafe", "lodging"]

# 모바일 카테고리 이름 → 벡터 차원명
_MOBILE_CAT_TO_DIM = {
    "attraction": ["nature"],
    "culture": ["culture"],
    "festival": ["festival"],
    "course": ["nature", "culture"],
    "leports": ["activity"],
    "cafe": ["cafe"],
    "shopping": ["shopping"],
    "food": ["food"],
}


def apply_category_filter(user_vec: 'np.ndarray', mobile_categories: list[str] | None) -> 'np.ndarray':
    """
    선택한 카테고리 차원은 1.0, 선택 안 한 카테고리 차원은 0.0으로 강제.
    분위기/실용 차원은 건드리지 않음.
    mobile_categories: ["cafe", "food", ...] 모바일에서 보낸 카테고리 이름 목록
    """
    if not mobile_categories:
        return user_vec

    result = user_vec.copy()
    dim_index = {dim: i for i, dim in enumerate(DIM_ORDER)}

    # 선택된 카테고리 차원 수집
    selected_dims = set()
    for cat in mobile_categories:
        dims = _MOBILE_CAT_TO_DIM.get(cat.strip().lower(), [])
        selected_dims.update(dims)

    # 카테고리 8개 차원만 조작
    for dim in CATEGORY_DIMS:
        idx = dim_index[dim]
        if dim in selected_dims:
            result[idx] = 1.0
        else:
            result[idx] = 0.0

    return result

# 동행 유형 → 분위기 차원 부스트 (CLAUDE.md 설계 기반, 가장 강한 신호)
_COMPANION_BOOST = {
    "couple":  {"romantic": 0.7, "aesthetic": 0.5, "nightlife": 0.4, "gourmet": 0.5},
    "family":  {"family": 0.8, "healing": 0.4, "learning": 0.4, "mood_nature": 0.4},
    "friends": {"active": 0.5, "nightlife": 0.5, "gourmet": 0.5, "aesthetic": 0.4},
    "solo":    {"healing": 0.6, "mood_nature": 0.5, "learning": 0.5, "heritage": 0.5},
}

# 나이대 → 분위기 부스트 (약한 신호 × 0.6)
# ⚠️  임시 규칙 기반 값 — 배치 파이프라인 완성 후 DB 기반으로 교체 예정
#
# 교체 계획:
#   Spark 배치(매일 새벽 4시)가 연령대×성별×동행유형 세그먼트별 Go/Skip 통계를 집계하고,
#   세그먼트에서 Go 상위 5개 장소의 벡터 평균을 segment_vectors 테이블에 저장.
#   이후 build_cold_start_vector()에서 _AGE_BOOST 대신
#   DB의 segment_vectors를 조회하도록 수정한다.
_AGE_BOOST = [
    ((10, 29), {"active": 0.4, "aesthetic": 0.4, "nightlife": 0.4}),
    ((30, 49), {"gourmet": 0.3, "healing": 0.3, "family": 0.3}),
    ((50, 99), {"heritage": 0.4, "learning": 0.4, "healing": 0.3}),
]

# 언어 → 분위기 부스트 (외국 관광객 여행 패턴, 약한 신호 × 0.5)
_LANGUAGE_BOOST = {
    "zh": {"shopping": 0.4, "gourmet": 0.4, "heritage": 0.3},  # 중국어
    "ja": {"heritage": 0.4, "aesthetic": 0.4, "healing": 0.3},  # 일본어
    "en": {"heritage": 0.3, "mood_nature": 0.4, "aesthetic": 0.3},  # 영어
}


def build_cold_start_vector(
    companion: str | None = None,
    age: int | None = None,
    gender: str | None = None,
    language: str | None = None,
    country: str | None = None,
    content_type_ids: list[int] | None = None,
    segment_vectors: dict | None = None,
) -> np.ndarray:
    """
    사용자 메타데이터로 cold-start 임시 벡터 생성.
    학습된 벡터가 없을 때 코사인 유사도 계산에 사용.

    segment_vectors가 있으면 (CSV 통계 기반) 가중 합산으로 base 생성.
    없으면 기존 하드코딩 규칙으로 폴백.
    """
    # ── 세그먼트 벡터 기반 (데이터 드리븐) ──
    if segment_vectors and len(segment_vectors) > 0:
        weights = {"nationality": 0.5, "age": 0.3, "gender": 0.2}
        weighted_sum = np.zeros(len(DIM_ORDER), dtype=np.float32)
        total_weight = 0.0

        for seg_type, vec in segment_vectors.items():
            w = weights.get(seg_type, 0.1)
            weighted_sum += w * vec
            total_weight += w

        if total_weight > 0:
            weighted_sum /= total_weight

        scores = {dim: float(weighted_sum[i]) for i, dim in enumerate(DIM_ORDER)}

    else:
        # ── 기존 하드코딩 규칙 폴백 ──
        scores = {dim: 0.0 for dim in DIM_ORDER}

        # 1. 동행 유형 → 분위기 차원 직접 부스트
        if companion:
            boost = _COMPANION_BOOST.get(companion.lower(), {})
            for dim, val in boost.items():
                scores[dim] = max(scores[dim], val)

        # 2. 나이대 → 보조 부스트
        if age:
            for (low, high), boost in _AGE_BOOST:
                if low <= age <= high:
                    for dim, val in boost.items():
                        scores[dim] = max(scores[dim], val * 0.6)
                    break

        # 3. 언어 → 약한 부스트
        if language:
            lang_boost = _LANGUAGE_BOOST.get(language.lower(), {})
            for dim, val in lang_boost.items():
                scores[dim] = max(scores[dim], val * 0.5)

    # 카테고리 선택은 항상 적용 (세그먼트 벡터 위에 오버라이드)
    for ct_id in (content_type_ids or []):
        cat_dim = _CATEGORY_DIM.get(ct_id)
        if cat_dim:
            scores[cat_dim] = 1.0

    return np.array([scores[dim] for dim in DIM_ORDER], dtype=np.float32)


def apply_vector_delta(user_vec: np.ndarray, delta: dict, lr: float = 0.25) -> np.ndarray:
    """
    사용자 벡터에 delta dict를 학습률로 반영. (refine_text LLM 결과 적용)
    delta: {dim_name: value (-1.0 ~ 1.0)}
    """
    result = user_vec.copy()
    for i, dim in enumerate(DIM_ORDER):
        d = delta.get(dim, 0.0)
        if d != 0.0:
            result[i] = float(np.clip(result[i] + lr * d, -1.0, 1.0))
    return result


def apply_vector_choices(user_vec: np.ndarray, choices: list[str], lr: float = 0.20) -> np.ndarray:
    """
    선택지 차원명 목록으로 사용자 벡터 해당 차원 직접 상향. (refine_choices 적용)
    choices: 차원명 리스트 (예: ["healing", "indoor"])
    """
    result = user_vec.copy()
    dim_index = {dim: i for i, dim in enumerate(DIM_ORDER)}
    for dim in choices:
        if dim in dim_index:
            i = dim_index[dim]
            result[i] = float(np.clip(result[i] + lr, -1.0, 1.0))
    return result


def rank_places(places: list[dict], user_vec: np.ndarray, top_n: int = 20) -> list[dict]:
    """
    places: list of dict (attr_vector 포함)
    user_vec: 24차원 사용자 벡터
    반환: similarity_score 내림차순 상위 top_n개
    """
    if len(places) == 0:
        return []

    # 장소 벡터 행렬로 만들어서 한번에 계산
    attr_matrix = np.stack([p["attr_vector"] for p in places])  # (N, 24)
    norms = np.linalg.norm(attr_matrix, axis=1)                 # (N,)
    user_norm = np.linalg.norm(user_vec)

    if user_norm == 0:
        scores = np.zeros(len(places))
    else:
        dots = attr_matrix @ user_vec                            # (N,)
        norms = np.where(norms == 0, 1e-9, norms)
        scores = dots / (norms * user_norm)

    # 상위 top_n 인덱스
    top_idx = np.argsort(scores)[::-1][:top_n]

    result = []
    for i in top_idx:
        p = places[i].copy()
        p.pop("attr_vector", None)
        p["similarity_score"] = round(float(scores[i]), 4)
        result.append(p)

    return result
