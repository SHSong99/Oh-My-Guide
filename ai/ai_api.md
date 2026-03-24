# Oh! My Guide — API 명세

## 전체 흐름

```
[Flow 1] 최초 추천
  Mobile → Backend: GET /api/userRecommend?category=food,nature&currentLat=37.56&currentLng=126.97
  Backend → AI Server: GET /api/userRecommend?category=food,nature&currentLat=37.56&currentLng=126.97&userId=1
  AI Server: 벡터 조회 → cold-start 처리 → 코사인 유사도 Top20 → LLM 리랭킹 Top5
  AI Server → Backend → Mobile: PlaceCard 5개

[Flow 2] Go / Skip 피드백
  Mobile → Backend: place_id + action
  Backend: 사용자 벡터 업데이트 (Go +0.15 / Skip -0.08) → DB 저장 (AI Server 미경유)

[Flow 3] 재추천 (Find other places)
  Mobile: Focus 선택 → Vibe 선택
  Mobile → Backend: POST /api/userRecommend/recommend/refresh
  Backend → AI Server: POST /api/userRecommend/recommend/refresh
  AI Server: 자연어 → LLM 벡터화 → 사용자 벡터 업데이트 → 코사인 유사도 Top20 → LLM 리랭킹 Top5
  AI Server → Backend → Mobile: PlaceCard 5개
```

---

## API 목록

| 엔드포인트 | 메서드 | 설명 | 구현 상태 |
|-----------|--------|------|---------|
| `/health` | GET | 헬스체크 | ✅ |
| `/api/userRecommend` | GET | 초기 추천 (카테고리 선택 후 첫 추천) | ✅ |
| `/api/userRecommend/recommend/refresh` | POST | 재추천 (Find other places) | ✅ |

> 기존 `/api/ai/recommend`, `/api/ai/refine`은 내부용으로 유지. 모바일/백엔드는 위 2개 API만 사용.

---

## 공통 응답 형식 (PlaceCard)

두 API 모두 동일한 응답 형식을 사용합니다.

```json
{
  "recommendations": [
    {
      "attr_id": 123,
      "name": "해운대해수욕장",
      "name_kr": "해운대해수욕장",
      "image_url": "https://tong.visitkorea.or.kr/...",
      "distance": "1.2km",
      "tag": "Nature",
      "latitude": 35.1587,
      "longitude": 129.1604
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `recommendations` | object[] | 추천 장소 목록 (최대 5개) |
| `attr_id` | int | 장소 고유 ID |
| `name` | string | 장소명 |
| `name_kr` | string | 한국어 장소명 |
| `image_url` | string \| null | 대표 이미지 URL |
| `distance` | string | 거리 문자열 (예: `"350m"`, `"1.2km"`) |
| `tag` | string | 카테고리 태그: `Nature`, `Culture`, `Festival`, `Activity`, `Lodging`, `Shopping`, `Food` |
| `latitude` | float | 장소 위도 |
| `longitude` | float | 장소 경도 |

### 공통 상태 코드

| 상태 코드 | 의미 |
|----------|------|
| `200 OK` | 성공 (결과 0건이어도 200, 빈 배열 반환) |
| `422 Unprocessable Entity` | 요청 형식 오류 |
| `500 Internal Server Error` | DB 연결 실패 등 서버 오류 |

---

## 1. GET /api/userRecommend

**역할**: 온보딩 카테고리 선택 후 첫 추천. 사용자 위치 + 선택 카테고리 기반 5개 장소 추천.

### Request (Query Parameters)

```
GET /api/userRecommend?category=food,nature&currentLat=37.5665&currentLng=126.978
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `category` | string | ❌ | 쉼표 구분 카테고리 (없으면 전체). 값: `attraction`, `culture`, `festival`, `course`, `leports`, `cafe`, `shopping`, `food` |
| `currentLat` | float | ✅ | 현재 위도 (기본 37.5665) |
| `currentLng` | float | ✅ | 현재 경도 (기본 126.978) |
| `userId` | int | ❌ | 사용자 ID (백엔드가 JWT에서 추출하여 전달) |
| `radiusKm` | float | ❌ | 검색 반경 km (기본 10.0) |

### 모바일 카테고리 → content_type_id 매핑

| 모바일 카테고리 | content_type_id |
|---------------|-----------------|
| `attraction` | 12 (관광지) |
| `culture` | 14 (문화시설) |
| `festival` | 15 (축제) |
| `course` | 12, 14 |
| `leports` | 28 (레포츠) |
| `cafe` | 39 (음식점) |
| `shopping` | 38 (쇼핑) |
| `food` | 39 (음식점) |

### Response

공통 응답 형식 참조.

### 내부 처리 순서

