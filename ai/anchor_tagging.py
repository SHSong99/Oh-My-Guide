"""
Step A: 앵커 100건 선정 + gemini-2.5-pro로 분위기 10차원 정밀 태깅.

사용법:
  py -3 anchor_tagging.py select    # 앵커 100건 선정 → anchor_places.csv
  py -3 anchor_tagging.py tag       # gemini-2.5-pro로 태깅 → anchor_tagged.csv
  py -3 anchor_tagging.py status    # 태깅 진행 상황 확인
"""

import csv
import json
import os
import sys
import time
import random
import re

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
GEMINI_URL = f"{GMS_BASE_URL}/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent"

ANCHOR_FILE = "anchor_places.csv"
TAGGED_FILE = "anchor_tagged.csv"

# ── 분위기 10개 차원 정의 (프롬프트용) ────────────────────────────────────────

MOOD_DIMS = {
    "healing": "힐링/휴식/산책. 공원, 숲길, 산책로, 수목원, 온천, 정원 등 마음의 안정을 주는 장소",
    "aesthetic": "감성/분위기/포토. 인테리어, 벽화, 한옥, 루프탑, 이색적이고 예쁜 분위기의 장소",
    "gourmet": "미식/맛집. 유명 맛집, 전문점, 특색 있는 요리, 식도락을 즐길 수 있는 장소",
    "learning": "학습/문화/공연. 박물관, 미술관, 전시관, 극장, 공연장 등 배우고 감상하는 장소",
    "heritage": "역사/유적/전통. 사찰, 궁궐, 성곽, 문화재, 독립운동 유적, 전통문화를 느끼는 장소",
    "mood_nature": "자연경관/산/바다. 등산, 계곡, 해변, 폭포, 섬, 전망대 등 자연의 아름다움을 즐기는 장소",
    "romantic": "연인/데이트. 야경, 노을, 분수, 유람선, 꽃길 등 커플에게 어울리는 로맨틱한 장소",
    "family": "가족/어린이. 놀이터, 동물원, 체험농장, 키즈카페 등 가족 단위로 즐기기 좋은 장소",
    "active": "스포츠/액티비티. 서핑, 래프팅, 스키, 자전거, 클라이밍 등 몸을 움직이는 활동적인 장소",
    "nightlife": "야간/축제/밤문화. 야경, 야시장, 야간개장, 라이브 공연, 분수쇼, 축제 등 밤에 즐기는 장소",
}

TAGGING_PROMPT = """당신은 한국 관광지 분석 전문가입니다.
아래 관광지의 제목과 설명을 읽고, 10개 분위기 차원에 대해 각각 0.0~1.0 점수를 매겨주세요.

**점수 기준:**
- 0.0: 해당 분위기와 전혀 관련 없음
- 0.1~0.3: 약간 관련 있음
- 0.4~0.6: 보통 수준으로 해당됨
- 0.7~0.9: 강하게 해당됨
- 1.0: 그 분위기를 완벽하게 대표함

**10개 분위기 차원:**
{dim_descriptions}

**관광지 정보:**
- 제목: {title}
- 유형: {content_type}
- 주소: {addr}
- 설명: {overview}

**반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요:**
```json
{{
  "healing": 0.0,
  "aesthetic": 0.0,
  "gourmet": 0.0,
  "learning": 0.0,
  "heritage": 0.0,
  "mood_nature": 0.0,
  "romantic": 0.0,
  "family": 0.0,
  "active": 0.0,
  "nightlife": 0.0
}}
```"""

CONTENT_TYPE_NAMES = {
    12: "관광지",
    14: "문화시설",
    15: "축제공연행사",
    25: "여행코스",
    28: "레포츠",
    32: "숙박",
    38: "쇼핑",
    39: "음식점",
}


# ── 1. 앵커 100건 선정 ───────────────────────────────────────────────────────

