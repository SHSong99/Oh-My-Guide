# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OhMyGuide (`com.ohmyguide.app`) — a native Android travel/location guide app with AI-assisted recommendations, real-time location tracking, and Naver Maps integration. Written in Kotlin with Jetpack Compose.

## Build Commands

```bash
./gradlew build                    # Build debug APK
./gradlew installDebug             # Install on connected device/emulator
./gradlew clean build              # Clean and rebuild
./gradlew test                     # Run unit tests
./gradlew connectedAndroidTest     # Run instrumented tests (requires device)
./gradlew assembleRelease          # Build release APK
```

## Architecture

Clean Architecture with three layers, all under `app/src/main/java/com/ohmyguide/app/`:

- **data/** — API definitions (Retrofit), response/request models, repository implementations
- **domain/** — Business models and use cases
- **ui/** — Jetpack Compose screens and ViewModels (MVVM pattern)
  - `ui/screen/` — Feature screens (auth, onboarding, home, map, explore, phrases, place, navi, transport, story, mypage)
  - `ui/common/` — Reusable composables (buttons, top bars, chat bubbles)
  - `ui/navi/` — Navigation graph (`NavGraph.kt`) and route definitions (`Routes.kt`)
  - `ui/theme/` — Design system (colors, typography, gradients)
- **di/** — Hilt dependency injection modules (`AppModule`, `LocationModule`)
- **service/** — Android services (foreground location tracking)
- **fixtures/** — Mock/fixture data for development

## Key Technical Details

- **DI**: Hilt — use `@HiltViewModel`, `@AndroidEntryPoint`, `@Inject`
- **Navigation**: Jetpack Navigation Compose — routes defined as sealed class `Screen` in `Routes.kt`
- **State management**: ViewModel + StateFlow, collected via `collectAsState()` in composables
- **Networking**: Retrofit + OkHttp (with SSE support via `okhttp-sse`)
- **Maps**: Naver Maps SDK with `naver-map-compose` wrapper
- **Location**: Play Services FusedLocationProvider with a foreground service
- **Serialization**: Gson
- **Annotation processing**: KSP (not kapt)

## Local Configuration

`local.properties` must contain `NAVER_MAP_CLIENT_ID=<client_id>` — this is injected into both the manifest and BuildConfig at build time.

## Build Targets

- Min SDK 24 (Android 7.0), Target/Compile SDK 36
- JVM target: 11
- Dependency versions centralized in `gradle/libs.versions.toml`
