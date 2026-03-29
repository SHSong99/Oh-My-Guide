"""
부산 외국인 관광객 통계 CSV → segment_vectors 테이블 생성.

CSV의 종합(복수응답) 활동 비율(%)을 24차원 벡터로 매핑하여
(segment_type, segment_key) 별로 DB에 저장.

사용법:
  DB_HOST=localhost py -3 generate_segment_vectors.py
"""

import csv
import json
import os
import sys

import numpy as np
import psycopg2
from dotenv import load_dotenv

from vector_utils import DIM_ORDER

load_dotenv()

DB_CONFIG = {
    "host":     os.getenv("DB_HOST", "localhost"),
    "port":     int(os.getenv("DB_PORT", 5432)),
    "dbname":   os.getenv("DB_NAME", "ohmyguide"),
    "user":     os.getenv("DB_USER", "admin"),
    "password": os.getenv("DB_PASSWORD", "1234"),
}

CSV_FILE = os.path.join(os.path.dirname(__file__), "busan_tourist_data.csv")

# ── CSV 종합 컬럼 인덱스 (0-based) ──
# 20: 맛집탐방, 21: 레저스포츠, 22: 자연풍경, 23: 영화/드라마촬영지
# 24: 역사문화유적, 25: 전통문화체험, 26: 공연/축제/이벤트, 27: 쇼핑
# 28: 산/해변길/트레킹, 29: 박물관/테마공원, 30: 회의/업무, 31: 유흥/오락
# 32: 종교/순례, 33: 해양활동, 34: 치료미용, 35: 기타

# CSV 종합 컬럼 → 24차원 매핑 (컬럼 인덱스 → [(차원명, 가중치)])
COLUMN_TO_DIMS = {
    20: [("food", 0.7), ("gourmet", 0.7), ("cafe", 0.3)],           # 맛집탐방
    21: [("activity", 0.8), ("active", 0.8)],                        # 레저·스포츠
    22: [("nature", 0.8), ("mood_nature", 0.8)],                     # 자연풍경감상
    23: [("aesthetic", 0.7), ("culture", 0.3)],                      # 영화/드라마촬영지
    24: [("heritage", 0.8), ("culture", 0.6), ("learning", 0.5)],    # 역사·문화유적
    25: [("culture", 0.7), ("heritage", 0.5)],                       # 전통문화체험
    26: [("festival", 0.9)],                                         # 공연/축제/이벤트
    27: [("shopping", 0.9)],                                         # 쇼핑
    28: [("nature", 0.5), ("active", 0.6), ("outdoor", 0.7)],       # 산/해변길/트레킹
    29: [("culture", 0.4), ("learning", 0.6)],                       # 박물관/테마공원
    31: [("nightlife", 0.9)],                                        # 유흥/오락
    33: [("active", 0.4), ("outdoor", 0.5), ("nature", 0.3)],       # 해양활동
    34: [("healing", 0.8)],                                          # 치료·미용
}

# ── 구분_소분류 → segment_type, segment_key 매핑 ──
SEGMENT_MAP = {
    # 성별
    "남성": ("gender", "male"),
    "여성": ("gender", "female"),
    # 연령
    "15~19세": ("age", "10s"),
    "20대": ("age", "20s"),
    "30대": ("age", "30s"),
    "40대": ("age", "40s"),
    "50대": ("age", "50s"),
    "60세 이상": ("age", "60s"),
    # 국적
    "일본": ("nationality", "Japan"),
    "중국": ("nationality", "China"),
    "대만": ("nationality", "Taiwan"),
    "홍콩": ("nationality", "HongKong"),
    "태국": ("nationality", "Thailand"),
    "싱가포르": ("nationality", "Singapore"),
    "말레이시아": ("nationality", "Malaysia"),
    "필리핀": ("nationality", "Philippines"),
    "베트남": ("nationality", "Vietnam"),
    "인도네시아": ("nationality", "Indonesia"),
    "인도": ("nationality", "India"),
    "미국": ("nationality", "USA"),
    "캐나다": ("nationality", "Canada"),
    "영국": ("nationality", "UK"),
    "호주": ("nationality", "Australia"),
    "프랑스": ("nationality", "France"),
    "독일": ("nationality", "Germany"),
    "러시아": ("nationality", "Russia"),
    "그 외 국가": ("nationality", "Other"),
}