def select_anchors():
    """content_type과 지역별로 다양하게 100건 선정."""
    conn = psycopg2.connect(**DB_CONFIG)
    cur = conn.cursor()

    # overview 50자 이상, 숙박(32)/여행코스(25) 제외
    cur.execute("""
        SELECT attr_id, content_id, title, overview, content_type_id,
               addr1, sido_code, latitude, longitude
        FROM attractions
        WHERE overview IS NOT NULL AND LENGTH(overview) > 50
          AND content_type_id NOT IN (25, 32)
        ORDER BY attr_id
    """)
    all_places = cur.fetchall()
    cur.close()
    conn.close()

    columns = ["attr_id", "content_id", "title", "overview", "content_type_id",
               "addr1", "sido_code", "latitude", "longitude"]
    places = [dict(zip(columns, row)) for row in all_places]

    # content_type별 할당 비율 (데이터 비율 반영, 쇼핑은 적게)
    allocation = {
        12: 35,  # 관광지
        39: 25,  # 음식점
        14: 15,  # 문화시설
        38: 10,  # 쇼핑
        15: 8,   # 축제 (overview 없지만 혹시)
        28: 7,   # 레포츠 (overview 없지만 혹시)
    }

    selected = []
    random.seed(42)

    for ct_id, count in allocation.items():
        ct_places = [p for p in places if p["content_type_id"] == ct_id]
        if not ct_places:
            print(f"  content_type={ct_id}: 0건 → 건너뜀")
            continue

        # 지역(sido_code) 다양성 확보를 위해 지역별로 분산 선택
        by_sido = {}
        for p in ct_places:
            sido = p["sido_code"]
            by_sido.setdefault(sido, []).append(p)

        # 라운드 로빈으로 지역별 순환 선택
        picked = []
        sido_keys = list(by_sido.keys())
        random.shuffle(sido_keys)
        idx = 0
        while len(picked) < min(count, len(ct_places)):
            sido = sido_keys[idx % len(sido_keys)]
            if by_sido[sido]:
                p = random.choice(by_sido[sido])
                by_sido[sido].remove(p)
                picked.append(p)
            else:
                sido_keys.remove(sido)
                if not sido_keys:
                    break
            idx += 1

        selected.extend(picked)
        print(f"  content_type={ct_id} ({CONTENT_TYPE_NAMES.get(ct_id, '?')}): {len(picked)}건 선정")

    print(f"\n총 {len(selected)}건 앵커 선정 완료")

    # CSV 저장
    with open(ANCHOR_FILE, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=columns)
        writer.writeheader()
        writer.writerows(selected)

    print(f"저장: {ANCHOR_FILE}")
    return selected


# ── 2. Gemini 태깅 ───────────────────────────────────────────────────────────

def call_gemini(title, content_type_id, addr, overview):
    """gemini-2.5-pro로 분위기 10차원 태깅."""
    dim_desc = "\n".join(f"- **{k}**: {v}" for k, v in MOOD_DIMS.items())
    prompt = TAGGING_PROMPT.format(
        dim_descriptions=dim_desc,
        title=title,
        content_type=CONTENT_TYPE_NAMES.get(content_type_id, "기타"),
        addr=addr or "",
        overview=overview[:3000],  # 토큰 절약
    )

    body = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "temperature": 0.1,
            "maxOutputTokens": 4096,  # thinking 모델이라 사고 토큰 + 응답 토큰 필요
        },
    }

    # 최대 3회 재시도 (429 rate limit 대비)
    for attempt in range(3):
        resp = requests.post(
            GEMINI_URL,
            headers={
                "Content-Type": "application/json",
                "x-goog-api-key": GMS_KEY,
            },
            json=body,
            timeout=120,
        )

        if resp.status_code == 429:
            wait = 10 * (attempt + 1)
            print(f"[429 rate limit, {wait}s 대기]", end=" ", flush=True)
            time.sleep(wait)
            continue

        if resp.status_code != 200:
            raise Exception(f"API error {resp.status_code}: {resp.text[:200]}")
        break
    else:
        raise Exception("3회 재시도 실패 (429)")

    data = resp.json()
    candidate = data["candidates"][0]

    # finishReason 확인
    if candidate.get("finishReason") == "MAX_TOKENS":
        raise Exception("MAX_TOKENS - 응답 잘림")

    content = candidate.get("content", {})
    parts = content.get("parts", [])
    if not parts:
        raise Exception(f"응답에 parts 없음: finishReason={candidate.get('finishReason')}")

    text = parts[0].get("text", "")

    # JSON 추출
    json_match = re.search(r'\{[^{}]*\}', text, re.DOTALL)
    if not json_match:
        raise Exception(f"JSON 파싱 실패: {text[:200]}")

    scores = json.loads(json_match.group())

    # 검증: 10개 차원 모두 있는지, 값이 0.0~1.0인지
    for dim in MOOD_DIMS:
        if dim not in scores:
            scores[dim] = 0.0
        scores[dim] = max(0.0, min(1.0, float(scores[dim])))

    return scores


