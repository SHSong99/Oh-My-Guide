
 # Oh My Guide - Android 프로젝트 가이드

## 프로젝트 개요

외국인 관광객을 위한 AI 가이드 앱. 사용자의 GPS 위치 기반으로 주변 관광지를 추천하고, 네이버 지도 위에서 네비게이션 + 챗봇 가이드를 제공한다.

- 패키지명: `com.ohmyguide.app`
- 언어: Kotlin
- UI: Jetpack Compose (XML 금지)
- DI: Hilt
- 지도: 네이버 지도 SDK 3.23.1 + naver-map-compose
- GPS: FusedLocationProviderClient + 포그라운드 서비스
- API 키 관리: `local.properties` → `BuildConfig` (git에 올리지 않음)

---

## 폴더 구조

```
com.ohmyguide.app/
├── data/                ← 서버 API 통신
│   ├── api/             ← Retrofit 인터페이스
│   ├── model/           ← API 요청/응답 모델
│   └── repository/      ← API 호출 실행 (비즈니스 로직 금지)
│
├── domain/              ← 비즈니스 로직
│   ├── model/           ← 앱 내부 데이터 모델
│   └── usecase/         ← 데이터 가공 로직
│
├── service/             ← 안드로이드 시스템 서비스 (GPS, 알림 등)
│   └── LocationForegroundService.kt
│
├── di/                  ← Hilt 모듈 (의존성 주입 설정)
│   ├── AppModule.kt
│   └── LocationModule.kt
│
├── fixtures/            ← Mock/Preview 데이터
│   └── Fixtures.kt
│
├── ui/
│   ├── common/          ← 공통 재사용 컴포넌트
│   ├── theme/           ← Color.kt, Gradient.kt, Type.kt, Theme.kt
│   ├── navi/            ← NavGraph.kt, Routes.kt
│   └── screen/          ← 각 화면별 패키지
│       ├── auth/
│       ├── onboarding/  ← Splash, Welcome, GpsPermission, Category, Loading
│       ├── home/        ← HomeScreen + HomeComponents + HomeViewModel
│       ├── map/         ← MapScreen + MapViewModel
│       ├── explore/     ← ExploreScreen + CourseDetailScreen + CourseNaviScreen
│       ├── place/       ← PlaceScreen
│       ├── transport/   ← TransportPickerScreen + TransitDetailScreen
│       ├── navi/        ← NaviScreen + NaviComponents + NaviViewModel
│       ├── story/       ← StoryScreen + StoryComponents
│       ├── phrases/     ← PhrasesScreen + PhrasesComponents
│       └── mypage/      ← MyPageScreen
│
├── MainActivity.kt      ← NavHost 라우팅만 (로직 금지)
└── MyApplication.kt     ← Hilt + NaverMapSdk 인증
```

---

## 화면 라우트

`ui/navi/Routes.kt`의 Screen sealed class 참조.

- Splash → Welcome → GpsPermission → InterestSelect(Category) → Loading → Home
- Home → Place → Transport → TransitDetail → Navi
- Home → Explore → CourseDetail → CourseNavi
- 하단탭: Home, Explore, Phrases

---

## GPS 아키텍처

```
핸드폰 GPS / Wi-Fi / 기지국
    ↓
FusedLocationProviderClient (3초 간격)
    ↓
LocationForegroundService (companion object StateFlow)
    ↓
각 Screen에서 collectAsState()로 수집
    ↓
NaverMap + rememberFusedLocationSource()로 파란 점 표시
```

- 권한 요청: GpsPermissionScreen의 "Allow Location Access" 버튼에서 처리
- 역지오코딩: `Geocoder(context, Locale.ENGLISH)`로 좌표 → 영어 주소 변환

---

## 필수 코딩 지침

### 1. Compose Only
XML 레이아웃 사용 금지. Compose UI만 사용.

### 2. 색상 토큰
`Color(0xFF...)` 직접 사용 금지. `ui/theme/Color.kt`에 토큰 추가 후 import.

