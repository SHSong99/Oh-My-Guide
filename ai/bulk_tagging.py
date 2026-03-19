"""
Step B: gpt-4.1-nano로 나머지 장소 대량 태깅.
앵커 태깅 결과에서 Few-shot 예시를 뽑아 프롬프트에 포함.

사용법:
  py -3 bulk_tagging.py run       # 대량 태깅 실행
  py -3 bulk_tagging.py status    # 진행 상황 확인
  py -3 bulk_tagging.py save_db   # 태깅 결과를 DB에 저장
"""

import csv
import json
import os
import sys
import time
import re
from concurrent.futures import ThreadPoolExecutor, as_completed

import psycopg2
import requests
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", 5432)),
    "dbname": os.getenv("DB_NAME", "ohmyguide"),
    "user": os.getenv("DB_USER", "admin"),
    "password": os.getenv("DB_PASSWORD", "1234"),
}

GMS_KEY = os.getenv("GMS_KEY")
GMS_BASE_URL = os.getenv("GMS_BASE_URL", "https://gms.ssafy.io/gmsapi")
OPENAI_URL = f"{GMS_BASE_URL}/api.openai.com/v1/responses"

ANCHOR_FILE = "anchor_tagged.csv"
BULK_FILE = "bulk_tagged.csv"

CONTENT_TYPE_NAMES = {
    12: "관광지", 14: "문화시설", 15: "축제공연행사",
    25: "여행코스", 28: "레포츠", 32: "숙박", 38: "쇼핑", 39: "음식점",
}

MOOD_DIMS = [
    "healing", "aesthetic", "gourmet", "learning", "heritage",
    "mood_nature", "romantic", "family", "active", "nightlife",
]

MOOD_DESC = {
    "healing": "힐링/휴식/산책 (공원, 숲길, 온천, 정원)",
    "aesthetic": "감성/분위기/포토 (인테리어, 벽화, 한옥, 루프탑)",
    "gourmet": "미식/맛집 (전문점, 요리, 셰프, 코스요리)",
    "learning": "학습/문화/공연 (박물관, 전시, 극장, 공연장)",
    "heritage": "역사/유적/전통 (사찰, 궁궐, 문화재, 독립운동)",
    "mood_nature": "자연경관/산/바다 (등산, 계곡, 해변, 폭포)",
    "romantic": "연인/데이트 (야경, 노을, 분수, 유람선)",
    "family": "가족/어린이 (놀이터, 동물원, 체험, 캠핑)",
    "active": "스포츠/액티비티 (서핑, 래프팅, 스키, 자전거)",
    "nightlife": "야간/축제/밤문화 (야경, 야시장, 라이브, 축제)",
}


# ── Few-shot 예시 생성 ───────────────────────────────────────────────────────

def load_fewshot_examples() -> str:
    """앵커 결과에서 다양한 Few-shot 예시 10건 선정."""
    with open(ANCHOR_FILE, "r", encoding="utf-8-sig") as f:
        anchors = list(csv.DictReader(f))

    # 각 차원에서 가장 높은 점수를 가진 장소 1개씩 선정 (중복 제거)
    selected = {}
    for dim in MOOD_DIMS:
        best = max(anchors, key=lambda r: float(r[dim]))
        if best["attr_id"] not in selected:
            selected[best["attr_id"]] = best

    # 10개 미만이면 추가
    if len(selected) < 10:
        for a in anchors:
            if a["attr_id"] not in selected:
                selected[a["attr_id"]] = a
            if len(selected) >= 10:
                break

    examples = []
    for r in list(selected.values())[:10]:
        scores = {d: float(r[d]) for d in MOOD_DIMS}
        examples.append(
            f"제목: {r['title']}\n"
            f"점수: {json.dumps(scores, ensure_ascii=False)}"
        )

    return "\n\n".join(examples)


# ── 프롬프트 ─────────────────────────────────────────────────────────────────

def build_prompt(fewshot: str) -> str:
    dim_desc = "\n".join(f"- {k}: {v}" for k, v in MOOD_DESC.items())
    return f"""한국 관광지의 분위기를 10개 차원으로 점수(0.0~1.0)를 매기는 작업입니다.

**10개 차원:**
{dim_desc}

**점수 기준:** 0.0=무관, 0.1~0.3=약간, 0.4~0.6=보통, 0.7~0.9=강함, 1.0=완벽 대표

**예시:**
{fewshot}

아래 관광지의 점수를 JSON으로만 응답하세요. 다른 텍스트 없이 JSON만 출력하세요.

제목: {{title}}
유형: {{content_type}}
설명: {{overview}}"""


# ── API 호출 ─────────────────────────────────────────────────────────────────

