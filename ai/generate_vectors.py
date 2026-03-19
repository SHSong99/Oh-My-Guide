"""
전체 50,472건 attraction_vectors 생성 및 DB 저장.

전략:
  - overview 있는 3,663건: LLM 태깅 결과 (anchor_tagged.csv + bulk_tagged.csv) 사용
  - 나머지 ~46,800건:      LLM 태깅 데이터를 학습셋으로 KNN 회귀 예측
                           (TF-IDF on title+overview + content_type_id 원-핫)

사용법:
  py -3 generate_vectors.py         # 벡터 생성 + DB 저장
  py -3 generate_vectors.py stats   # 통계만 출력 (DB 미수정)
"""

import csv
import json
import os
import sys

import numpy as np
import psycopg2
from dotenv import load_dotenv
from sklearn.neighbors import KNeighborsRegressor
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import normalize
from sklearn.feature_extraction.text import TfidfVectorizer
from scipy.sparse import hstack, csr_matrix

load_dotenv()

# ── 설정 ──────────────────────────────────────────────────────────────────────

DB_CONFIG = {
    "host":     os.getenv("DB_HOST", "localhost"),
    "port":     int(os.getenv("DB_PORT", 5432)),
    "dbname":   os.getenv("DB_NAME", "ohmyguide"),
    "user":     os.getenv("DB_USER", "admin"),
    "password": os.getenv("DB_PASSWORD", "1234"),
}

ANCHOR_FILE = "anchor_tagged.csv"
BULK_FILE   = "bulk_tagged.csv"

# 24차원 키 이름 (DB 저장 기준)
CATEGORY_DIMS  = ["nature", "culture", "festival", "activity", "shopping", "food", "cafe", "lodging"]
MOOD_DIMS_DB   = ["healing", "aesthetic", "gourmet", "learning", "heritage",
                   "mood_nature", "romantic", "family", "active", "nightlife"]
PRACTICAL_DIMS = ["free_entry", "parking_available", "pet_friendly", "baby_friendly", "indoor", "outdoor"]

# content_type_id 목록 (원-핫 인코딩용)
ALL_CONTENT_TYPES = [12, 14, 15, 25, 28, 32, 38, 39]

# content_type_id → 카테고리 차원명
CATEGORY_MAP = {
    12: "nature", 14: "culture", 15: "festival",
    28: "activity", 38: "shopping", 39: "food", 32: "lodging",
}

CAFE_TITLE_KW  = ["카페", "커피", "디저트", "베이커리", "빵집", "케이크", "로스터리", "브루어리"]
INDOOR_KW  = ["실내", "박물관", "미술관", "전시관", "공연장", "극장", "아쿠아리움",
               "수족관", "과학관", "쇼핑몰", "백화점", "마트", "센터", "홀", "스튜디오"]
OUTDOOR_KW = ["야외", "공원", "해변", "해수욕장", "계곡", "폭포", "등산", "트레킹",
               "올레길", "들판", "숲", "광장", "정원", "산책로"]


# ── 특성 추출 ─────────────────────────────────────────────────────────────────

def make_text(title: str, overview: str) -> str:
    """title + overview 합산 텍스트."""
    parts = []
    if title:
        parts.append(title)
    if overview:
        parts.append(overview[:500])  # overview 앞 500자만 사용
    return " ".join(parts)


def make_content_type_onehot(content_type_id: int) -> list:
    """content_type_id → 원-핫 벡터 (len=8)."""
    return [1.0 if ct == content_type_id else 0.0 for ct in ALL_CONTENT_TYPES]


def build_feature_matrix(records: list, tfidf: TfidfVectorizer | None = None, fit: bool = False):
    """
    records: list of (content_type_id, title, overview)
    반환: scipy sparse matrix (N, tfidf_features + 8)
    """
    texts = [make_text(r[1], r[2]) for r in records]
    onehots = [make_content_type_onehot(r[0]) for r in records]

    if fit:
        tfidf_mat = tfidf.fit_transform(texts)
    else:
        tfidf_mat = tfidf.transform(texts)

    onehot_mat = csr_matrix(np.array(onehots, dtype=np.float32))
    return hstack([tfidf_mat, onehot_mat])


# ── KNN 모델 학습 + 예측 ──────────────────────────────────────────────────────