토큰 네이밍 규칙:
- 정보 카드 → `Info` (InfoPurple, InfoPurpleBg)
- 다크 모드 → `Dark` (DarkBg, DarkSurface)
- 대중교통 → `Transit` (TransitAmber, TransitGray)
- 메뉴/뱃지 → `Menu` (MenuBookmark, MenuBookmarkBg)
- 배경 → `Bg` (BgScreen, BgDivider)
- 텍스트 → `Text` (TextPrimary, TextCaption)
- 보더 → `Border` (Border, BorderLight)

### 3. 그라디언트
화면/컴포넌트 파일에 `Brush.xxxGradient(...)` 직접 정의 금지. `ui/theme/Gradient.kt`에 추가 후 import.

### 4. 공통 컴포넌트
`ui/common/`에서 재사용. 중복 구현 금지. 1파일 = 1컴포넌트.

현재 등록된 공통 컴포넌트:
- OmgButton.kt → CTA 버튼 (아이콘 지원), PrimaryButton 별칭
- OmgTopBar.kt → 뒤로가기 + 제목 탑바
- BottomNavBar.kt → 하단 네비게이션 (Home, Explore, Phrases)
- RadioIndicator.kt → 라디오 선택 표시
- InfoCard.kt → 아이콘 + 값 + 라벨 정보 카드
- AppDivider.kt → 구분선
- MascotAvatar.kt → 마스코트 아바타
- FeaturePill.kt → 이모지 + 라벨 필
- ChatBubbles.kt → GuideBubble / UserBubble
- FloatingNavButton.kt → 내비 플로팅 버튼
- TypingIndicator.kt → 타이핑 중 표시

### 5. API 구조
Api → Repository → UseCase → ViewModel. ApiResult 래퍼 사용.

### 6. SSE
OkHttp SseClient 사용.

### 7. 파일 크기 제한
Composable 파일 1개당 300줄 이하. 초과 시 `{Feature}Components.kt`로 분리.

### 8. UiState + Preview
모든 화면에 `sealed class XxxUiState` 정의. `@Preview` 함수 필수.

### 9. Mock 데이터
컴포넌트/ViewModel 안에 Mock 데이터 금지. `fixtures/Fixtures.kt`에 분리.

### 10. 로깅
`Log.d()`는 `BuildConfig.DEBUG` 조건 안에서만 사용.

### 11. MainActivity
NavHost 라우팅만. 로직 작성 금지.

### 12. ViewModel
1 ViewModel = 1 화면. 다른 화면의 상태 포함 금지.

### 13. Repository
API 호출만. 비즈니스 로직 금지.

### 14. service/ 폴더
핸드폰 자체 기능 (GPS, 알림, 센서 등). 서버 API가 아닌 시스템 서비스는 여기에 배치. `data/repository/`에 넣지 않는다.

---

## PR 전 체크리스트

- 하드코딩 색상 `Color(0x...)` 없는지 확인
- 인라인 그라디언트 `Brush.xxxGradient(...)` 없는지 확인
- 화면 파일 300줄 이하인지 확인
- 공통 컴포넌트 중복 구현 없는지 확인
- Mock 데이터가 컴포넌트/ViewModel에 들어있지 않은지 확인
- `@Preview` 함수 포함 여부 확인
- `assembleDebug` 빌드 성공 확인

---

## 빌드 & 실행 명령어

```bash
# 빌드
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug

# 실기기 설치 & 실행
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew installDebug -PandroidConnectedDeviceProvider.serial=R39M30B58GZ
adb -s R39M30B58GZ shell am start -n com.ohmyguide.app/.MainActivity

# 에뮬레이터 설치 & 실행
JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew installDebug
adb shell am start -n com.ohmyguide.app/.MainActivity
```

---

## 네이버 지도 설정

- SDK 버전: 3.23.1 (NCP 인증 방식)
- 클라이언트 ID: `local.properties`의 `NAVER_MAP_CLIENT_ID`
- 인증: `MyApplication.onCreate()`에서 `NaverMapSdk.NcpKeyClient`로 코드 기반 등록
- NCP 콘솔 필수 설정: Dynamic Map 체크 + 패키지명 `com.ohmyguide.app` 등록