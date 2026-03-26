#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
부산 관광지 overview_tts 생성 스크립트
overview 데이터를 기반으로 관광 가이드 스타일의 TTS 텍스트를 생성합니다.
"""

import random
import re

# content_type_id 매핑
TYPE_MAP = {
    "12": "관광지",
    "14": "문화시설",
    "15": "축제/행사",
    "25": "여행코스",
    "28": "레포츠",
    "32": "숙박",
    "38": "쇼핑",
    "39": "음식점",
}

# 인사말 변형
GREETINGS = [
    "안녕하세요, 반갑습니다!",
    "안녕하세요! 부산 여행, 즐기고 계신가요?",
    "안녕하세요, 부산 여행 가이드입니다!",
    "안녕하세요! 오늘도 즐거운 부산 여행 되고 계신가요?",
    "안녕하세요, 여행자 여러분! 반갑습니다!",
    "안녕하세요! 부산의 매력 속으로 함께 떠나볼까요?",
    "안녕하세요! 멋진 부산 여행의 동행이 되어드릴게요!",
    "안녕하세요, 부산에 오신 걸 환영해요!",
]

# content_type별 소개 멘트
INTRO_BY_TYPE = {
    "12": [
        "지금 소개해 드릴 곳은 부산의 멋진 관광 명소,",
        "다음으로 안내해 드릴 곳은 부산 여행에서 빠질 수 없는,",
        "이번에 만나볼 곳은 부산을 대표하는 관광지,",
        "자, 이번에는 부산의 숨은 보석 같은 곳,",
    ],
    "14": [
        "문화와 예술이 살아 숨 쉬는 곳,",
        "이번에 소개해 드릴 문화 공간은,",
        "부산의 문화를 느낄 수 있는 곳,",
        "예술과 문화가 어우러진 공간,",
    ],
    "15": [
        "신나는 축제의 현장으로 안내해 드릴게요!",
        "부산의 활기찬 축제를 소개합니다!",
        "놓치면 아쉬운 부산의 특별한 행사,",
        "부산에서만 즐길 수 있는 특별한 축제,",
    ],
    "25": [
        "부산의 매력을 한 번에 느낄 수 있는 여행 코스,",
        "알차게 부산을 즐기실 수 있는 추천 코스,",
    ],
    "28": [
        "몸도 마음도 신나는 레포츠 체험지,",
        "짜릿한 액티비티를 즐길 수 있는 곳,",
        "부산에서 즐기는 특별한 레저 체험,",
    ],
    "32": [
        "편안한 휴식을 위한 숙소를 소개해 드릴게요!",
        "여행의 피로를 풀어줄 숙소,",
        "부산 여행 중 머물기 좋은 곳,",
    ],
    "38": [
        "쇼핑을 좋아하신다면 주목해 주세요!",
        "부산에서 쇼핑을 즐기실 수 있는 곳,",
        "여행의 즐거움을 더해줄 쇼핑 명소,",
    ],
    "39": [
        "부산 하면 역시 맛집이죠!",
        "미식 여행을 위한 추천 맛집을 소개합니다!",
        "부산의 맛을 제대로 느낄 수 있는 곳,",
        "여행에서 빠질 수 없는 먹거리를 소개할게요!",
    ],
}

# 마무리 멘트
CLOSINGS = [
    "혹시 근처에 가볼 만한 곳이 더 궁금하시다면, 계속 가이드해 드릴게요!",
    "이 근처에도 추천할 만한 장소가 많답니다. 궁금하시면 계속 안내해 드릴게요!",
    "주변에 또 다른 멋진 장소들이 있으니, 원하시면 계속 가이드해 드릴게요!",
    "근처에 추천 드릴 곳이 더 있으니, 계속 함께 부산 여행을 즐겨볼까요?",
    "이 주변에도 숨은 명소들이 있답니다! 궁금하시면 계속 가이드해 드릴게요!",
]


def extract_district(addr):
    """주소에서 구/군 추출"""
    parts = addr.split()
    if len(parts) >= 2:
        return parts[1]  # e.g., "중구", "해운대구"
    return "부산"


def clean_overview(overview):
    """overview에서 HTML 태그 등 제거 및 정리"""
    text = re.sub(r'<[^>]+>', '', overview)
    text = re.sub(r'\\n', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text


def shorten_overview(text, max_len=500):
    """긴 overview를 적절히 줄임"""
    if len(text) <= max_len:
        return text
    # 문장 단위로 자르기
    sentences = re.split(r'(?<=[.!?다])\s+', text)
    result = ""
    for s in sentences:
        if len(result) + len(s) > max_len:
            break
        result += s + " "
    return result.strip() if result.strip() else text[:max_len]


def make_conversational(overview, title):
    """overview를 대화체로 변환"""
    text = clean_overview(overview)
    text = shorten_overview(text, 400)

    # ~이다 -> ~인데요, ~있다 -> ~있는데요 등 일부 변환
    # 완벽한 변환은 어려우므로 자연스러운 연결어를 추가
    return text


def generate_tts(attr_id, title, addr, overview, content_type_id):
    """가이드 TTS 텍스트 생성"""
    random.seed(int(attr_id))  # 재현 가능한 랜덤

    district = extract_district(addr)
    greeting = random.choice(GREETINGS)
    intros = INTRO_BY_TYPE.get(content_type_id, INTRO_BY_TYPE["12"])
    intro = random.choice(intros)
    closing = random.choice(CLOSINGS)

    if overview == "-" or len(overview.strip()) < 5:
        # overview가 없는 경우
        type_name = TYPE_MAP.get(content_type_id, "장소")
        body = f"{district}에 위치한 {title}입니다. 부산을 여행하시면서 한번 들러보시면 좋을 {type_name}이에요."
        tts = f"{greeting} {intro} {title}입니다! {body} {closing}"
    else:
        conv_overview = make_conversational(overview, title)
        tts = f"{greeting} {intro} {title}입니다! {district}에 위치한 이곳을 소개해 드릴게요. {conv_overview} {closing}"

    return tts


def escape_sql(text):
    """SQL 문자열 이스케이프"""
    return text.replace("'", "''").replace("\\", "\\\\")


def main():
    updates = []
    import os
    tmp_path = os.path.join(os.environ.get("TEMP", "/tmp"), "busan_all.txt")
    with open(tmp_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            parts = line.split("|||")
            if len(parts) != 5:
                continue

            attr_id, title, addr, overview, content_type_id = parts
            attr_id = attr_id.strip()
            title = title.strip()
            addr = addr.strip()
            overview = overview.strip()
            content_type_id = content_type_id.strip()

            tts = generate_tts(attr_id, title, addr, overview, content_type_id)
            escaped_tts = escape_sql(tts)

            updates.append(
                f"UPDATE attractions SET overview_tts = '{escaped_tts}' WHERE attr_id = {attr_id};"
            )

    # SQL 파일 출력
    with open("update_busan_tts.sql", "w", encoding="utf-8") as f:
        f.write("-- 부산광역시 관광지 overview_tts UPDATE 쿼리\n")
        f.write(f"-- 총 {len(updates)}개 레코드\n\n")
        for u in updates:
            f.write(u + "\n")

    print(f"총 {len(updates)}개 UPDATE 쿼리 생성 완료 -> update_busan_tts.sql")


if __name__ == "__main__":
    main()