def train_and_predict(
    train_records: list,   # list of (content_type_id, title, overview)
    train_labels: np.ndarray,   # shape (N_train, 10)
    pred_records: list,    # list of (content_type_id, title, overview)
    k: int = 10,
) -> np.ndarray:
    """
    LLM 태깅 데이터로 KNN 학습 → 미태깅 데이터 예측.
    반환: shape (N_pred, 10), 값 범위 0.0~1.0
    """
    print(f"  TF-IDF 벡터화 (학습 {len(train_records)}건)...")
    tfidf = TfidfVectorizer(
        max_features=5000,
        ngram_range=(1, 2),
        sublinear_tf=True,
        min_df=2,
    )

    X_train = build_feature_matrix(train_records, tfidf, fit=True)
    print(f"  특성 차원: {X_train.shape[1]}")

    print(f"  KNN 학습 (k={k})...")
    knn = KNeighborsRegressor(
        n_neighbors=k,
        metric="cosine",
        algorithm="brute",
        weights="distance",
        n_jobs=-1,
    )
    knn.fit(X_train, train_labels)

    print(f"  예측 ({len(pred_records)}건)...")
    batch_size = 5000
    preds = []
    for i in range(0, len(pred_records), batch_size):
        batch = pred_records[i:i + batch_size]
        X_batch = build_feature_matrix(batch, tfidf, fit=False)
        pred_batch = knn.predict(X_batch)
        preds.append(pred_batch)
        done = min(i + batch_size, len(pred_records))
        print(f"    {done:,}/{len(pred_records):,}건 예측...")

    result = np.clip(np.vstack(preds), 0.0, 1.0)
    return result


# ── CSV 로드 ──────────────────────────────────────────────────────────────────

def load_llm_tagged() -> dict:
    """anchor_tagged.csv + bulk_tagged.csv → {attr_id: scores_dict}"""
    tagged = {}
    csv_mood_dims = ["healing", "aesthetic", "gourmet", "learning", "heritage",
                     "nature", "romantic", "family", "active", "nightlife"]
    for filepath in [ANCHOR_FILE, BULK_FILE]:
        if not os.path.exists(filepath):
            print(f"  [경고] 파일 없음: {filepath}")
            continue
        with open(filepath, "r", encoding="utf-8-sig") as f:
            for row in csv.DictReader(f):
                attr_id = int(row["attr_id"])
                tagged[attr_id] = {
                    dim: float(row.get(dim, 0.0))
                    for dim in csv_mood_dims
                }
    return tagged


# ── 카테고리/실용 차원 (규칙 기반) ────────────────────────────────────────────

def category_dims(content_type_id: int, title: str, llm_aesthetic: float) -> dict:
    vec = {dim: 0.0 for dim in CATEGORY_DIMS}
    cat_dim = CATEGORY_MAP.get(content_type_id)
    if cat_dim:
        vec[cat_dim] = 1.0

    # cafe 판별
    if content_type_id == 39:
        is_cafe = any(kw in (title or "") for kw in CAFE_TITLE_KW)
        if not is_cafe and llm_aesthetic >= 0.7:
            is_cafe = True
        if is_cafe:
            vec["cafe"] = 1.0
            vec["food"] = 0.3

    return vec


def practical_dims(title: str, overview: str) -> dict:
    text = (title or "") + " " + (overview or "")
    vec = {dim: 0.0 for dim in PRACTICAL_DIMS}
    if any(kw in text for kw in INDOOR_KW):
        vec["indoor"] = 0.8
    if any(kw in text for kw in OUTDOOR_KW):
        vec["outdoor"] = 0.8
    return vec


# ── 메인 ──────────────────────────────────────────────────────────────────────

