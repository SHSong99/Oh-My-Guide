# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repo Structure

This is a **monorepo** hosted on GitLab with three top-level services:

- `backend/` — Spring Boot API (this directory)
- `ai/` — FastAPI ML/recommendation service
- `infra/` — Docker Compose files, nginx config, Spark jobs

**This CLAUDE.md covers the `backend/` service only.** All commands below assume `backend/` as the working directory.

## Project Overview

**Oh My Guide (oh-my-guide)** — a Korean tourism guide backend service. Spring Boot 3.5 / Java 17 application with PostgreSQL, Redis, Kafka, and Google OAuth2. Part of SSAFY 14기 project (group com.e103, package `com.e103.ohmyguide`).

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

Copy `.env.example` to `.env` and fill in values. Spring loads `.env` as properties via `spring.config.import: "optional:file:.env[.properties]"` in `application.yml`. Required variables: DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, REDIS_HOST/PORT, KAFKA_BOOTSTRAP_SERVERS, KAFKA_GROUP_ID, JWT_SECRET, JWT_EXPIRATION.

## Testing

- Tests use **Testcontainers** with PostgreSQL 16 — Docker must be running.
- All integration tests extend `IntegrationTestSupport`, which spins up a PostgreSQL container, activates `test` profile, and wraps tests in `@Transactional`.
- Test profile config: `src/test/resources/application-test.yml` — uses Testcontainers JDBC driver (`jdbc:tc:postgresql:16:///gmdb`) with `ddl-auto: create-drop`.

## Architecture

### Domain-driven package structure

Each domain lives under `com.e103.ohmyguide.domain.<domain>/` with `entity/` and `repository/` sub-packages. Discover domains by browsing `src/main/java/com/e103/ohmyguide/domain/`.

### Key patterns

- **BaseEntity** (`global/entity/`) — `@MappedSuperclass` with `createdAt`/`updatedAt` via JPA Auditing (enabled in `JPAConfig`)
- Entities use `@NoArgsConstructor(access = PROTECTED)` + `@Builder` with private constructor
- All `@ManyToOne` associations use `FetchType.LAZY`
- Gugun entity uses `@JoinColumns` composite FK mapping (sido_code + gugun_code)
- No service/controller layer yet — currently entity + repository only

### Infrastructure

- **PostgreSQL** — primary database
- **Redis** — configured but dependency currently commented out in build.gradle
- **Kafka** — event streaming (spring-kafka dependency active)
- **Google OAuth2 / Spring Security** — dependencies commented out in build.gradle, config present in application.yml

## CI/CD

Jenkins pipeline (`Jenkinsfile`) triggers on **accepted merge requests to `master`** on GitLab. It detects which files changed and conditionally builds/deploys only affected services. Backend is built as a Docker image (`Amazon Corretto 17 Alpine`), deployed via `docker compose` from `infra/application-server/`, and notifications go to Mattermost on success/failure.