def csv_row_to_vector(row_values: list) -> np.ndarray:
    """CSV 행의 종합 컬럼(인덱스 20~35)을 24차원 벡터로 변환."""
    scores = {dim: 0.0 for dim in DIM_ORDER}

    for col_idx, dim_mappings in COLUMN_TO_DIMS.items():
        try:
            pct = float(row_values[col_idx])
        except (ValueError, IndexError):
            continue

        # 비율(0~100)을 0~1로 변환
        val = pct / 100.0

        for dim, weight in dim_mappings:
            scores[dim] = max(scores[dim], val * weight)

    # numpy 배열로 변환
    vec = np.array([scores[dim] for dim in DIM_ORDER], dtype=np.float32)

    # 최대값으로 정규화 (0~1 범위)
    max_val = vec.max()
    if max_val > 0:
        vec = vec / max_val

    return vec


def run():
    print("=" * 60)
    print("부산 외국인 관광객 통계 → segment_vectors 생성")
    print("=" * 60)

    # 1. CSV 파싱
    print("\n[1/3] CSV 파싱...")
    segments = []

    with open(CSV_FILE, "r", encoding="utf-8-sig") as f:
        reader = csv.reader(f)
        header = next(reader)

        for row in reader:
            sub_category = row[2].strip()
            sample_count = int(row[3]) if row[3] else 0

            if sub_category in SEGMENT_MAP:
                seg_type, seg_key = SEGMENT_MAP[sub_category]
                vec = csv_row_to_vector(row)
                segments.append((seg_type, seg_key, vec, sample_count))
                print(f"  {seg_type:12s} / {seg_key:15s} (n={sample_count:4d})")

    print(f"  → {len(segments)}건 파싱 완료")

    # 2. DB 테이블 생성 (없으면)
    print("\n[2/3] DB 테이블 확인...")
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()

    cur.execute("""
        CREATE TABLE IF NOT EXISTS segment_vectors (
            id              BIGSERIAL PRIMARY KEY,
            segment_type    VARCHAR(20)  NOT NULL,
            segment_key     VARCHAR(50)  NOT NULL,
            segment_vector  JSON         NOT NULL,
            source          VARCHAR(20)  DEFAULT 'csv',
            sample_count    INTEGER      DEFAULT 0,
            created_at      TIMESTAMP    DEFAULT NOW(),
            updated_at      TIMESTAMP    DEFAULT NOW(),
            UNIQUE (segment_type, segment_key)
        )
    """)
    conn.commit()
    print("  → 테이블 준비 완료")

    # 3. UPSERT
    print("\n[3/3] DB 저장...")
    for seg_type, seg_key, vec, sample_count in segments:
        vec_dict = {dim: round(float(vec[i]), 4) for i, dim in enumerate(DIM_ORDER)}

        cur.execute("""
            INSERT INTO segment_vectors (segment_type, segment_key, segment_vector, source, sample_count, created_at, updated_at)
            VALUES (%s, %s, %s, 'csv', %s, NOW(), NOW())
            ON CONFLICT (segment_type, segment_key)
            DO UPDATE SET segment_vector = EXCLUDED.segment_vector,
                          source = 'csv',
                          sample_count = EXCLUDED.sample_count,
                          updated_at = NOW()
        """, (seg_type, seg_key, json.dumps(vec_dict, ensure_ascii=False), sample_count))

    conn.commit()
    cur.close()
    conn.close()

    print(f"\n완료: {len(segments)}건 → segment_vectors 테이블 저장")
    print("  - nationality: ", sum(1 for s in segments if s[0] == "nationality"), "건")
    print("  - age:         ", sum(1 for s in segments if s[0] == "age"), "건")
    print("  - gender:      ", sum(1 for s in segments if s[0] == "gender"), "건")


if __name__ == "__main__":
    run()
