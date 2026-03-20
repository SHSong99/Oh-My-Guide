# Oh! My Guide - AI 추천 서버 프로젝트 컨텍스트

## 프로젝트 개요
사용자 위치 기반 실시간 관광지 AI 추천 & 가이드 서비스.
3인 팀 (SSAFY 14기), 서비스명: Oh! My Guide.
나는 AI 추천 로직 파트 담당. FastAPI 서버 개발.

---

## 서버 아키텍처

```
User → Spring Boot ← → AI Server(FastAPI)
           ↕                    ↕
        PostgreSQL ← ─ ─ ─ ─ ┘ (AI Server도 직접 접근)
           ↕
     Redis(세션/캐시)
           ↕
   HDFS + Kafka + Spark (빅데이터 배치)
```

- **Spring Boot**: 사용자 인증, 클라이언트 통신, 비즈니스 로직
- **AI Server (FastAPI)**: PostgreSQL 직접 접근, 코사인 유사도, LLM 호출, 가중치 학습, 추천 생성
- **PostgreSQL**: 장소 데이터 + 벡터 저장, 사용자 데이터
- **Redis**: 세션/캐시
- **HDFS + Kafka + Spark**: 빅데이터 배치 파이프라인
- **GMS**: SSAFY 제공 통합 LLM API (10만 Credit 제한, 모든 모델 동일 인터페이스)

### 핵심 원칙
- AI Server가 PostgreSQL에 직접 접근하여 장소 데이터 조회 + 사용자 벡터 읽기/쓰기
- Spring과 AI Server는 REST API로 통신하되, 무거운 장소 데이터는 AI Server가 DB에서 직접 읽음
- 나는 AI 폴더(ai-server/)만 담당. 백엔드 코드는 읽기만 하고 수정하지 않음

---

## DB 스키마 (확인 완료 - dump_postgresql.sql 기준)

### PostgreSQL 접속 정보
```
Host: localhost:5432
DB: ohmyguide
User: admin
Password: 1234
Docker: postgis/postgis:16-3.4
Volume: ohmyguide-postgres-data
```

### 테이블 구조 (확인 완료)

**attractions** (장소 - 50,472건)
| 컬럼 | 타입 | 비고 |
|------|------|------|
| attr_id | bigint PK | 자동 증가 |
| content_id | integer | TourAPI content_id |
| addr1, addr2 | varchar(100) | 주소 |
| first_image1, first_image2 | varchar(500) | 이미지 URL |
| gugun_code | integer | 구군 코드 |
| homepage | varchar(1000) | HTML 포함 |
| latitude, longitude | numeric(20,17) | 좌표 |
| overview | varchar(10000) | 장소 설명 (46,650건 NULL) |
| sido_code | integer | 시도 코드 |
| tel | varchar(20) | 전화번호 |
| title | varchar(500) | 장소명 |
| content_type_id | bigint | FK → contenttypes |
| created_at, updated_at | timestamp(6) | |

**attraction_vectors** (장소 벡터 - **50,472건 완료**)
| 컬럼 | 타입 | 비고 |
|------|------|------|
| attr_id | bigint PK | FK → attractions |
| attraction_vector | json | 24차원 벡터 저장 |
| created_at, updated_at | timestamp(6) | |

**user_vectors** (사용자 벡터 - 현재 비어있음)
| 컬럼 | 타입 | 비고 |
|------|------|------|
| user_id | bigint PK | FK → users |
| preference_vector | json | 24차원 벡터 저장 |
| created_at, updated_at | timestamp(6) | |

**users** (사용자)
| 컬럼 | 타입 | 비고 |
|------|------|------|
| user_id | bigint PK | 자동 증가 |
| email | varchar(100) | |
| nickname | varchar(20) | |
| profile_image_url | varchar(500) | |
| onboarding_completed | boolean | 기본 false |
| created_at, updated_at | timestamp(6) | |

**chat_messages** (채팅 이력 - 현재 비어있음)
| 컬럼 | 타입 | 비고 |
|------|------|------|
| chat_message_id | bigint PK | 자동 증가 |
| user_id | bigint | FK → users |
| role | varchar(255) | 'USER' \| 'ASSISTANT' |
| content | varchar(1000) | |
| created_at, updated_at | timestamp(6) | |

