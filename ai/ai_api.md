# Oh! My Guide — AI Server API 명세

AI Server Base URL: `http://[AI서버주소]:8000`

---

## 전체 흐름 요약

```
[Flow 1] 최초 추천
  Frontend → Backend: 사용자 프로필 + 위치 + 카테고리
  Backend  → AI Server: POST /api/ai/recommend
  AI Server: 반경 내 장소 조회 → 코사인 유사도 Top20 → LLM 리랭킹 Top5
  AI Server → Backend: 추천 5개 + 추천 이유
  Backend  → Frontend: 화면 표시

[Flow 2] Go / Skip 피드백
  Frontend → Backend: place_id + action
  Backend: 사용자 벡터 업데이트 (Go +0.15 / Skip -0.08) → DB 저장 (AI Server 미경유)

[Flow 3] 재추천
  Frontend → Backend: 설문 결과 (선택지 or 자유입력)
  Backend  → AI Server: POST /api/ai/refine
  AI Server: LLM으로 설문 결과 벡터화 → 사용자 벡터 업데이트 → 기존 본 장소 제외 후 Top5 추천
  AI Server → Backend: 추천 5개 + 추천 이유
  Backend  → Frontend: 화면 표시
```

> **중복 장소 제외**: AI Server가 담당. Backend가 `excluded_attr_ids`를 전달하면 AI Server가 DB 조회 시 필터링.

---

## API 목록

| 엔드포인트 | 메서드 | 구현 상태 |
|-----------|--------|---------|
| `/health` | GET | ✅ 완료 |
| `/api/ai/recommend` | POST | ⚠️ 부분 완료 (Top20 반환, LLM 리랭킹 미구현) |
| `/api/ai/refine` | POST | ❌ 미구현 |

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

**역할**: 사용자 위치 + 프로필 기반으로 반경 내 장소를 코사인 유사도로 Top20 선정 후 LLM으로 최종 5개 추천.

### Request Header

| 항목 | 값 |
|------|-----|
| Content-Type | `application/json` |

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
    "language": "ko",
    "country": "KR"
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
| `recommendations` | object[] | 추천 장소 목록. 최대 5개 (LLM 리랭킹 구현 후) / 현재 최대 20개 |
| `recommendations[].attr_id` | int | 장소 고유 ID (attractions.attr_id) |
| `recommendations[].content_id` | int \| null | TourAPI content_id. 외부 API 연동 시 사용 |
| `recommendations[].title` | string \| null | 장소명 |
| `recommendations[].addr1` | string \| null | 도로명 주소 |
| `recommendations[].latitude` | float | 장소 위도 |
| `recommendations[].longitude` | float | 장소 경도 |
| `recommendations[].content_type_id` | int \| null | 카테고리 ID (12/14/15/28/32/38/39) |
| `recommendations[].first_image1` | string \| null | 대표 이미지 URL. 없으면 null |
| `recommendations[].distance_km` | float | 사용자 현재 위치로부터의 거리 (km, 소수점 3자리) |
| `recommendations[].similarity_score` | float | 코사인 유사도 (0.0~1.0). cold-start 거리순 폴백 시 0.0 |
| `recommendations[].reason` | string | LLM이 생성한 추천 이유 (LLM 리랭킹 구현 후 추가 예정) |
| `cold_start` | bool | `true` = DB에 사용자 벡터 없어서 프로필 기반 임시 벡터로 처리 |

**`422 Unprocessable Entity`** — 필수 필드 누락 또는 타입 오류 시

```json
{
  "detail": [
    {
      "type": "missing",
      "loc": ["body", "user_id"],
      "msg": "Field required"
    }
  ]
}
```

### 동작 케이스

| 상황 | 동작 |
|------|------|
| 사용자 벡터 있음 | 코사인 유사도 → LLM 리랭킹 |
| 벡터 없음 + user_profile 있음 | 프로필로 초기 벡터 생성 후 DB 저장 → 코사인 유사도 → LLM 리랭킹 |
| 벡터 없음 + user_profile 없음 | 0.0 기본 벡터 DB 저장 → 거리순 반환 (similarity_score=0.0) |