def call_nano(title: str, content_type: str, overview: str, system_prompt: str) -> dict:
    """gpt-4.1-nano로 태깅."""
    user_msg = f"제목: {title}\n유형: {content_type}\n설명: {overview[:2000]}"

    body = {
        "model": "gpt-4.1-nano",
        "instructions": system_prompt,
        "input": user_msg,
        "temperature": 0.1,
        "max_output_tokens": 200,
    }

    for attempt in range(3):
        try:
            resp = requests.post(
                OPENAI_URL,
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {GMS_KEY}",
                },
                json=body,
                timeout=30,
            )

            if resp.status_code == 429:
                time.sleep(5 * (attempt + 1))
                continue

            if resp.status_code != 200:
                raise Exception(f"API {resp.status_code}: {resp.text[:100]}")

            data = resp.json()
            text = data["output"][0]["content"][0]["text"]

            # JSON 추출: 직접 파싱 시도 → 실패 시 regex 폴백
            try:
                scores = json.loads(text.strip())
            except json.JSONDecodeError:
                json_match = re.search(r'\{[^{}]*\}', text, re.DOTALL)
                if not json_match:
                    raise Exception(f"JSON 파싱 실패: {text[:100]}")
                scores = json.loads(json_match.group())
            for dim in MOOD_DIMS:
                if dim not in scores:
                    scores[dim] = 0.0
                scores[dim] = max(0.0, min(1.0, round(float(scores[dim]), 2)))

            return scores

        except Exception as e:
            if attempt == 2:
                raise
            time.sleep(2)

    raise Exception("3회 재시도 실패")


# ── 대량 태깅 ────────────────────────────────────────────────────────────────

def run_bulk():
    print("=" * 60)
    print("대량 태깅 (gpt-4.1-nano)")
    print("=" * 60)

    # Few-shot 준비
    fewshot = load_fewshot_examples()
    system_prompt = build_prompt(fewshot)
    print(f"Few-shot 예시 로드 완료")

    # 이미 태깅된 앵커 attr_id
    anchor_ids = set()
    with open(ANCHOR_FILE, "r", encoding="utf-8-sig") as f:
        for r in csv.DictReader(f):
            anchor_ids.add(int(r["attr_id"]))

    # DB에서 overview 있는 장소 조회
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    cur.execute("""
        SELECT attr_id, title, overview, content_type_id, addr1
        FROM attractions
        WHERE overview IS NOT NULL AND LENGTH(overview) > 50
          AND content_type_id NOT IN (25, 32)
        ORDER BY attr_id
    """)
    all_places = cur.fetchall()
    cur.close()
    conn.close()

    # 앵커 제외
    places = [
        {"attr_id": r[0], "title": r[1], "overview": r[2],
         "content_type_id": r[3], "addr1": r[4]}
        for r in all_places if r[0] not in anchor_ids
    ]
    print(f"태깅 대상: {len(places)}건 (앵커 {len(anchor_ids)}건 제외)")

    # 이미 태깅된 결과 로드
    tagged = {}
    if os.path.exists(BULK_FILE):
        with open(BULK_FILE, "r", encoding="utf-8-sig") as f:
            for r in csv.DictReader(f):
                tagged[int(r["attr_id"])] = r
        print(f"기존 결과 {len(tagged)}건 로드 (이어서 진행)")

    remaining = [p for p in places if p["attr_id"] not in tagged]
    print(f"남은: {remaining_count}건\n" if (remaining_count := len(remaining)) else "전부 완료!\n")

    if not remaining:
        return

    # 태깅 실행
    columns = ["attr_id", "title", "content_type_id"] + MOOD_DIMS
    success = len(tagged)
    fail = 0
    total = len(places)

    for i, place in enumerate(remaining):
        attr_id = place["attr_id"]
        title = place["title"]
        ct_name = CONTENT_TYPE_NAMES.get(place["content_type_id"], "기타")

        if (success + fail) % 100 == 0 and (success + fail) > 0:
            print(f"\n--- 진행: {success}/{total} 성공, {fail} 실패 ---\n")

        try:
            scores = call_nano(title, ct_name, place["overview"], system_prompt)

            row = {"attr_id": attr_id, "title": title, "content_type_id": place["content_type_id"]}
            row.update(scores)
            tagged[attr_id] = row
            success += 1

            # 0.3 이상만 간단 출력
            brief = " ".join(f"{d}={scores[d]:.1f}" for d in MOOD_DIMS if scores[d] >= 0.3)
            if (success % 50 == 0) or (success <= 5):
                print(f"[{success}/{total}] {title} → {brief}")

            # 100건마다 중간 저장
            if success % 100 == 0:
                _save_bulk(tagged, columns)
                print(f"  [중간 저장: {success}건]")

        except Exception as e:
            fail += 1
            if fail <= 10 or fail % 50 == 0:
                print(f"[FAIL #{fail}] {title} → {e}")
            time.sleep(2)

    # 최종 저장
    _save_bulk(tagged, columns)
    print(f"\n완료: 성공 {success}건, 실패 {fail}건")
    print(f"저장: {BULK_FILE}")
    print(f"예상 Credit 소모: ~{success} Credit")