**phrases** (문구 - 현재 비어있음) / **attraction_phrases** (장소-문구 연결 - 현재 비어있음) / **user_phrases** (사용자-문구 연결 - 현재 비어있음)

**contenttypes** (장소 유형)
| content_type_id | content_type_name | 건수 |
|----------------|-------------------|------|
| 12 | 관광지 | 13,091 |
| 14 | 문화시설 | 2,632 |
| 15 | 축제공연행사 | 1,688 |
| 25 | 여행코스 | 1,069 |
| 28 | 레포츠 | 4,570 |
| 32 | 숙박 | 3,583 |
| 38 | 쇼핑 | 8,262 |
| 39 | 음식점(관광지) | 15,577 |

**sidos** (시도 17개) / **guguns** (구군)

### 맞춰야 할 것
- AI Server의 SQLAlchemy 모델을 위 테이블/컬럼명과 동일하게 작성
- Spring이 보내는 API 요청 형식에 AI Server 엔드포인트를 맞춤
- DB 접속 정보를 AI Server의 .env에 동일하게 설정

---

## 5단계 추천 플로우

### Step 1: 취향 수집 → 24차원 벡터 변환
- 설문에서 **동행 유형**(커플/가족/친구/혼자)을 먼저 수집
- 동행 유형에 따라 관련 분위기 차원에 초기 가중치 부여:
  - 커플 → romantic, aesthetic, nightlife, gourmet 약간 상향
  - 가족 → family, healing, learning, nature 약간 상향
  - 친구 → active, nightlife, gourmet, aesthetic 약간 상향
  - 혼자 → healing, nature, learning, heritage 약간 상향
- 선택지 UI로 세부 취향 파악 + 자연어 입력
- LLM(gpt 4o mini, 2 Credit)이 응답을 분석하여 24차원 가중치 벡터로 변환
- 방안 A: 미리 정의된 선택지 2~3개 + 마지막 LLM 1회 호출
- 방안 B: 매 턴 LLM이 이전 응답 보고 다음 질문 동적 생성 (2 Credit 모델, ~1초)
- **두 방안 프로토타입 테스트 후 최종 선정 예정**

### Step 2: 코사인 유사도 → 상위 20개
- Spring이 10km 내 + 카테고리 필터링한 장소를 AI Server에 전달
- NumPy로 사용자 24차원 벡터 vs 장소 24차원 벡터 코사인 유사도 계산
- 상위 20개 선정 (~1ms, LLM 호출 없음, Credit 0)
- 콜드 스타트: 상호작용 3회 미만 시 장소 벡터 평균값으로 폴백

### Step 3: LLM 리랭킹 (RAG)
- Retrieve: 코사인 유사도 상위 20개 장소 데이터
- Augment: 사용자 프로필 + 자연어 입력 이력 + 시간/날씨 맥락
- Generate: LLM(gpt 5 mini, 5 Credit)이 최종 5개 선정 + 자연어 추천 이유 생성
- 벡터만으로 불가능한 맥락 판단 수행 ("저녁이니 야경 우선", "어제 해변 갔으니 카페 섞기")

### Step 4: 사용자 피드백 → 가중치 학습
- Go: 장소 벡터 방향으로 이동 (학습률 0.15)
- Skip: 장소의 강한 속성만 감쇠 (학습률 0.08)
- 텍스트 선택지: 매핑된 차원 직접 조정 (학습률 0.20)
- 기타 직접 입력: LLM 파싱 후 반영 (학습률 0.25, 가장 강함)

### Step 5: 재추천 루프
- 불만족 시 선택지 5개 + 기타(직접 입력) 제공
- 선택 → 가중치 변경 → Step 2부터 다시 실행

### 학습률 설계 의도
- Go(0.15) > Skip(0.08): Skip은 "지금은 아니지만 나중에는 갈 수도"이므로 약하게
- 텍스트 선택(0.20) > Go(0.15): 명시적 의사 표현이 특정 장소 Go보다 강함
- 기타 입력(0.25) 가장 강함: 직접 타이핑 = 가장 확실한 취향 표현

