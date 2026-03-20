# Oh! My Guide — AI Server API 명세

AI Server Base URL: `http://[AI서버주소]:8000`

---

## 전체 흐름 요약

```
[Flow 1] 최초 추천
  Frontend → Backend: 사용자 프로필 + 위치 + 카테고리
  Backend  → AI Server: POST /api/ai/recommend
  AI Server: 반경 내 장소 조회 → 코사인 유사도 Top20 → LLM 리랭킹 Top5
  AI Server → Backend: 추천 5개 (+ 추천 이유)
  Backend  → Frontend: 화면 표시

[Flow 2] Go / Skip 피드백
  Frontend → Backend: place_id + action
  Backend: 사용자 벡터 업데이트 (Go +0.15 / Skip -0.08) → DB 저장 (AI Server 미경유)

[Flow 3] 재추천 (Find other places)
  Frontend: Step 1 Focus 선택 + Step 2 Vibe 선택 or 기타(자유입력)
  Frontend → Backend: 선택 결과 + 자유입력 텍스트
  Backend  → AI Server: POST /api/ai/refine
  AI Server: 선택지 → 벡터 반영 + 자유입력 → LLM 벡터화 → 기존 장소 제외 후 Top5 추천
  AI Server → Backend: 추천 5개 (+ 추천 이유)
  Backend  → Frontend: 화면 표시
```

> **중복 장소 제외**: AI Server가 담당. Backend가 `excluded_attr_ids`를 전달하면 AI Server가 DB 조회 시 필터링.

---

## API 목록

| 엔드포인트 | 메서드 | 구현 상태 |
|-----------|--------|---------|
| `/health` | GET | ✅ 완료 |
| `/api/ai/recommend` | POST | ✅ 완료 (코사인 유사도 + LLM 리랭킹 + 날씨/시간 맥락) |
| `/api/ai/refine` | POST | ✅ 완료 (선택지 + 자유입력 벡터 학습 + 재추천) |

---

## 공통 Response Header

| 상태 코드 | 의미 | 발생 상황 |
|----------|------|---------|
| `200 OK` | 성공 | 정상 처리 완료 |
| `422 Unprocessable Entity` | 요청 형식 오류 | 필수 필드 누락, 타입 불일치 등 |
| `500 Internal Server Error` | 서버 오류 | DB 연결 실패, 예상치 못한 예외 등 |

> Content-Type: `application/json`

---

## 1. POST /api/ai/recommend

**역할**: 사용자 위치 + 프로필 기반으로 반경 내 장소를 코사인 유사도로 Top20 선정 후 LLM(gpt-5-mini)으로 최종 5개 추천.

### Request Body