def _save_bulk(tagged, columns):
    with open(BULK_FILE, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=columns)
        writer.writeheader()
        for row in tagged.values():
            writer.writerow({c: row.get(c, 0.0) for c in columns})


# ── 상태 확인 ────────────────────────────────────────────────────────────────

def show_status():
    anchor_count = 0
    if os.path.exists(ANCHOR_FILE):
        with open(ANCHOR_FILE, "r", encoding="utf-8-sig") as f:
            anchor_count = sum(1 for _ in csv.DictReader(f))

    bulk_count = 0
    if os.path.exists(BULK_FILE):
        with open(BULK_FILE, "r", encoding="utf-8-sig") as f:
            bulk_count = sum(1 for _ in csv.DictReader(f))

    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()
    cur.execute("""
        SELECT COUNT(*) FROM attractions
        WHERE overview IS NOT NULL AND LENGTH(overview) > 50
          AND content_type_id NOT IN (25, 32)
    """)
    total = cur.fetchone()[0]
    cur.close()
    conn.close()

    remaining = total - anchor_count - bulk_count
    print(f"전체 대상: {total}건")
    print(f"앵커 태깅: {anchor_count}건")
    print(f"대량 태깅: {bulk_count}건")
    print(f"남은: {remaining}건")
    print(f"예상 Credit: ~{remaining} Credit")


# ── DB 저장 ──────────────────────────────────────────────────────────────────

def save_to_db():
    """앵커 + 대량 태깅 결과를 합쳐서 attraction_vectors에 저장."""
    # 카테고리 8개 매핑
    CATEGORY_DIMS = ["nature", "culture", "festival", "activity", "shopping", "food", "cafe", "lodging"]
    CATEGORY_MAP = {12: 0, 14: 1, 15: 2, 28: 3, 38: 4, 39: 5, 32: 7}
    PRACTICAL_DIMS = ["free_entry", "parking_available", "pet_friendly", "baby_friendly", "indoor", "outdoor"]

    # 태깅 결과 로드
    all_tagged = {}

    if os.path.exists(ANCHOR_FILE):
        with open(ANCHOR_FILE, "r", encoding="utf-8-sig") as f:
            for r in csv.DictReader(f):
                all_tagged[int(r["attr_id"])] = r

    if os.path.exists(BULK_FILE):
        with open(BULK_FILE, "r", encoding="utf-8-sig") as f:
            for r in csv.DictReader(f):
                all_tagged[int(r["attr_id"])] = r

    print(f"태깅 결과 {len(all_tagged)}건 로드")

    # DB 연결
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()

    # 기존 벡터 삭제
    cur.execute("DELETE FROM attraction_vectors")

    inserted = 0
    for attr_id, row in all_tagged.items():
        ct_id = int(row["content_type_id"])

        # 24차원 벡터 구성
        vector = {}

        # 카테고리 8개
        for i, dim in enumerate(CATEGORY_DIMS):
            vector[dim] = 0.0
        cat_idx = CATEGORY_MAP.get(ct_id)
        if cat_idx is not None:
            vector[CATEGORY_DIMS[cat_idx]] = 1.0
        # cafe 판별: 음식점(39)이면서 분위기 태깅에서 aesthetic 높으면
        if ct_id == 39:
            title = row.get("title", "")
            aesthetic_score = float(row.get("aesthetic", 0))
            if any(kw in title for kw in ["카페", "커피", "디저트", "베이커리"]) or aesthetic_score >= 0.7:
                vector["cafe"] = 1.0
                vector["food"] = 0.3  # 음식점 속성도 약간 유지

        # 분위기 10개 (CSV와 DB 키 이름 동일)
        for dim in MOOD_DIMS:
            vector[dim] = float(row.get(dim, 0.0))

        # 실용 6개 (현재 기본값)
        for dim in PRACTICAL_DIMS:
            vector[dim] = 0.0

        vector_json = json.dumps(vector, ensure_ascii=False)

        cur.execute("""
            INSERT INTO attraction_vectors (attr_id, attraction_vector, created_at, updated_at)
            VALUES (%s, %s, NOW(), NOW())
            ON CONFLICT (attr_id) DO UPDATE SET
                attraction_vector = EXCLUDED.attraction_vector,
                updated_at = NOW()
        """, (attr_id, vector_json))

        inserted += 1

    conn.commit()
    cur.close()
    conn.close()

    print(f"DB 저장 완료: {inserted}건 → attraction_vectors 테이블")


# ── main ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("사용법:")
        print("  py -3 bulk_tagging.py run       # 대량 태깅")
        print("  py -3 bulk_tagging.py status     # 진행 상황")
        print("  py -3 bulk_tagging.py save_db    # DB 저장")
        sys.exit(1)

    cmd = sys.argv[1]
    if cmd == "run":
        run_bulk()
    elif cmd == "status":
        show_status()
    elif cmd == "save_db":
        save_to_db()
    else:
        print(f"알 수 없는 명령: {cmd}")