### 가중치 벡터를 쓰는 이유 (vs 매번 전체 이력을 LLM에 넣기)
- 이력이 쌓여도 항상 24개 숫자만 보내면 됨 (비용 고정)
- LLM은 긴 이력에서 통계적 패턴을 정확히 못 잡음 (벡터는 수학적으로 압축)
- 동일 입력이면 코사인 유사도 결과가 항상 동일 (일관성)
- 가중치 벡터는 사용자 이력의 압축본

---

## 24차원 가중치 벡터 설계

### 카테고리 8개 (고정, TourAPI content_type_id 직접 매핑)
| 인덱스 | 차원명 | TourAPI 매핑 | DB content_type_id |
|--------|--------|-------------|-------------------|
| 0 | nature | 관광지(자연) | 12 |
| 1 | culture | 문화시설 | 14 |
| 2 | festival | 축제공연행사 | 15 |
| 3 | activity | 레포츠 | 28 |
| 4 | shopping | 쇼핑 | 38 |
| 5 | food | 음식점 | 39 |
| 6 | cafe | 음식점 중 카페 | 39 + overview에 카페/커피/디저트 포함 |
| 7 | lodging | 숙박 | 32 |

### 분위기 10개 (확정 - 클러스터링 + 키워드 검증 완료)

**분석 과정 (2026-03-18 완료):**
1. DB의 attractions 테이블 overview 텍스트 (50,472건 중 overview 있는 3,822건) 대상
2. Kiwi 형태소 분석 + TF-IDF + KMeans 클러스터링 (k=5~15 탐색, 최적 k=6~7)
3. 클러스터링으로 데이터에서 자연 분리되는 6개 확인: healing, aesthetic, gourmet, learning, heritage, nature
4. 동행 유형(커플/가족/친구/혼자) 매칭을 위해 4개 수동 추가: romantic, family, active, nightlife
5. 키워드 사전 기반 매칭률 검증 → 10개 전부 유의미한 커버리지 확인

| 인덱스 | 차원명 | 설명 | 매칭률 | 대표 키워드 |
|--------|--------|------|--------|------------|
| 8 | healing | 힐링/휴식/산책 | 31.4% | 공원, 휴식, 산책, 숲길, 생태, 수목원 |
| 9 | aesthetic | 감성/분위기/포토 | 32.1% | 분위기, 인테리어, 감성, 한옥, 벽화, 독특 |
| 10 | gourmet | 미식/맛집 | 25.6% | 전문점, 요리, 맛집, 셰프, 코스요리 |
| 11 | learning | 학습/문화/공연 | 25.3% | 박물관, 전시, 교육, 공연, 미술관 |
| 12 | heritage | 역사/유적/전통 | 30.9% | 역사, 조선, 사찰, 문화재, 전통 |
| 13 | mood_nature | 자연경관/산/바다 | 21.1% | 바다, 계곡, 해변, 폭포, 등산, 전망대 |
| 14 | romantic | 연인/데이트 | 18.3% | 야경, 노을, 연인, 데이트, 분수, 조명 |
| 15 | family | 가족/어린이 | 26.4% | 체험, 어린이, 가족, 동물, 농장, 놀이터 |
| 16 | active | 스포츠/액티비티 | 7.3% | 스포츠, 래프팅, 서핑, 클라이밍, 자전거 |
| 17 | nightlife | 야간/축제/밤문화 | 9.9% | 야간, 조명, 축제, 분수쇼, 라이브 |

**active(7.3%)와 nightlife(9.9%)가 낮은 이유:**
- overview 텍스트 기반 한계. 축제(15)와 레포츠(28)는 overview가 0건
- 보완 전략: active → content_type_id=28(레포츠)이면 active=1.0 강제 부여
- 보완 전략: nightlife → detailIntro2 운영시간 필드로 야간 운영 여부 보강

**동행 유형별 커버리지 (검증 완료):**
| 동행 | 관련 차원 | 커버리지 |
|------|----------|---------|
| 커플 | romantic, aesthetic, nightlife, gourmet | 59.9% |
| 가족 | family, healing, learning, nature | 59.7% |
| 친구 | active, nightlife, gourmet, aesthetic | 58.6% |
| 혼자 | healing, nature, learning, heritage | 65.6% |

