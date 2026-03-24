프로젝트 개요
앱 이름: Oh My Guide!
타겟: 한국 방문 외국인 관광객
UI 언어: 영어 베이스, 한국어 구문(Phrase) 부분만 한국어
디바이스: 모바일 (iPhone 15 Pro 기준 393×852)
배경: 화이트 베이스
디자인 시스템 요약
메인 컬러: #5478FF (그라데이션 적용 시 linear-gradient 135deg, #2D56ED → #5478FF)
서브 컬러: #FFDE42 (그라데이션 적용 시 linear-gradient 135deg, #F5CA00 → #FFDE42)
메인 입체: linear-gradient(135deg, #325BFF 0%, #5478FF 50%, #7C98FF 100%) — CTA 버튼 등에 사용
서브 입체: linear-gradient(135deg, #FFC107 0%, #FFDE42 50%, #FFF08E 100%) — 한국어 구문 카드 등에 사용
다이렉트 믹스: linear-gradient(135deg, #5478FF 0%, #FFDE42 100%) — 프로그레스바, 로딩 등에 사용
텍스트 컬러: #1A1A2E (메인), #6B7280 (서브), #8892A4 (캡션/힌트)
보더/구분선: #E8ECF4
배경 서브: #F5F7FA (입력창 배경, 카드 배경 등)
로고 폰트: Fredoka 700
영어 UI 폰트: Fredoka (브랜드 요소, 헤딩) + Pretendard (바디)
한국어: Pretendard 400~800
카드 라운드: 16px
버튼 라운드: 16px
프로필 아바타 라운드: 50% (원형)
캐릭터: 한복 입은 도깨비 마스코트 (제공된 PNG 사용)

Screen 1: 스플래시 (Splash)
레이아웃: 전체 화면 센터 정렬
배경: 화이트 → #F0F4FF 수직 그라데이션 (미세하게)
구성 요소:

캐릭터 이미지: 가운데, 140×140px, drop-shadow(0 12px 24px rgba(84,120,255,0.2))
캐릭터 아래 24px 간격으로 로고 텍스트 "Oh My Guide!" — Fredoka 700, 28px, 메인 입체 그라데이션 텍스트
로고 아래 8px 간격으로 서브 카피 "Your personal Korea travel buddy" — Pretendard 400, 13px, #8892A4
서브 카피 아래 32px 간격으로 로딩 스피너 — 32×32, 원형, 보더 3px #E8ECF4, 상단 보더만 #5478FF
애니메이션 참고: 캐릭터는 바운스 인(작았다가 커지며 등장), 텍스트는 페이드 인, 2초 후 자동 전환


Screen 2: 웰컴 (Welcome / Onboarding Intro)
레이아웃: 세로 풀스크린, 상단 콘텐츠 + 하단 CTA 고정
상단 여백: 54px (상태바 영역)
구성 요소:

캐릭터 이미지: 센터, 160×160px, 둥둥 떠다니는 플로팅 효과 (위아래 8px 반복), drop-shadow
캐릭터 아래 28px → 메인 타이틀 영역 (센터 정렬):

"Annyeong! 👋" — Fredoka 700, 26px, #1A1A2E
바로 아래 "I'm your Korea guide" — Fredoka 700, 22px, #1A1A2E


타이틀 아래 16px → 설명 텍스트 (센터, max-width 280px):

"I'll help you discover amazing places, navigate like a local, and even teach you useful Korean phrases along the way!" — Pretendard 400, 14px, #6B7280, line-height 1.6


설명 아래 24px → 피처 필(Feature Pills) 가로 wrap 배치, 센터 정렬, gap 8px:

"📍 GPS Guide" / "🗣️ Korean Phrases" / "🎯 Personalized" / "🗺️ Navigation"
각 필: Pretendard 500, 12px, #5478FF, padding 6px 14px, background #F5F7FA, border-radius 20px, border 1px solid #E8ECF4
하단 CTA 영역: padding 좌우 28px, 하단 40px


메인 버튼 "Let's Get Started! ✨" — full width, padding 16px 24px, 메인 입체 그라데이션 배경, 흰색 텍스트, Fredoka 600 16px, border-radius 16px, box-shadow 0 8px 24px rgba(84,120,255,0.3)
메인 버튼 아래 12px → "Sign in with Google" 텍스트 버튼 — Pretendard 400, 13px, #8892A4, 배경 투명


Screen 3: 온보딩 카테고리 선택 (Guided Category Selection)
핵심 콘셉트: 설문조사가 아닌 챗봇이 대화하듯 가이딩하는 느낌. 상단은 채팅 UI, 중간에 카테고리 카드가 나열됨.
헤더: 높이 54px, 센터에 "Oh My Guide!" Fredoka 600 16px #1A1A2E, 하단 보더 1px #F0F2F5
채팅 영역: flex-grow, 스크롤 가능, padding 16px 20px, 배경 #FAFBFF → #FFFFFF 수직 그라데이션
챗봇 메시지 버블 1:

좌측 캐릭터 아바타 36×36 원형, border 2px solid #5478FF
버블: background linear-gradient(135deg, #F0F4FF, #FFFFFF), border-radius 4px 18px 18px 18px, padding 12px 16px, box-shadow 0 2px 8px rgba(84,120,255,0.08)
텍스트: "Nice to meet you! 🎉 Before we explore, tell me — what excites you most about Korea?" — Pretendard 400, 14px, #1A1A2E, line-height 1.5
타이핑 효과로 한 글자씩 나오는 느낌 (피그마에선 완성 상태로 표현)

챗봇 메시지 버블 2:

동일 스타일
"Pick as many as you like! I'll use this to find the perfect spots for you."

카테고리 카드 리스트: 세로 나열, gap 8px, 버블 아래 8px 시작
각 카테고리 카드 구성:

전체: full width, padding 14px 16px, border-radius 16px, border 2px solid #E8ECF4 (미선택) / 2px solid #5478FF (선택), background #FFFFFF (미선택) / rgba(84,120,255,0.05) (선택)
선택 시 box-shadow 0 4px 16px rgba(84,120,255,0.15), 약간 scale(1.02)
좌측: 이모지 아이콘 박스 44×44, border-radius 12px, background #F5F7FA (미선택) / 해당 카테고리 컬러 20% 투명도 (선택), 이모지 22px
중앙: 카테고리명 Fredoka 600 15px + 설명 Pretendard 400 12px #8892A4
우측: 체크 서클 24×24, 미선택 시 border 2px solid #D0D5DD / 선택 시 메인 입체 그라데이션 fill + 흰색 체크 아이콘

카테고리 6개:

🍜 Korean Food / Street food, BBQ & cafés / 포인트 #FF6B6B
🏛️ Culture & History / Temples, palaces & museums / 포인트 #5478FF
🌿 Nature & Parks / Mountains, beaches & trails / 포인트 #4CAF50
🛍️ Shopping / Markets, malls & vintage / 포인트 #FF9800
🌙 Nightlife / Bars, clubs & live music / 포인트 #9C27B0
🎤 K-Culture / K-pop, K-drama spots / 포인트 #E91E63

하단 고정 영역: padding 12px 20px 36px, border-top 1px #F0F2F5, 배경 흰색

캐릭터 미니 아바타 28×28 + 상태 텍스트 Pretendard 400 12px #8892A4

0개 선택: "Choose what interests you!"
1개 선택: "Great pick! Add more or let's go!"
2개 이상: "Awesome combo! N selected"


CTA 버튼: "Find My Perfect Spots! 🚀" (활성) / "Select at least one" (비활성)

활성: 메인 입체 그라데이션, 흰색 텍스트, shadow
비활성: background #E8ECF4, color #B0B8C8, shadow 없음
Fredoka 600 15px, full width, padding 15px 24px, border-radius 16px



피그마 시안 변형: 미선택 상태 1장 + 2~3개 선택된 상태 1장, 총 2 프레임 권장

Screen 4: 로딩 (Finding Spots)
레이아웃: 전체 화면 센터 정렬, 배경 #FFFFFF
구성 요소:

캐릭터 120×120, 통통 튀는 바운스 애니메이션 (피그마에선 약간 위로 올라간 상태로), drop-shadow
아래 20px → "Finding amazing spots..." — Fredoka 600 18px #1A1A2E
아래 8px → "Scanning nearby places based on your interests" — Pretendard 400 13px #8892A4 센터, max-width 240px
아래 20px → 프로그레스 바: 200px wide, 4px height, border-radius 2px, 배경 #E8ECF4, 채움 다이렉트 믹스 그라데이션 (#5478FF → #FFDE42), 60% 정도 채워진 상태로


Screen 5: 메인 챗봇 — 장소 추천 화면
핵심: 온보딩 완료 후 진입하는 메인 화면. 챗봇이 대화하듯 장소를 추천해주는 구조.
헤더: 높이 60px, padding 좌우 20px

좌측: 캐릭터 아바타 36×36 원형 + border 2px solid #5478FF
아바타 우측 8px: "Oh My Guide!" Fredoka 600 15px #1A1A2E + 아래에 초록 dot(6×6) + "Online · GPS Active" Pretendard 400 11px #8892A4
우측 끝: 설정 아이콘 버튼 36×36, border-radius 12px, background #F5F7FA, 안에 톱니바퀴 아이콘 #5478FF

채팅 바디: flex-grow, 스크롤, padding 16px, 배경 #FAFBFF → #FFFFFF
콘텐츠 순서:
(1) 관심사 필 표시: 센터 정렬, 가로 배치

라운드 칩: padding 6px 14px, background #F5F7FA, border-radius 20px
안에 선택된 카테고리 이모지들 16px + "Your interests" Pretendard 400 11px #8892A4

(2) 챗봇 메시지:

"Great choices! 🎉 Based on your interests, I found some amazing spots nearby. Let me show you my top picks!"
스타일은 Screen 3 챗봇 버블과 동일

(3) 장소 추천 카드 3개: 세로 나열, gap 8px, 좌측 44px 들여쓰기 (아바타 너비만큼)
각 카드:

전체: full width, padding 12px 14px, background #FFFFFF, border-radius 16px, border 1px solid #E8ECF4, box-shadow 0 2px 8px rgba(0,0,0,0.04)
좌측: 이모지 아이콘 박스 60×60, border-radius 12px, 해당 카테고리 색상 30~60% 투명도 그라데이션 배경, 이모지 26px
중앙:

장소명: Fredoka 600 14px #1A1A2E (예: "Gwangjang Market")
한글명: Pretendard 400 11px #8892A4 (예: "광장시장")
아래 6px: 별점 "★ 4.8" Pretendard 600 11px #FFBF00 + 거리 "350m" 11px #8892A4 + 태그 칩 "Food" 10px padding 2px 8px background #F0F4FF color #5478FF border-radius 8px


우측: 화살표 chevron right 아이콘 #C0C6D4

샘플 데이터:

🥘 Gwangjang Market / 광장시장 / ★4.8 / 350m / Food / #FF6B6B
🏘️ Bukchon Hanok Village / 북촌한옥마을 / ★4.6 / 1.2km / Culture / #5478FF
🗼 Namsan Tower / 남산타워 / ★4.7 / 2.1km / Nature / #4CAF50

(4) "다른 추천 보기" 버튼: full width, padding 12px, border-radius 14px, border 1px dashed rgba(84,120,255,0.25), background rgba(84,120,255,0.04), 텍스트 "Show More Recommendations ✨" Fredoka 600 13px #5478FF
(5) Quick Korean Phrases 섹션:

헤더: 캐릭터 미니 아바타 24×24 + "Quick Korean Phrases 🇰🇷" Fredoka 600 12px #5478FF
카드 2개 가로 배치, gap 8px, 각 flex: 1
각 카드: padding 12px, border-radius 14px, background linear-gradient(135deg, #FFFDE7, #FFF9C4), border 1px solid rgba(255,222,66,0.25)

한국어: Pretendard 700 18px #1A1A2E (예: "이거 주세요")
발음: Pretendard 400 italic 10px #5478FF (예: "i-geo ju-se-yo")
영어: Pretendard 400 11px #6B7280 (예: "This one, please")


두 번째 카드: "얼마예요?" / "eol-ma-ye-yo" / "How much is it?"

하단 입력창: padding 12px 16px 36px, border-top 1px #F0F2F5

인풋 영역: background #F5F7FA, border-radius 16px, padding 12px 16px

좌측: 검색 아이콘 20×20 #B0B8C8
중앙: placeholder "Ask me anything about Korea..." Pretendard 400 14px #B0B8C8
우측: 전송 버튼 32×32, border-radius 10px, 메인 입체 그라데이션 배경, 흰색 send 아이콘




Screen 6: 장소 상세 (Place Detail)
헤더: 뒤로가기 화살표 + 장소명 + 공유 아이콘
메인 이미지: full width, height 220px, border-radius 하단 24px, 장소 대표 사진 (피그마에서 unsplash 이미지 활용)
이미지 위 오버레이: 하단에 그라데이션 (투명 → 반투명 검정), 그 위에 장소명 흰색
콘텐츠 영역: padding 20px

장소명: Fredoka 700 22px #1A1A2E
영문/한글명: Pretendard 400 13px #8892A4
별점 + 거리 + 카테고리 태그: 가로 배치
상세 설명: Pretendard 400 14px #6B7280, line-height 1.6, 2~3줄

정보 카드 그리드 (2×2): gap 12px

각 카드: flex 1, padding 16px, background #F5F7FA, border-radius 16px
카드 1: 🕐 운영시간 "Hours" / "09:00 - 18:00"
카드 2: 💰 요금 "Fee" / "Free" 또는 "₩5,000"
카드 3: 📍 거리 "Distance" / "350m · 5 min walk"
카드 4: 🗺️ 지도 미리보기 (미니맵, 내 위치 핀 + 목적지 핀 표시)
각 카드 내: 이모지 + 라벨 Pretendard 600 12px #8892A4 + 값 Pretendard 600 14px #1A1A2E

하단 고정 버튼 2개: 가로 배치, gap 12px, padding 16px 20px 36px

"NO" 버튼: flex 1, padding 16px, background #F5F7FA, border-radius 16px, Fredoka 600 15px #8892A4, 센터 정렬
"GO" 버튼: flex 2, padding 16px, 메인 입체 그라데이션, 흰색 텍스트, Fredoka 600 15px, border-radius 16px, box-shadow


Screen 7: 네비게이션 + 가이드
상단 50%: 지도 영역

지도 배경 (피그마에선 카카오맵/구글맵 스크린샷 또는 회색 지도 placeholder)
현재 위치 핀 (파란 dot + pulse 링) + 목적지 핀 (캐릭터 아이콘 마커)
경로 라인: #5478FF, 3px, 약간 커브
지도 하단 오버레이 바: background 반투명 white blur, border-radius 상단 20px, padding 12px 20px

"350m · 5 min" Fredoka 600 14px #1A1A2E + 상태 칩 "Walking 🚶" 배경 #E8F5E9 color #4CAF50



헤더 오버레이: 지도 위 상단

뒤로가기 + "이동 중" / "도착 완료" 상태 표시
토글 버튼: 지도/가이드 모드 전환 아이콘

하단 50%: 가이드 영역 (스크롤 가능)

챗봇 형태로 AI 가이드 콘텐츠 제공
캐릭터 아바타 + 버블 스타일
콘텐츠: 장소 소개, 유명한 이유, 관광 포인트, 이동 팁 등
하단에 Quick Phrases 4개 (서브 입체 그라데이션 배경 카드)

각 구문: 한국어 + 발음 + 영어
우측에 북마크 아이콘 (탭 시 저장)




Screen 8: 구문집 (My Phrases) — 2순위이므로 간략하게
탭 구조: "All" / "Saved" / "My Own"
카테고리 필터: 가로 스크롤 칩 (음식, 기본, 쇼핑, 길찾기 등)
각 구문 카드: 한국어 큰 글씨 + 발음 + 영어, 우측 북마크 토글
하단: 입력창 "Add your own phrase..."

Screen 9: K-Culture 테마 코스 — 2순위이므로 간략하게
세로 카드 리스트, 각 카드:

배경 이미지 (테마 관련) + 오버레이
제목 + 영문명 + 장소 수 + 소요시간 + 동선 아이콘
예: "🎤 K-Pop Pilgrimage" / 4 spots · 3 hours


전체 인터랙션 플로우 요약
스플래시 → 웰컴 → 카테고리 선택 (챗봇 가이딩) → 로딩 → 메인 챗봇 (장소 추천 카드) → 장소 탭 시 상세 → GO 탭 시 네비+가이드 → 가이드 완료 후 메인 복귀 또는 다음 장소
피그마 프레임 구성 제안: 총 9~10프레임. 카테고리 선택은 미선택/선택 2가지 상태, 장소 상세는 1프레임, 네비+가이드는 이동 중/도착 완료 2가지 상태로 잡으면 포트폴리오에도 충분한 볼륨이 나올 거야.