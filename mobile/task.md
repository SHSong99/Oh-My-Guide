# 다국어(i18n) 남은 작업

## 완료된 작업
- [x] AppStrings 인터페이스 + 5개 언어 파일 생성 (EN, JA, KO, ZH_CN, ZH_TW)
- [x] AppLanguage enum + LanguageManager + LocalStrings CompositionLocal 설정
- [x] 온보딩 화면 적용 (Splash, Welcome, GpsPermission, Category, Loading)
- [x] HomeComponents, BottomNavBar, MyPageScreen 적용
- [x] HomeScreen, MapScreen, PlaceScreen 적용
- [x] TransportPickerScreen, TransportComponents, TransitDetailScreen 적용
- [x] NaviScreen, NaviComponents 적용
- [x] ExploreScreen, ExploreComponents, CourseDetailScreen, CourseNaviScreen 적용
- [x] StoryComponents 적용

## 남은 작업

### 1. PhrasesComponents.kt - LocalStrings 적용
- `PhrasesHeader`: "한국어 구문" → `strings.koreanPhrasesTitle`
- `PhrasesHeader`: "Korean Phrases for Travelers" → `strings.koreanPhrasesSubtitle`
- `PhrasesHeader`: "$savedCount saved" → `"$savedCount ${strings.saved}"`
- `MascotTip`: "Tap the bookmark..." → `strings.bookmarkHint`
- `PhraseSectionCard`: "${section.phrases.size} phrases" → `"${section.phrases.size} ${strings.phrasesUnit}"`

### 2. CourseDetailScreen.kt - spots 문자열
- `CourseInfo`: "${course.spotCount} spots" → `"${course.spotCount} ${strings.spots}"`

### 3. 빌드 확인
- `JAVA_HOME="C:/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug` 빌드 성공 확인
- TransportComponents.kt의 enum 변경(label→labelKey, desc→descKey)이 다른 곳에 영향 없는지 확인

### 4. NaviViewModel 내 하드코딩 문자열 확인
- NaviViewModel에서 챗봇 메시지로 보내는 문자열이 있다면 LocalStrings 적용 필요
- HomeViewModel도 동일하게 확인

### 5. Fixtures.kt 내 사용자에게 보이는 문자열 점검
- 카테고리 라벨, 챗봇 메시지 등 Mock 데이터 중 번역이 필요한 항목 확인

### 6. 언어 전환 UI 연결
- MyPageScreen의 Language 설정에서 실제 `LanguageManager.setLanguage()` 호출 연결
- 또는 온보딩에서 언어 선택 시 `LanguageManager` 연동