**클러스터링 vs 초기 설계 비교:**
- 초기 설계 10개: healing, active, aesthetic, romantic, gourmet, learning, adventure, family, nightlife, local
- 최종 확정 10개: healing, aesthetic, gourmet, learning, **heritage**(←local+역사), **nature**(←adventure+자연경관), romantic, family, active, nightlife
- adventure → mood_nature로 통합 (클러스터링에서 산/계곡/바다가 한 그룹으로 묶임, 카테고리 nature와 키 충돌 방지)
- local → heritage로 변경 (클러스터링에서 역사/유적/사찰/전통이 한 그룹으로 묶임)

### 실용 6개 (고정, TourAPI detailIntro2 필드)
| 인덱스 | 차원명 | TourAPI 필드 | 추출 규칙 |
|--------|--------|-------------|----------|
| 18 | free_entry | usefee | "무료", "0원" 포함 → 1.0 |
| 19 | parking_available | parking | "가능" 포함 → 1.0 |
| 20 | pet_friendly | chkpet | "가능", "동반" 포함 → 1.0 |
| 21 | baby_friendly | chkbabycarriage | "가능", "대여" 포함 → 1.0 |
| 22 | indoor | overview 키워드 | "실내", "박물관", "전시" 등 |
| 23 | outdoor | overview 키워드 | "야외", "산책", "해변" 등 |

### 장소 벡터 생성 방법

**현재 DB 데이터 현황 (2026-03-18 확인):**
- 전체 attractions: 50,472건
- overview 있는 장소: 3,822건 (7.6%) ← 분위기 태깅 가능
- overview 없는 장소: 46,650건 ← title + 주소 + content_type_id만 존재
- 축제(15), 레포츠(28), 숙박(32)은 overview 0건

**태깅 전략 (완료 - 2026-03-18):**
1. **카테고리 8개**: content_type_id로 규칙 기반 (50,472건, 100% 정확)
2. **실용 6개**: indoor/outdoor은 overview 키워드 기반, 나머지(free_entry 등)는 detailIntro2 미확보로 현재 0.0
3. **분위기 10개 - overview 있는 3,663건**:
   - Step A: Gemini 2.5 Pro로 앵커 85건 정밀 태깅 (`anchor_tagged.csv`)
   - Step B: gpt-4.1-nano Few-shot으로 3,578건 대량 처리 (`bulk_tagged.csv`)
4. **분위기 10개 - overview 없는 46,809건**:
   - LLM 태깅 3,663건을 학습셋으로 **KNN 회귀 예측** (TF-IDF + content_type_id 원-핫, k=10)
   - GMS 추가 호출 없음

**향후 overview 데이터 추가 시 재생성 방법:**
- `ai/anchor_tagging.py`: 신규 앵커 선정 + Gemini 태깅
- `ai/bulk_tagging.py`: gpt-4.1-nano 대량 태깅
- `ai/generate_vectors.py`: 전체 50,472건 벡터 재생성 → DB 저장

### 분위기 키워드 사전 (태깅 및 검색용)

각 분위기 차원별 매칭에 사용하는 키워드 사전. Kiwi 형태소 분석 토큰 + 원문 부분문자열 매칭 병행.
2글자는 형태소 정확 매칭, 3글자 이상은 원문 부분문자열 매칭 (1글자 키워드는 노이즈 방지를 위해 제외).

