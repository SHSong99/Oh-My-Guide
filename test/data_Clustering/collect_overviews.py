"""
Oh! My Guide - TourAPI overview 데이터 수집 스크립트

[사전 준비]
1. https://www.data.go.kr 회원가입
2. "한국관광공사_국문 관광정보 서비스_GW" 검색 → 활용신청 (자동 승인)
3. 마이페이지 → 인증키 발급 현황 → 인증키 복사
4. 아래 SERVICE_KEY에 붙여넣기
5. pip install requests pandas tqdm
6. python collect_overviews.py

[수집 대상]
- contentTypeId: 12(관광지), 14(문화시설), 15(축제), 28(레포츠), 38(쇼핑), 39(음식점)
- overview가 있는 것만 저장
- 예상 3~5만건, 소요시간 약 30~60분
"""

import requests
import pandas as pd
import time
import os
from tqdm import tqdm

# ── 여기에 인증키 붙여넣기 ──
SERVICE_KEY = "db6e1d5fd6aefc819ca3cf2f480bed86fc7ac78dc9c750821ca21ecfce650226"

# ── 설정 ──
BASE_URL = "https://apis.data.go.kr/B551011/KorService2"
CONTENT_TYPES = {
    "12": "관광지",
    "14": "문화시설",
    "15": "축제공연행사",
    "28": "레포츠",
    "38": "쇼핑",
    "39": "음식점",
}
PAGE_SIZE = 50  # 한 번에 가져올 건수
OUTPUT_FILE = "tourapi_overviews.csv"


def fetch_list(content_type_id: str, page: int) -> dict:
    """지역기반 관광정보 목록 조회"""
    params = {
        "serviceKey": SERVICE_KEY,
        "MobileOS": "ETC",
        "MobileApp": "OhMyGuide",
        "_type": "json",
        "numOfRows": PAGE_SIZE,
        "pageNo": page,
        "contentTypeId": content_type_id,
        "arrange": "C",  # 수정일순
    }
    resp = requests.get(f"{BASE_URL}/areaBasedList2", params=params, timeout=30)
    resp.raise_for_status()
    return resp.json()


def fetch_detail(content_id: str) -> dict:
    """공통정보 조회 (overview 포함) - 429시 자동 재시도"""
    params = {
        "serviceKey": SERVICE_KEY,
        "MobileOS": "ETC",
        "MobileApp": "OhMyGuide",
        "_type": "json",
        "contentId": content_id,
        "numOfRows": 1,
        "pageNo": 1,
    }
    for attempt in range(5):
        resp = requests.get(f"{BASE_URL}/detailCommon2", params=params, timeout=30)
        if resp.status_code == 429:
            wait = 5 * (attempt + 1)
            print(f"\n  429 Rate Limit - {wait}초 대기 후 재시도 ({attempt+1}/5)")
            time.sleep(wait)
            continue
        resp.raise_for_status()
        return resp.json()
    raise Exception("429 재시도 초과")


def get_total_count(content_type_id: str) -> int:
    """해당 타입의 전체 건수 조회"""
    data = fetch_list(content_type_id, 1)
    try:
        return int(data["response"]["body"]["totalCount"])
    except (KeyError, TypeError):
        return 0


def collect_all():
    """전체 수집 실행"""
    all_rows = []

    # 이어하기 지원: 기존 파일이 있으면 로드
    existing_ids = set()
    if os.path.exists(OUTPUT_FILE):
        existing = pd.read_csv(OUTPUT_FILE)
        existing_ids = set(existing["content_id"].astype(str))
        all_rows = existing.to_dict("records")
        print(f"기존 {len(existing_ids)}건 로드됨. 이어서 수집합니다.")

    for ctype, cname in CONTENT_TYPES.items():
        total = get_total_count(ctype)
        total_pages = (total // PAGE_SIZE) + 1
        print(f"\n{'='*50}")
        print(f"[{cname}] (contentTypeId={ctype}) 전체 {total}건, {total_pages}페이지")
        print(f"{'='*50}")

        for page in tqdm(range(1, total_pages + 1), desc=f"{cname}"):
            try:
                data = fetch_list(ctype, page)
                items = data.get("response", {}).get("body", {}).get("items", {}).get("item", [])

                if not items:
                    continue
                if isinstance(items, dict):  # 1건일 때 dict로 옴
                    items = [items]

                for item in items:
                    cid = str(item.get("contentid", ""))
                    if not cid or cid in existing_ids:
                        continue

                    # 상세 조회 (overview 가져오기)
                    try:
                        detail = fetch_detail(cid)
                        detail_item = detail["response"]["body"]["items"]["item"]
                        if isinstance(detail_item, list):
                            detail_item = detail_item[0]

                        overview = detail_item.get("overview", "")
                        if not overview or len(overview.strip()) < 20:
                            continue  # overview 없거나 너무 짧으면 스킵

                        row = {
                            "content_id": cid,
                            "content_type_id": ctype,
                            "content_type_name": cname,
                            "title": item.get("title", ""),
                            "addr1": item.get("addr1", ""),
                            "mapx": item.get("mapx", ""),
                            "mapy": item.get("mapy", ""),
                            "lcls_systm1": detail_item.get("lclsSystm1", ""),
                            "lcls_systm2": detail_item.get("lclsSystm2", ""),
                            "lcls_systm3": detail_item.get("lclsSystm3", ""),
                            "overview": overview,
                        }
                        all_rows.append(row)
                        existing_ids.add(cid)

                    except Exception as e:
                        print(f"\n  상세 조회 실패 (contentId={cid}): {e}")
                        continue

                    # API 부하 방지
                    time.sleep(1.0)

            except Exception as e:
                print(f"\n  목록 조회 실패 (page={page}): {e}")
                time.sleep(1)
                continue

            # 페이지마다 중간 저장
            if all_rows:
                df = pd.DataFrame(all_rows)
                df.to_csv(OUTPUT_FILE, index=False, encoding="utf-8-sig")

    # 최종 저장
    df = pd.DataFrame(all_rows)
    df.to_csv(OUTPUT_FILE, index=False, encoding="utf-8-sig")
    print(f"\n{'='*50}")
    print(f"수집 완료! 총 {len(all_rows)}건 → {OUTPUT_FILE}")
    print(f"{'='*50}")

    # 통계
    print("\n[타입별 건수]")
    for ctype, cname in CONTENT_TYPES.items():
        count = len([r for r in all_rows if r["content_type_id"] == ctype])
        print(f"  {cname}: {count}건")


if __name__ == "__main__":
    if SERVICE_KEY == "여기에_인증키_붙여넣기":
        print("❌ SERVICE_KEY를 설정해주세요!")
        print("   1. https://www.data.go.kr 가입")
        print("   2. '한국관광공사_국문 관광정보 서비스_GW' 검색 → 활용신청")
        print("   3. 마이페이지 → 인증키 복사")
        print("   4. 이 파일의 SERVICE_KEY에 붙여넣기")
    else:
        collect_all()