---

## 2. POST /api/ai/refine

**역할**: 재추천. 사용자의 추가 설문(선택지 or 자유입력)을 LLM으로 벡터화하고, 이미 본 장소 제외 후 새로운 5개 추천.

> ❌ **미구현**

### Request Header

| 항목 | 값 |
|------|-----|
| Content-Type | `application/json` |

### Request Body

```json
{
  "user_id": 1,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "content_type_ids": [12, 14],
  "radius_km": 10.0,
  "excluded_attr_ids": [123, 456, 789],
  "refine_text": "실내에서 조용히 쉴 수 있는 곳이 좋겠어요",
  "refine_choices": ["healing", "indoor"]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `user_id` | int | ✅ | 사용자 ID |
| `latitude` | float | ✅ | 현재 위도 |
| `longitude` | float | ✅ | 현재 경도 |
| `content_type_ids` | int[] | ✅ | 카테고리 필터 |
| `radius_km` | float | ❌ | 검색 반경 km (기본 10.0) |
| `excluded_attr_ids` | int[] | ❌ | 이미 추천된 장소 attr_id 목록. 해당 장소 재추천 방지 |
| `refine_text` | string | ❌ | 사용자 자유 입력 텍스트. LLM으로 벡터화 후 반영 (학습률 0.25) |
| `refine_choices` | string[] | ❌ | 선택지 차원명 목록 (예: `["healing", "indoor"]`). 해당 차원 직접 상향 (학습률 0.20) |

> `refine_text`와 `refine_choices` 중 하나 이상 필요. 둘 다 오면 모두 반영.

### Response

**`200 OK`**

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

| 필드 | 타입 | 설명 |
|------|------|------|
| `recommendations` | object[] | 재추천 장소 목록. 최대 5개. `excluded_attr_ids`에 포함된 장소 미포함 |
| `recommendations[].attr_id` | int | 장소 고유 ID (attractions.attr_id) |
| `recommendations[].content_id` | int \| null | TourAPI content_id |
| `recommendations[].title` | string \| null | 장소명 |
| `recommendations[].addr1` | string \| null | 도로명 주소 |
| `recommendations[].latitude` | float | 장소 위도 |
| `recommendations[].longitude` | float | 장소 경도 |
| `recommendations[].content_type_id` | int \| null | 카테고리 ID |
| `recommendations[].first_image1` | string \| null | 대표 이미지 URL. 없으면 null |
| `recommendations[].distance_km` | float | 사용자 현재 위치로부터의 거리 (km, 소수점 3자리) |
| `recommendations[].similarity_score` | float | 업데이트된 사용자 벡터 기준 코사인 유사도 (0.0~1.0) |
| `recommendations[].reason` | string | LLM이 생성한 추천 이유 |
| `cold_start` | bool | 항상 `false` (refine은 기존 벡터 존재 시에만 호출) |

**`422 Unprocessable Entity`** — 필수 필드 누락 또는 타입 오류 시

```json
{
  "detail": [
    {
      "type": "missing",
      "loc": ["body", "user_id"],
      "msg": "Field required"
    }
  ]
}
```

### 내부 로직

1. `refine_text` → GMS LLM 호출하여 24차원 벡터 델타 추출 → 사용자 벡터에 반영 (학습률 0.25)
2. `refine_choices` → 해당 차원 직접 상향 조정 (학습률 0.20)
3. 업데이트된 벡터 DB 저장
4. `excluded_attr_ids` 제외한 반경 내 장소 조회 → 코사인 유사도 Top20 → LLM 리랭킹 Top5

---

## 학습률 설계 의도 (Backend 참고용)

Go/Skip 벡터 업데이트는 Backend가 직접 처리. 아래 학습률 기준 참고.

| 피드백 종류 | 학습률 | 이유 |
|------------|--------|------|
| Go | 0.15 | 방문 의사 표현 |
| Skip | 0.08 | "지금은 아니지만 나중엔 갈 수도" |
| 선택지 선택 (refine) | 0.20 | 명시적 취향 표현 |
| 자유 텍스트 입력 (refine) | 0.25 | 가장 확실한 취향 표현 |