```python
MOOD_KEYWORDS = {
    "healing": [
        "힐링", "치유", "휴식", "쉼터", "산책", "산책로", "평화", "조용",
        "고요", "명상", "템플스테이", "숲길", "둘레길", "녹지",
        "정원", "수목원", "식물원", "온천", "스파", "족욕", "찜질",
        "공원", "생태", "습지", "피톤치드", "자연휴양림", "휴양",
        "테라피", "웰니스", "요가", "노천탕", "노천",
        "편안", "여유", "느긋", "소풍", "벤치",
    ],
    "aesthetic": [
        "감성", "인테리어", "분위기", "포토존", "포토",
        "인스타", "감각", "세련", "모던", "빈티지",
        "루프탑", "테라스", "오션뷰", "리버뷰", "레이크뷰",
        "벽화", "아트", "갤러리", "디자인", "조형물",
        "한옥", "고풍", "유럽풍", "이국적", "이색", "독특",
        "무드", "데코", "카페거리", "예쁜", "아기자기",
        "스냅", "촬영", "풍경", "조형", "설치미술",
    ],
    "gourmet": [
        "맛집", "미식", "미슐랭", "식도락", "별미", "전문점",
        "요리", "셰프", "코스요리", "한정식", "정식",
        "한식", "양식", "중식", "일식", "브런치", "뷔페",
        "식감", "풍미", "진미", "구이", "숙성",
        "국밥", "비빔밥", "냉면", "삼겹살", "갈비",
        "해물", "횟집", "초밥", "돈까스", "파스타", "스테이크",
        "치킨", "피자", "막걸리", "전통주", "와인",
        "백반", "정갈", "손맛", "노포", "원조",
    ],
    "learning": [
        "박물관", "미술관", "과학관", "전시관", "기념관", "전시",
        "전시실", "교육", "학습", "체험관", "역사관", "문학관",
        "도서관", "아카데미", "강좌", "워크숍",
        "과학", "기술", "연구", "탐구", "관람", "해설",
        "도슨트", "유물", "소장품", "컬렉션",
        "공연", "연극", "뮤지컬", "콘서트", "오페라",
        "오케스트라", "무대", "극장", "공연장",
    ],
    "heritage": [
        "역사", "유적", "문화재", "국보", "보물", "사적",
        "조선", "고려", "삼국", "백제", "신라", "고구려",
        "궁궐", "왕궁", "성곽", "산성", "읍성", "석탑",
        "불탑", "석불", "마애불", "향교", "서원", "서당",
        "사당", "종묘", "왕릉", "고분",
        "독립", "항일", "독립운동", "기념비", "순국", "의병", "광복",
        "사찰", "암자", "법당", "대웅전", "불교", "스님", "창건",
        "전통", "민속", "한복", "전통문화", "고택", "종가",
    ],
    "mood_nature": [
        "등산", "봉우리", "정상", "능선", "계곡",
        "폭포", "절벽", "암벽", "협곡", "동굴",
        "바다", "해변", "해수욕장", "해안", "갯벌",
        "포구", "항구", "등대", "선착장", "방파제",
        "하천", "호수", "저수지", "연못",
        "들판", "초원", "억새", "갈대", "철새", "조류",
        "일출", "일몰", "운해", "전망대", "경관", "절경",
        "트레킹", "올레길", "해파랑길", "산악", "등산로",
        "바위", "기암", "기암괴석", "해안선", "수평선",
    ],
    "romantic": [
        "데이트", "연인", "커플", "프로포즈", "웨딩",
        "로맨틱", "낭만", "사랑",
        "야경", "일몰", "노을", "석양",
        "분수", "불빛", "조명", "촛불", "캔들",
        "와인바", "칵테일", "루프탑",
        "크루즈", "유람선", "요트",
        "관람차", "페리",
        "장미", "꽃길", "벚꽃", "라벤더",
        "오션뷰", "리버뷰", "레이크뷰", "선셋",
        "드라이브", "해안도로", "산책", "야간산책",
    ],
    "family": [
        "가족", "어린이", "키즈", "유아", "아이들", "아동",
        "키즈카페", "놀이터", "놀이공원", "테마파크", "워터파크",
        "동물원", "아쿠아리움", "수족관", "체험", "만들기",
        "견학", "체험학습",
        "유모차", "수유실", "패밀리", "온가족",
        "캐릭터", "애니메이션", "인형",
        "목장", "농장", "동물", "먹이주기", "승마",
        "미끄럼틀", "트램펄린", "레일바이크",
        "피크닉", "캠핑", "글램핑", "바비큐",
    ],
    "active": [
        "레포츠", "스포츠", "액티비티", "모험", "탐험", "어드벤처",
        "서핑", "래프팅", "카약", "카누", "패러글라이딩", "번지점프",
        "짚라인", "오프로드",
        "스쿠버", "다이빙", "스노클링", "낚시",
        "스키", "스노보드", "썰매",
        "자전거", "사이클", "라이딩",
        "암벽등반", "클라이밍", "볼더링",
        "골프", "테니스", "축구", "농구", "야구",
        "수영", "워터스포츠", "수상스키", "웨이크보드", "제트스키",
        "마라톤", "러닝", "조깅", "트레일러닝",
        "바이크", "MTB", "ATV",
    ],
    "nightlife": [
        "야경", "야간", "야시장", "야간개장", "나이트",
        "불빛", "조명", "네온", "일루미네이션",
        "펍", "클럽", "라운지", "루프탑바",
        "포차", "호프", "이자카야",
        "라이브", "공연",
        "불꽃놀이", "분수쇼", "미디어아트", "빛축제",
        "달빛", "별빛", "야간산책", "야간투어",
        "칵테일", "야간조명", "야간관람",
        "축제", "페스티벌", "버스킹",
    ],
}
```