```json
{
  "user_id": 1,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "content_type_ids": [12, 14],
  "radius_km": 10.0,
  "user_profile": {
    "companion": "couple",
    "age": 28,
    "gender": "M",
    "language": "en",
    "country": "US"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `user_id` | int | ✅ | 사용자 ID |
| `latitude` | float | ✅ | 현재 위도 |
| `longitude` | float | ✅ | 현재 경도 |
| `content_type_ids` | int[] | ✅ | 카테고리 필터 (12=관광지, 14=문화시설, 15=축제, 28=레포츠, 32=숙박, 38=쇼핑, 39=음식점) |
| `radius_km` | float | ❌ | 검색 반경 km (기본 10.0, 범위 0.1~50.0) |
| `user_profile` | object | ❌ | 신규 사용자 cold-start용. 없으면 0.0 기본 벡터 사용 |
| `user_profile.companion` | string | ❌ | `couple` / `family` / `friends` / `solo` |
| `user_profile.age` | int | ❌ | 나이 |
| `user_profile.gender` | string | ❌ | `M` / `F` |
| `user_profile.language` | string | ❌ | `ko` / `en` / `zh` / `ja` |
| `user_profile.country` | string | ❌ | 국가 코드 (예: `KR`, `US`) |

### Response

**`200 OK`**

```json
{
  "recommendations": [
    {
      "attr_id": 123,
      "content_id": 2761234,
      "title": "경복궁",
      "addr1": "서울특별시 종로구 사직로 161",
      "latitude": 37.5796,
      "longitude": 126.9770,
      "content_type_id": 12,
      "first_image1": "https://tong.visitkorea.or.kr/...",
      "distance_km": 1.234,
      "similarity_score": 0.8521,
      "reason": "역사적 유적지로 커플 데이트에 어울리는 야경 명소입니다."
    }
  ],
  "cold_start": false
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `recommendations` | object[] | 추천 장소 목록 (최대 5개) |
| `recommendations[].attr_id` | int | 장소 고유 ID (attractions.attr_id) |
| `recommendations[].content_id` | int \| null | TourAPI content_id |
| `recommendations[].title` | string \| null | 장소명 |
| `recommendations[].addr1` | string \| null | 도로명 주소 |
| `recommendations[].latitude` | float | 장소 위도 |
| `recommendations[].longitude` | float | 장소 경도 |
| `recommendations[].content_type_id` | int \| null | 카테고리 ID (12/14/15/28/32/38/39) |
| `recommendations[].first_image1` | string \| null | 대표 이미지 URL. 없으면 null |
| `recommendations[].distance_km` | float | 사용자 현재 위치로부터의 거리 (km, 소수점 3자리) |
| `recommendations[].similarity_score` | float | 코사인 유사도 (0.0~1.0) |
| `recommendations[].reason` | string \| null | LLM이 생성한 추천 이유. REASON_ENABLED=false이면 null |
| `cold_start` | bool | `true` = DB에 사용자 벡터 없어서 프로필 기반 임시 벡터로 처리 |

### 동작 케이스

| 상황 | 동작 |
|------|------|
| 사용자 벡터 있음 | 코사인 유사도 → LLM 리랭킹 |
| 벡터 없음 + user_profile 있음 | 프로필로 초기 벡터 생성 후 DB 저장 → 코사인 유사도 → LLM 리랭킹 |
| 벡터 없음 + user_profile 없음 | 0.0 기본 벡터 DB 저장 → 거리순 반환 (similarity_score=0.0) |

### LLM 리랭킹 맥락 (자동 수집)

AI Server가 리랭킹 시 자동으로 다음 맥락을 수집하여 LLM에 전달:
- **날씨**: Open-Meteo API로 현재 기온, 날씨 상태(맑음/비/눈 등), 풍속 조회
- **시간**: KST 기준 현재 시각 + 시간대 (아침/점심/오후/저녁/밤)
- **장소 설명**: DB의 overview 텍스트 (있는 경우)
- **사용자 맥락**: user_profile의 동행유형, 나이, 언어

---

## 2. POST /api/ai/refine

**역할**: 재추천. 사용자의 추가 선택(Focus + Vibe or 기타 자유입력)을 벡터에 반영하고, 이미 본 장소 제외 후 새로운 5개 추천.

### Request Body

```json
{
  "user_id": 1,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "content_type_ids": [12, 14, 39],
  "radius_km": 10.0,
  "excluded_attr_ids": [123, 456, 789, 101, 202],
  "refine_choices": ["gourmet", "food", "cafe"],
  "refine_text": "비 오는 날이라 실내에서 따뜻한 음식 먹고 싶어요",
  "user_profile": {
    "companion": "couple",
    "age": 28,
    "language": "en"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `user_id` | int | ✅ | 사용자 ID |
| `latitude` | float | ✅ | 현재 위도 |
| `longitude` | float | ✅ | 현재 경도 |
| `content_type_ids` | int[] | ✅ | 카테고리 필터 |
| `radius_km` | float | ❌ | 검색 반경 km (기본 10.0) |
| `excluded_attr_ids` | int[] | ❌ | 이미 추천된 장소 attr_id 목록. 재추천 시 제외 |
| `refine_choices` | string[] | ❌ | 선택지 차원명 목록. 해당 차원 직접 상향 (학습률 0.20) |
| `refine_text` | string | ❌ | 기타 자유 입력 텍스트. LLM으로 벡터화 후 반영 (학습률 0.25) |
| `user_profile` | object | ❌ | 리랭킹 맥락용 사용자 메타데이터 |

> `refine_text`와 `refine_choices` 중 하나 이상 필요. 둘 다 오면 모두 반영.

### 모바일 "Find other places" 선택지 → refine 매핑

모바일 앱의 재추천 플로우는 2단계 선택 + 기타 입력으로 구성:

**Step 1: Focus 선택 (1개)**

| 모바일 선택지 | `refine_choices` 값 |
|-------------|-------------------|
| Local Food & Cafe | `["gourmet", "food", "cafe"]` |
| Photo Spots | `["aesthetic", "romantic"]` |
| Shopping & Trends | `["shopping", "nightlife"]` |
| Nature & Healing | `["mood_nature", "healing", "outdoor"]` |
| Culture & History | `["learning", "heritage", "culture"]` |
| Activity & Sports | `["active", "activity", "outdoor"]` |

**Step 2: Vibe 선택 (1개) 또는 기타 자유입력**

| 모바일 선택지 | `refine_choices`에 추가 |
|-------------|----------------------|
| Active & Bustling | `["active", "nightlife"]` |
| Calm & Healing | `["healing", "indoor"]` |
| Romantic & Mood | `["romantic", "aesthetic"]` |
| Nightlife | `["nightlife"]` |
| 기타 (자유입력) | `refine_text`로 전달 |

**예시: Focus "Local Food & Cafe" + Vibe "Calm & Healing"**
```json
{
  "refine_choices": ["gourmet", "food", "cafe", "healing", "indoor"]
}
```

**예시: Focus "Photo Spots" + 기타 "바다가 보이는 루프탑 카페"**
```json
{
  "refine_choices": ["aesthetic", "romantic"],
  "refine_text": "바다가 보이는 루프탑 카페"
}
```

### Response

**`200 OK`** — `/api/ai/recommend`와 동일한 형식

```json
{
  "recommendations": [
    {
      "attr_id": 999,
      "content_id": 1234567,
      "title": "국립중앙박물관",
      "addr1": "서울특별시 용산구 서빙고로 137",
      "latitude": 37.5238,
      "longitude": 126.9801,
      "content_type_id": 14,
      "first_image1": "https://tong.visitkorea.or.kr/...",
      "distance_km": 3.456,
      "similarity_score": 0.9012,
      "reason": "실내에서 조용히 즐길 수 있는 역사·문화 공간입니다."
    }
  ],
  "cold_start": false
}
```

### 내부 처리 순서

```
① 사용자 벡터 DB 조회
② refine_choices → 해당 차원 직접 상향 (학습률 0.20)
③ refine_text → LLM(gpt-4o-mini) 벡터화 → 사용자 벡터에 반영 (학습률 0.25)
④ 업데이트된 벡터 DB 저장
⑤ excluded_attr_ids 제외 + 반경 내 장소 조회 (PostGIS)
⑥ 코사인 유사도 → 상위 20개
⑦ LLM 리랭킹(gpt-5-mini) → 최종 5개 (날씨+시간+overview+사용자맥락)
⑧ 응답 반환
```

---

## 학습률 설계 의도 (Backend 참고용)

Go/Skip 벡터 업데이트는 Backend가 직접 처리. 아래 학습률 기준 참고.

| 피드백 종류 | 학습률 | 이유 |
|------------|--------|------|
| Go | 0.15 | 방문 의사 표현 |
| Skip | 0.08 | "지금은 아니지만 나중엔 갈 수도" |
| 선택지 선택 (refine) | 0.20 | 명시적 취향 표현 |
| 자유 텍스트 입력 (refine) | 0.25 | 가장 확실한 취향 표현 |

---

## 24차원 벡터 차원명 목록 (refine_choices 참조용)

### 카테고리 8개
| 차원명 | 설명 | content_type_id |
|--------|------|-----------------|
| `nature` | 관광지(자연) | 12 |
| `culture` | 문화시설 | 14 |
| `festival` | 축제공연행사 | 15 |
| `activity` | 레포츠 | 28 |
| `shopping` | 쇼핑 | 38 |
| `food` | 음식점 | 39 |
| `cafe` | 카페 | 39 (서브카테고리) |
| `lodging` | 숙박 | 32 |

### 분위기 10개
| 차원명 | 설명 |
|--------|------|
| `healing` | 힐링/휴식/산책 |
| `aesthetic` | 감성/분위기/포토 |
| `gourmet` | 미식/맛집 |
| `learning` | 학습/문화/공연 |
| `heritage` | 역사/유적/전통 |
| `mood_nature` | 자연경관/산/바다 |
| `romantic` | 연인/데이트 |
| `family` | 가족/어린이 |
| `active` | 스포츠/액티비티 |
| `nightlife` | 야간/축제/밤문화 |

### 실용 6개
| 차원명 | 설명 |
|--------|------|
| `free_entry` | 무료 입장 |
| `parking_available` | 주차 가능 |
| `pet_friendly` | 반려동물 동반 |
| `baby_friendly` | 유아 동반 |
| `indoor` | 실내 |
| `outdoor` | 야외 |