def tag_anchors():
    """앵커 CSV를 읽고 gemini-2.5-pro로 태깅."""
    if not os.path.exists(ANCHOR_FILE):
        print(f"앵커 파일 없음: {ANCHOR_FILE}")
        print("먼저 'py -3 anchor_tagging.py select' 실행")
        return

    # 앵커 읽기
    with open(ANCHOR_FILE, "r", encoding="utf-8-sig") as f:
        anchors = list(csv.DictReader(f))

    print(f"앵커 {len(anchors)}건 로드")

    # 이미 태깅된 결과가 있으면 이어서 진행
    tagged = {}
    if os.path.exists(TAGGED_FILE):
        with open(TAGGED_FILE, "r", encoding="utf-8-sig") as f:
            for row in csv.DictReader(f):
                tagged[row["attr_id"]] = row
        print(f"기존 태깅 {len(tagged)}건 로드 (이어서 진행)")

    # 결과 컬럼
    result_columns = ["attr_id", "title", "content_type_id", "addr1"] + list(MOOD_DIMS.keys())

    # 태깅 시작
    success = 0
    fail = 0

    for i, anchor in enumerate(anchors):
        attr_id = anchor["attr_id"]

        # 이미 태깅된 건 건너뛰기
        if attr_id in tagged:
            success += 1
            continue

        title = anchor["title"]
        print(f"[{i+1}/{len(anchors)}] {title} (attr_id={attr_id})...", end=" ", flush=True)

        try:
            scores = call_gemini(
                title=title,
                content_type_id=int(anchor["content_type_id"]),
                addr=anchor.get("addr1", ""),
                overview=anchor["overview"],
            )

            row = {
                "attr_id": attr_id,
                "title": title,
                "content_type_id": anchor["content_type_id"],
                "addr1": anchor.get("addr1", ""),
            }
            row.update(scores)
            tagged[attr_id] = row
            success += 1

            score_str = " ".join(f"{k}={v:.1f}" for k, v in scores.items() if v > 0.2)
            print(f"OK → {score_str}")

            # 매 10건마다 중간 저장
            if success % 10 == 0:
                _save_tagged(tagged, result_columns)

            # API rate limit 대비 (gemini-2.5-pro는 느리므로 여유있게)
            time.sleep(3)

        except Exception as e:
            fail += 1
            print(f"FAIL → {e}")
            time.sleep(10)

    # 최종 저장
    _save_tagged(tagged, result_columns)
    print(f"\n완료: 성공 {success}건, 실패 {fail}건")
    print(f"저장: {TAGGED_FILE}")


def _save_tagged(tagged, columns):
    """태깅 결과를 CSV로 저장."""
    with open(TAGGED_FILE, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=columns)
        writer.writeheader()
        for row in tagged.values():
            writer.writerow(row)


def show_status():
    """태깅 진행 상황 표시."""
    if not os.path.exists(ANCHOR_FILE):
        print("앵커 파일 없음. 먼저 select 실행")
        return

    with open(ANCHOR_FILE, "r", encoding="utf-8-sig") as f:
        total = sum(1 for _ in csv.DictReader(f))

    tagged_count = 0
    if os.path.exists(TAGGED_FILE):
        with open(TAGGED_FILE, "r", encoding="utf-8-sig") as f:
            tagged_count = sum(1 for _ in csv.DictReader(f))

    print(f"앵커: {total}건")
    print(f"태깅 완료: {tagged_count}건")
    print(f"남은: {total - tagged_count}건")
    print(f"예상 Credit: {(total - tagged_count) * 30}")


# ── main ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("사용법:")
        print("  py -3 anchor_tagging.py select   # 앵커 100건 선정")
        print("  py -3 anchor_tagging.py tag       # gemini-2.5-pro 태깅")
        print("  py -3 anchor_tagging.py status    # 진행 상황 확인")
        sys.exit(1)

    cmd = sys.argv[1]

    if cmd == "select":
        select_anchors()
    elif cmd == "tag":
        tag_anchors()
    elif cmd == "status":
        show_status()
    else:
        print(f"알 수 없는 명령: {cmd}")