### 벡터 값 범위
- 장소: 각 차원 0.0 ~ 1.0
- 사용자: 각 차원 -1.0 (비선호) ~ 1.0 (강한 선호)
- 신규 사용자 초기값: 모두 0.0

---

## GMS 설정 (SSAFY Gen AI Management System)

### API 호출 방식
GMS는 기존 AI API의 프록시. 엔드포인트만 GMS로 바꾸고, API_KEY 자리에 GMS_KEY를 넣으면 됨.

```
GMS_BASE_URL = https://gms.ssafy.io/gmsapi
```

**OpenAI 계열 (gpt-4o-mini, gpt-4.1-nano, gpt-5-mini 등):**
```
URL:  {GMS_BASE_URL}/api.openai.com/v1/responses
Header: Authorization: Bearer {GMS_KEY}
Body: OpenAI API 형식 그대로 (model, input 등)
```

**Gemini 계열 (gemini-2.5-pro 등):**
```
URL:  {GMS_BASE_URL}/generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
Header: x-goog-api-key: {GMS_KEY}
Body: Gemini API 형식 그대로 (contents 등)
```

### 모델별 용도 및 Credit

| 용도 | 모델 | Credit/회 | API 타입 |
|------|------|----------|---------|
| 벡터 태깅 앵커 생성 (1회성, 100건) | gemini-2.5-pro | 30 | Gemini |
| 벡터 태깅 대량 처리 (1회성, ~3,800건) | gpt-4.1-nano | 1 | OpenAI |
| Step 1 취향 수집 (벡터 변환) | gpt-4o-mini | 2 | OpenAI |
| Step 3 RAG 리랭킹 (최종 추천) | gpt-5-mini | 5 | OpenAI |
| 기타 입력 파싱 | gpt-4o-mini | 2 | OpenAI |
| 선택지 생성 | gpt-4o-mini | 2 | OpenAI |

총 예산: ~90,000 / 100,000 Credit

---

## Spring ↔ AI Server API 계약

**⚠️ 아래는 초기 설계안. 실제 Spring 코드를 읽고 기존 API 형식에 맞춰 수정해야 함.**

### POST /api/v1/onboard (Step 1: 취향 수집)
- Request: user_id, user_message, chat_history, is_complete
- Response: message, choices[], is_complete, updated_user_weights[]
- AI Server가 사용자 벡터를 DB에 직접 저장

### POST /api/v1/recommend (Step 2+3: 추천)
- Request: user_id, user_prompt, category, latitude, longitude, radius_km
- AI Server가 DB에서 직접 반경 내 장소 조회 + 사용자 벡터 읽기
- Response: recommendations[{content_id, title, reason, score}], message

### POST /api/v1/feedback (Step 4: Go/Skip 학습)
- Request: user_id, place_id, action("go"|"skip")
- AI Server가 DB에서 장소 벡터 + 사용자 벡터 읽고, 학습 후 DB에 직접 저장
- Response: status, action

### POST /api/v1/refine (Step 5: 재추천)
- Request: user_id, user_prompt, choice_id, latitude, longitude, excluded_place_ids[]
- AI Server가 DB에서 직접 조회 + 가중치 변경 + 재추천
- Response: recommendations[], choices[], message

---

## 코드 구조

```
ai/
├── main.py                    # FastAPI 진입점 (개발 예정)
├── requirements.txt
├── Dockerfile
├── Jenkinsfile
├── .env.example
│
├── [벡터 생성 스크립트 - overview 추가 시 재실행]
├── anchor_tagging.py          # Step A: Gemini 2.5 Pro로 앵커 100건 정밀 태깅
├── bulk_tagging.py            # Step B: gpt-4.1-nano Few-shot으로 overview 있는 장소 대량 태깅
├── generate_vectors.py        # 전체 50,472건 벡터 생성 → DB 저장
│                              #   LLM 태깅 결과 + KNN 회귀(TF-IDF + content_type 원-핫)로 예측
│
└── [LLM 태깅 결과 데이터 - generate_vectors.py 재실행 시 필요]
    ├── anchor_tagged.csv      # Gemini 태깅 완료 85건 (10개 분위기 차원 점수)
    └── bulk_tagged.csv        # gpt-4.1-nano 태깅 완료 3,578건 (10개 분위기 차원 점수)
```

