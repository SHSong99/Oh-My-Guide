# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repo Structure

This is a **monorepo** hosted on GitLab with three top-level services:

- `backend/` — Spring Boot API (this directory)
- `ai/` — FastAPI ML/recommendation service
- `infra/` — Docker Compose files, nginx config, Spark jobs

**This CLAUDE.md covers the `backend/` service only.** All commands below assume `backend/` as the working directory.

## Project Overview

**Oh My Guide (oh-my-guide)** — a Korean tourism guide backend service. Spring Boot 3.5 / Java 17 application with PostgreSQL, Redis, Kafka, HDFS/Spark, and Google OAuth2. Part of SSAFY 14기 project (group com.e103, package `com.e103.ohmyguide`).

## Build & Run Commands

```bash
# Build (skip tests)
./gradlew bootJar -x test

# Run all tests (requires Docker for Testcontainers)
./gradlew test

# Run a single test class
./gradlew test --tests "com.e103.ohmyguide.domain.user.repository.UserRepositoryTest"

# Run a single test method
./gradlew test --tests "com.e103.ohmyguide.domain.user.repository.UserRepositoryTest.someMethod"

# Clean build
./gradlew clean build
```

## Environment Setup

Copy `.env.example` to `.env` and fill in values. Spring loads `.env` as properties via `spring.config.import: "optional:file:.env[.properties]"` in `application.yml`.

Required variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_GROUP_ID`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `TOKEN_SECRET`, `TOUR_API_KEY`, `HDFS_HOST`, `HDFS_PORT`, `HDFS_USER`.

## Testing

- Tests use **Testcontainers** with PostgreSQL 16 — Docker must be running.
- **Integration tests** extend `IntegrationTestSupport` (`@SpringBootTest` + `@ActiveProfiles("test")` + `@Testcontainers` + `@Transactional`), which spins up a PostgreSQL container.
- **Controller tests** extend `ControllerTestSupport` for MockMvc-based endpoint testing.
- Test profile config: `src/test/resources/application-test.yml` — uses Testcontainers JDBC driver (`jdbc:tc:postgresql:16:///gmdb`) with `ddl-auto: create-drop`.

## Architecture

### Domain-driven package structure

Each domain lives under `com.e103.ohmyguide.domain.<domain>/` with sub-packages like `entity/`, `repository/`, `controller/`, `service/`, `dto/`. Discover domains by browsing `src/main/java/com/e103/ohmyguide/domain/`.

Key domains with full controller/service layers:
- **auth** — Google OAuth2 login, JWT token management, Spring Security integration (`AuthController`, `TokenProvider`, `CustomOAuth2UserService`)
- **user** — User profile and onboarding (`UserController`: `GET /user/me`, `PUT /user/onboarding`)
- **popularplace** — Attraction recommendations with Spark job integration (`PopularPlaceController`: `GET /pickRecommend`, `POST /pickRecommend/calculate`)
- **userlog** — Kafka consumer → HDFS log writer pipeline (no REST endpoints)

Entity-only domains: `attraction`, `attractionphrase`, `attractionvector`, `chatmessage`, `contenttype`, `gugun`, `phrase`, `sido`, `userphrase`, `uservector`, `uservisited`

### Global package (`com.e103.ohmyguide.global`)

- **config/** — `JPAConfig` (JPA Auditing), `SecurityConfig` (OAuth2 + JWT filter chain, stateless sessions, method-level security), `WebMvcConfig` (CORS), `RestTemplateConfig`, `HdfsConfig`, `SchedulingConfig`, `AppProperties` (binds `app.*` properties)
- **entity/** — `BaseEntity` (`@MappedSuperclass` with `createdAt`/`updatedAt` via JPA Auditing)
- **exception/** — `ResourceNotFoundException` (404), `UnAuthorizedException` (401), `OAuth2AuthenticationProcessingException`
- **api/** — `TourApiClient` (Korea Tour API HTTP client)
- **init/** — `AttractionDataFiller` (startup data loader)
- **util/** — `CookieUtils` (OAuth2 cookie helpers)

### Key patterns

- Entities use `@NoArgsConstructor(access = PROTECTED)` + `@Builder` with private constructor
- All `@ManyToOne` associations use `FetchType.LAZY`
- Gugun entity uses `@JoinColumns` composite FK mapping (sido_code + gugun_code)
- Security: Google OAuth2 → JWT token flow; `TokenAuthenticationFilter` validates JWTs on each request; `@CurrentUser` annotation injects authenticated principal
- Kafka consumer (`UserLogConsumer`) writes user activity events to HDFS via `HdfsLogWriter`; Spark jobs (`SparkJobService` via Apache Livy) analyze those logs

### Infrastructure

- **PostgreSQL** — primary database (`ddl-auto: validate` in prod)
- **Redis** — configured in `application.yml` (dependency currently commented out in build.gradle)
- **Kafka** — async user activity event streaming
- **HDFS** — distributed log storage (Hadoop client dependency)
- **Apache Livy** — remote Spark job submission for log analysis
- **Google OAuth2 + JWT** — authentication flow with Spring Security

## CI/CD

Jenkins pipeline (`Jenkinsfile`) triggers on **accepted merge requests to `master`** on GitLab. It detects which files changed and conditionally builds/deploys only affected services. Backend is built as a Docker image (`Amazon Corretto 17 Alpine`), deployed via `docker compose` from `infra/application-server/`, and notifications go to Mattermost on success/failure.
