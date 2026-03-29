"""
PostgreSQL 연결 및 쿼리.
"""

import json
import os

import psycopg2
import psycopg2.extras
from dotenv import load_dotenv

from vector_utils import parse_vector

load_dotenv()

DB_CONFIG = {
    "host":     os.getenv("DB_HOST", "localhost"),
    "port":     int(os.getenv("DB_PORT", 5432)),
    "dbname":   os.getenv("DB_NAME", "ohmyguide"),
    "user":     os.getenv("DB_USER", "admin"),
    "password": os.getenv("DB_PASSWORD", "1234"),
}


def get_conn():
    return psycopg2.connect(**DB_CONFIG, cursor_factory=psycopg2.extras.RealDictCursor)


# ── 사용자 벡터 ───────────────────────────────────────────────────────────────

def get_user_vector(user_id: int):
    """
    user_vectors 테이블에서 사용자 벡터 조회.
    반환: (numpy 배열, cold_start: bool)
    cold_start=True 이면 벡터 없음 → 거리순 폴백 필요
    """
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT preference_vector FROM user_vectors WHERE user_id = %s",
                (user_id,)
            )
            row = cur.fetchone()

    if row is None:
        return None, True

    vec_dict = row["preference_vector"]
    if isinstance(vec_dict, str):
        vec_dict = json.loads(vec_dict)

    return parse_vector(vec_dict), False


def save_user_vector(user_id: int, vec) -> None:
    """
    사용자 벡터를 user_vectors 테이블에 저장 (없으면 INSERT, 있으면 UPDATE).
    vec: numpy 배열 (24차원)
    """
    from vector_utils import DIM_ORDER
    vec_dict = {dim: round(float(vec[i]), 6) for i, dim in enumerate(DIM_ORDER)}

    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                INSERT INTO user_vectors (user_id, preference_vector, created_at, updated_at)
                VALUES (%s, %s, NOW(), NOW())
                ON CONFLICT (user_id)
                DO UPDATE SET preference_vector = EXCLUDED.preference_vector,
                              updated_at = NOW()
                """,
                (user_id, json.dumps(vec_dict))
            )
        conn.commit()


# ── 세그먼트 벡터 (Cold Start) ─────────────────────────────────────────────────

def get_segment_vectors(nationality: str | None, age_group: str | None, gender: str | None) -> dict:
    """
    segment_vectors 테이블에서 국적/연령/성별 세그먼트 벡터 조회.
    반환: {"nationality": np.ndarray, "age": np.ndarray, "gender": np.ndarray}
    매칭 없는 항목은 포함하지 않음.
    """
    result = {}
    lookups = [
        ("nationality", nationality),
        ("age", age_group),
        ("gender", gender),
    ]

    with get_conn() as conn:
        with conn.cursor() as cur:
            for seg_type, seg_key in lookups:
                if not seg_key:
                    continue
                cur.execute(
                    "SELECT segment_vector FROM segment_vectors WHERE segment_type = %s AND segment_key = %s",
                    (seg_type, seg_key),
                )
                row = cur.fetchone()
                if row:
                    vec_dict = row["segment_vector"]
                    if isinstance(vec_dict, str):
                        vec_dict = json.loads(vec_dict)
                    result[seg_type] = parse_vector(vec_dict)

    return result


# ── 반경 내 장소 + 벡터 조회 ─────────────────────────────────────────────────

def get_places_in_radius(
    lat: float,
    lng: float,
    radius_km: float,
    content_type_ids: list[int],
    excluded_attr_ids: list[int] | None = None,
) -> list[dict]:
    """
    PostGIS ST_DWithin으로 반경 내 장소 조회.
    attraction_vectors와 JOIN하여 벡터도 함께 반환.
    content_type_ids가 비어 있으면 전체 카테고리.
    excluded_attr_ids: 제외할 장소 ID 목록 (재추천 시 이미 본 장소 제외).
    """
    radius_m = radius_km * 1000

    type_filter = ""
    exclude_filter = ""
    params = [lng, lat, lng, lat, radius_m]

    if content_type_ids:
        type_filter = "AND a.content_type_id = ANY(%s)"
        params.append(content_type_ids)

    if excluded_attr_ids:
        exclude_filter = "AND a.attr_id != ALL(%s)"
        params.append(excluded_attr_ids)

    query = f"""
        SELECT
            a.attr_id,
            a.content_id,
            a.title,
            a.addr1,
            a.latitude::float       AS latitude,
            a.longitude::float      AS longitude,
            a.content_type_id,
            a.first_image1,
            a.overview,
            ST_Distance(
                ST_MakePoint(a.longitude::float, a.latitude::float)::geography,
                ST_MakePoint(%s, %s)::geography
            ) / 1000.0              AS distance_km,
            v.attraction_vector
        FROM attractions a
        JOIN attraction_vectors v ON a.attr_id = v.attr_id
        WHERE a.latitude IS NOT NULL
          AND a.longitude IS NOT NULL
          AND ST_DWithin(
                ST_MakePoint(a.longitude::float, a.latitude::float)::geography,
                ST_MakePoint(%s, %s)::geography,
                %s
              )
          {type_filter}
          {exclude_filter}
        ORDER BY distance_km
    """

    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(query, params)
            rows = cur.fetchall()

    places = []
    for row in rows:
        vec_dict = row["attraction_vector"]
        if isinstance(vec_dict, str):
            vec_dict = json.loads(vec_dict)

        places.append({
            "attr_id":         row["attr_id"],
            "content_id":      row["content_id"],
            "title":           row["title"],
            "addr1":           row["addr1"],
            "latitude":        row["latitude"],
            "longitude":       row["longitude"],
            "content_type_id": row["content_type_id"],
            "first_image1":    row["first_image1"],
            "overview":        row["overview"],
            "distance_km":     round(float(row["distance_km"]), 3),
            "attr_vector":     parse_vector(vec_dict),  # numpy 배열
        })

    return places