### 개발 예정 파일 (main.py 이하 서버 코드)
- **config.py**: 설정 (GMS URL, DB URL, 모델명, 학습률)
- **dimensions.py**: ALL_DIMS 리스트(24개), DIM_COUNT=24, DIM_INDEX 딕셔너리
- **api/routes.py**: 엔드포인트 4개
- **schemas/models.py**: 요청/응답 스키마
- **db/session.py**: SQLAlchemy async 세션
- **db/models.py**: DB 테이블 모델. **반드시 백엔드 코드를 읽고 컬럼명/타입 맞출 것**
- **services/gms_client.py**: GMS API 호출
- **services/weight_calculator.py**: rank_by_cosine() + update_on_go/skip/choice/custom()
- **services/place_repository.py**: DB에서 반경 내 장소 조회, 장소 벡터 읽기
- **services/user_repository.py**: 사용자 벡터 읽기/쓰기
- **services/recommendation.py**: 5단계 파이프라인 오케스트레이터

### 벡터 스크립트 사용법
```bash
# ① DB에 벡터 재생성 (GMS 토큰 소모 없음 - CSV 읽어서 KNN 계산만)
py -3 generate_vectors.py

# ② overview 데이터가 새로 추가됐을 때만 (GMS 토큰 소모)
py -3 anchor_tagging.py select   # 앵커 선정 (토큰 없음)
py -3 anchor_tagging.py tag      # Gemini 2.5 Pro 태깅 → anchor_tagged.csv (30 Credit/건)
py -3 bulk_tagging.py run        # gpt-4.1-nano 태깅 → bulk_tagged.csv (1 Credit/건)
py -3 generate_vectors.py        # 전체 벡터 재생성 → DB 저장 (토큰 없음)
```

---

## 빅데이터 배치 (매일 새벽 4시)
- 전체 사용자 로그를 국가/연령대/성별/동행 유형별로 Go/Skip 패턴 통계 분석
- 분석 결과를 장소 벡터에 반영하여 매일 업데이트
- 신규 사용자에게 동일 세그먼트 통계 벡터를 초기값으로 부여 (콜드 스타트 해결)
- 데이터 쌓일수록 규칙 기반 → 랭킹 모델 기반 추천으로 확장

### 나이대별 cold-start 개선 계획 (배치 연동)

**현재 (임시)**: `vector_utils.py`의 `_AGE_BOOST` 딕셔너리에 규칙 기반 하드코딩 값 사용.
- 10~29세: active, aesthetic, nightlife 상향
- 30~49세: gourmet, healing, family 상향
- 50세~: heritage, learning, healing 상향

**목표 (배치 완성 후 교체)**:
- Spark 배치가 매일 새벽 4시에 연령대 × 성별 × 동행 유형 세그먼트별 Go/Skip 통계 집계
- 각 세그먼트에서 가장 많이 Go한 장소 Top 5의 벡터 평균 → 세그먼트 대표 벡터 계산
- 결과를 별도 테이블(예: `segment_vectors`)에 저장
- `build_cold_start_vector()` 함수를 수정하여 `_AGE_BOOST` 대신 DB에서 해당 세그먼트 벡터를 조회하도록 변경
- 즉, `_AGE_BOOST`는 배치 파이프라인이 붙기 전까지만 쓰는 임시 규칙이고, 실제 데이터 기반 통계로 교체 예정

**교체 시 수정 위치**: `ai/vector_utils.py`의 `_AGE_BOOST` 및 `build_cold_start_vector()` 함수

---

## 벡터DB(RediSearch)를 안 쓰는 이유
- 24차원 × 수백 건이면 NumPy로 1ms에 끝남
- 768차원 수백만 건을 검색하는 게 아니라서 벡터DB는 과함
- PostGIS로 위치 필터링, 코사인 유사도는 Python 코드로 충분