```
① 사용자 벡터 DB 조회
② cold-start인 경우 → 선택 카테고리 기반 초기 벡터 생성 후 DB 저장
③ 카테고리 문자열 → content_type_ids 매핑
④ 반경 내 장소 + 벡터 조회 (PostGIS)
⑤ 코사인 유사도 → 상위 20개
⑥ LLM 리랭킹(gpt-5-mini) → 최종 5개 (날씨 + 시간 맥락 자동 수집)
⑦ PlaceCard 형식으로 변환 후 응답
```

---

## 2. POST /api/userRecommend/recommend/refresh

**역할**: 재추천. 사용자가 GO 없이 "Find other places" 버튼 클릭 시 카테고리/분위기/자유텍스트를 자연어로 받아 새로운 5개 추천.

### Request Body

```json
{
  "user_id": 1,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "radius_km": 10.0,
  "category": "Local Food & Cafe",
  "mood": "Calm & Healing",
  "free_text": "바다가 보이는 실내 카페",
  "excluded_attr_ids": [123, 456]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `user_id` | int | ✅ | 사용자 ID (백엔드가 JWT에서 추출하여 전달) |
| `latitude` | float | ✅ | 현재 위도 |
| `longitude` | float | ✅ | 현재 경도 |
| `radius_km` | float | ❌ | 검색 반경 km (기본 10.0) |
| `category` | string | ❌ | 카테고리 자연어 입력 (Focus 선택값) |
| `mood` | string | ❌ | 분위기 자연어 입력 (Vibe 선택값) |
| `free_text` | string | ❌ | 기타 자유 텍스트 |
| `excluded_attr_ids` | int[] | ❌ | 이미 추천된 장소 ID 목록 (중복 제외) |

### 모바일 "Find other places" 선택지

**Step 1: Focus 선택** — `category` 필드로 전달

| 모바일 선택지 | category 값 |
|-------------|------------|
| Local Food & Cafe | `"Local Food & Cafe"` |
| Photo Spots | `"Photo Spots"` |
| Shopping & Trends | `"Shopping & Trends"` |

**Step 2: Vibe 선택** — `mood` 필드로 전달

| 모바일 선택지 | mood 값 |
|-------------|---------|
| Active & Bustling | `"Active & Bustling"` |
| Calm & Healing | `"Calm & Healing"` |
| Nightlife | `"Nightlife"` |

> category, mood, free_text 모두 자연어로 전달. AI 서버가 LLM으로 벡터화하여 사용자 벡터에 반영 (학습률 0.25).

### Response

공통 응답 형식 참조.

### 내부 처리 순서

```
① 사용자 벡터 DB 조회
② category + mood + free_text → 하나로 합쳐서 LLM(gpt-4o-mini) 벡터화
③ 사용자 벡터에 반영 (학습률 0.25) → DB 저장
④ category 자연어 → content_type_ids 자동 매핑
⑤ excluded_attr_ids 제외 + 반경 내 장소 조회 (PostGIS)
⑥ 코사인 유사도 → 상위 20개
⑦ LLM 리랭킹(gpt-5-mini) → 최종 5개 (날씨 + 시간 맥락 자동 수집)
⑧ PlaceCard 형식으로 변환 후 응답
```

---

## Backend 호출 경로

모바일은 백엔드를 경유하여 AI 서버에 접근합니다.

```
[초기 추천]
Mobile: GET /api/userRecommend?category=food&currentLat=37.56&currentLng=126.97
  → Backend (JWT에서 userId 추출)
  → AI Server: GET /api/userRecommend?category=food&currentLat=37.56&currentLng=126.97&userId=1

[재추천]
Mobile: POST /api/userRecommend/recommend/refresh (body: {latitude, longitude, category, mood, ...})
  → Backend (JWT에서 userId 추출, body에 user_id 추가)
  → AI Server: POST /api/userRecommend/recommend/refresh
```

> 모바일 요청 헤더: `Authorization: Bearer {JWT토큰}`

---

## LLM 리랭킹 맥락 (자동 수집)

두 API 모두 리랭킹 시 자동으로 다음 맥락을 수집하여 LLM에 전달:

- **날씨**: Open-Meteo API — 기온, 날씨 상태(맑음/비/눈), 풍속
- **시간**: KST 기준 현재 시각 + 시간대 (아침/점심/오후/저녁/밤)
- **장소 설명**: DB의 overview 텍스트

---

## 학습률 설계 (Backend 참고용)

Go/Skip 벡터 업데이트는 Backend가 직접 처리.

| 피드백 종류 | 학습률 | 이유 |
|------------|--------|------|
| Go | 0.15 | 방문 의사 표현 |
| Skip | 0.08 | "지금은 아니지만 나중엔 갈 수도" |
| 재추천 자연어 입력 | 0.25 | 가장 확실한 취향 표현 |

---

## 24차원 벡터 차원명 목록

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