def run():
    print("=" * 60)
    print("전체 attraction_vectors 생성 및 DB 저장")
    print("=" * 60)

    # 1. LLM 태깅 CSV 로드
    print("\n[1/5] LLM 태깅 CSV 로드...")
    llm_tagged = load_llm_tagged()
    print(f"  → {len(llm_tagged):,}건 로드 완료")

    # 2. DB에서 전체 attractions 조회
    print("\n[2/5] attractions 조회...")
    conn = psycopg2.connect(**DB_CONFIG)
    cur  = conn.cursor()
    cur.execute("SELECT attr_id, content_type_id, title, overview FROM attractions ORDER BY attr_id")
    all_places = cur.fetchall()
    print(f"  → {len(all_places):,}건 조회 완료")

    # 3. 학습셋 / 예측셋 분리
    print("\n[3/5] KNN 학습 및 예측...")
    csv_mood_dims = ["healing", "aesthetic", "gourmet", "learning", "heritage",
                     "nature", "romantic", "family", "active", "nightlife"]

    train_records = []
    train_labels  = []
    pred_records  = []
    pred_ids      = []

    for attr_id, ct_id, title, overview in all_places:
        rec = (ct_id, title or "", overview or "")
        if attr_id in llm_tagged:
            train_records.append(rec)
            scores = llm_tagged[attr_id]
            train_labels.append([scores[d] for d in csv_mood_dims])
        else:
            pred_records.append(rec)
            pred_ids.append(attr_id)

    train_labels = np.array(train_labels, dtype=np.float32)
    print(f"  학습셋: {len(train_records):,}건 / 예측셋: {len(pred_records):,}건")

    # KNN 예측
    pred_scores = train_and_predict(train_records, train_labels, pred_records, k=10)

    # 레포츠(28)는 active 강제 1.0
    active_idx = csv_mood_dims.index("active")
    for i, (ct_id, _, _) in enumerate(pred_records):
        if ct_id == 28:
            pred_scores[i][active_idx] = 1.0

    # 예측 결과 딕셔너리화
    pred_mood_map = {}
    for i, attr_id in enumerate(pred_ids):
        pred_mood_map[attr_id] = {
            dim: round(float(pred_scores[i][j]), 3)
            for j, dim in enumerate(csv_mood_dims)
        }

    # 4. 24차원 벡터 조합
    print("\n[4/5] 24차원 벡터 조합...")
    place_map = {r[0]: r for r in all_places}  # attr_id → row
    vectors = []

    for attr_id, ct_id, title, overview in all_places:
        if attr_id in llm_tagged:
            mood_scores_csv = llm_tagged[attr_id]
        else:
            mood_scores_csv = pred_mood_map[attr_id]

        aesthetic = mood_scores_csv.get("aesthetic", 0.0)
        vec = {}

        # 카테고리 8개
        vec.update(category_dims(ct_id, title, aesthetic))

        # 분위기 10개 (CSV "nature" → DB "mood_nature")
        for dim in csv_mood_dims:
            db_key = "mood_nature" if dim == "nature" else dim
            vec[db_key] = round(float(mood_scores_csv[dim]), 3)

        # 레포츠 active 강제
        if ct_id == 28:
            vec["active"] = 1.0

        # 실용 6개
        vec.update(practical_dims(title, overview))

        vectors.append((attr_id, json.dumps(vec, ensure_ascii=False)))

    print(f"  → {len(vectors):,}건 벡터 생성 완료")

    # 5. DB 저장
    print("\n[5/5] DB 저장...")
    cur.execute("DELETE FROM attraction_vectors")

    batch_size = 1000
    total = len(vectors)
    for i in range(0, total, batch_size):
        batch = vectors[i:i + batch_size]
        cur.executemany(
            """INSERT INTO attraction_vectors (attr_id, attraction_vector, created_at, updated_at)
               VALUES (%s, %s, NOW(), NOW())""",
            batch,
        )
        done = min(i + batch_size, total)
        if done % 10000 == 0 or done == total:
            print(f"  {done:,}/{total:,} 저장...")

    conn.commit()
    cur.close()
    conn.close()

    print(f"\n완료: {total:,}건 → attraction_vectors 테이블 저장")
    print(f"  LLM 태깅 직접 사용: {len(train_records):,}건")
    print(f"  KNN 예측 사용:      {len(pred_records):,}건")


def show_stats():
    print("=" * 60)
    print("통계")
    print("=" * 60)
    llm_tagged = load_llm_tagged()
    print(f"LLM 태깅 결과: {len(llm_tagged):,}건")

    conn = psycopg2.connect(**DB_CONFIG)
    cur  = conn.cursor()
    cur.execute("SELECT COUNT(*) FROM attractions")
    total = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM attraction_vectors")
    vectorized = cur.fetchone()[0]
    cur.close()
    conn.close()

    print(f"전체 attractions:        {total:,}건")
    print(f"현재 attraction_vectors: {vectorized:,}건")
    print(f"실행 시:")
    print(f"  LLM 태깅 직접 사용: {len(llm_tagged):,}건")
    print(f"  KNN 예측 사용:      {total - len(llm_tagged):,}건")


if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "stats":
        show_stats()
    else:
        run()