---

## TODO

### 완료
- [x] **DB 스키마 확인**: dump_postgresql.sql로 전체 테이블 구조 파악 완료 (chat_messages 테이블 추가 확인)
- [x] **분위기 차원 10개 확정**: 클러스터링(Kiwi+TF-IDF+KMeans) + 키워드 검증 완료
- [x] **사용자 벡터 저장 방식 확인**: user_vectors 테이블 이미 존재 (preference_vector json 컬럼)
- [x] **장소 벡터 저장 방식 확인**: attraction_vectors 테이블 이미 존재 (attraction_vector json 컬럼)
- [x] **PostgreSQL 접속 정보 확인**: localhost:5432/ohmyguide, admin/1234
- [x] **앵커 태깅**: Gemini 2.5 Pro로 85건 정밀 태깅 완료 (`anchor_tagged.csv`)
- [x] **대량 벡터 태깅**: gpt-4.1-nano Few-shot으로 3,578건 처리 완료 (`bulk_tagged.csv`)
- [x] **전체 attraction_vectors 생성**: 50,472건 DB 저장 완료 (LLM 3,663건 + KNN 예측 46,809건)

### 미완료
- [ ] **DB 모델 맞추기**: db/models.py를 위 스키마와 동일한 컬럼명/타입으로 작성
- [ ] Step 1 방안 A vs B 테스트 후 선정
- [ ] Spring → AI Server API 호출 부분 확인 → 엔드포인트 맞추기
- [ ] **설문 설계**: 동행 유형(커플/가족/친구/혼자) 수집 → 관련 분위기 차원 가중치 초기화에 활용
- [ ] **실용 6개 보강**: detailIntro2(free_entry, parking, pet, baby) 데이터 확보 후 벡터 재생성 (`generate_vectors.py` 재실행)

---

## AI 서버 파일별 역할

### 서버 코드
| 파일 | 역할 |
|------|------|
| `main.py` | FastAPI 진입점. 엔드포인트 정의 (`/api/ai/recommend`, `/api/ai/refine`), 요청/응답 스키마, 추천 파이프라인 오케스트레이션 |
| `database.py` | PostgreSQL 연결 (psycopg2). 사용자 벡터 CRUD, PostGIS 반경 내 장소+벡터 조회 |
| `gms_client.py` | GMS(LLM 프록시) API 호출. 리랭킹(`rerank_places`), 텍스트→벡터 변환(`vectorize_refine_text`), 날씨 조회(`get_weather`), 시간 맥락(`get_time_context`) |
| `vector_utils.py` | 24차원 벡터 연산. 코사인 유사도 랭킹(`rank_places`), cold-start 벡터 생성(`build_cold_start_vector`), Go/Skip/선택지 학습(`apply_vector_delta`, `apply_vector_choices`) |

### 벡터 생성 스크립트 (1회성 / overview 추가 시 재실행)
| 파일 | 역할 |
|------|------|
| `anchor_tagging.py` | Gemini 2.5 Pro로 앵커 장소 85건 정밀 태깅 → `anchor_tagged.csv` 생성 (30 Credit/건) |
| `bulk_tagging.py` | gpt-4.1-nano Few-shot으로 overview 있는 장소 ~3,578건 대량 태깅 → `bulk_tagged.csv` 생성 (1 Credit/건) |
| `generate_vectors.py` | LLM 태깅 CSV + KNN 회귀로 전체 50,472건 24차원 벡터 생성 → DB `attraction_vectors` 저장 (Credit 소모 없음) |

### 데이터 파일
| 파일 | 역할 |
|------|------|
| `anchor_tagged.csv` | Gemini 태깅 완료 85건 (분위기 10차원 점수) |
| `bulk_tagged.csv` | gpt-4.1-nano 태깅 완료 3,578건 (분위기 10차원 점수) |
| `ai_api.md` | AI Server API 명세 문서 |

### 인프라
| 파일 | 역할 |
|------|------|
| `requirements.txt` | Python 패키지 의존성 |
| `Dockerfile` | AI 서버 Docker 이미지 빌드 |
| `Jenkinsfile` | CI/CD 파이프라인 (빌드→배포) |
| `.env` / `.env.example` | 환경변수 (DB 접속, GMS_KEY 등) |
