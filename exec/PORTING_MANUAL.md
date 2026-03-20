# 포팅 메뉴얼 — Oh! My Guide

> 최종 업데이트: 2026-03-20

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [서버 구성 및 사전 준비](#2-서버-구성-및-사전-준비)
3. [Application Server 배포 (43.200.1.94)](#3-application-server-배포)
4. [Data Server 배포 (43.203.244.30)](#4-data-server-배포)
5. [Jenkins CI/CD 설정](#5-jenkins-cicd-설정)
6. [Spring Boot 배포](#6-spring-boot-배포)
7. [AI Server 배포](#7-ai-server-배포)
8. [최종 동작 확인](#8-최종-동작-확인)
9. [로컬 개발 환경 설정](#9-로컬-개발-환경-설정)

---

## 1. 프로젝트 개요

사용자 위치 기반 실시간 관광지 AI 추천 & 가이드 서비스.

### 전체 아키텍처

```
Internet
    │
    ▼
┌─────────────────────────────────────────────────────┐
│             Application Server (43.200.1.94)         │
│                j14e103a.p.ssafy.io                   │
│                                                      │
│  Nginx (80/443) ─── Let's Encrypt SSL                │
│     ├── /api/      → Spring Boot (8080)              │
│     ├── /ai/       → FastAPI (8000)                  │
│     ├── /jenkins/  → Jenkins (8080)                  │
│     └── /grafana/  → Grafana (3000)                  │
│                                                      │
│  PostgreSQL / Redis / Kafka / Prometheus / Grafana   │
└─────────────────────────────────────────────────────┘
    │
    │  서버 간 통신 (내부 IP)
    │
    ▼
┌─────────────────────────────────────────────────────┐
│              Data Server (43.203.244.30)             │
│                j14e103.p.ssafy.io                    │
│                                                      │
│  Hadoop (HDFS) / Spark / Livy                        │
└─────────────────────────────────────────────────────┘
```

### 서버 정보

| 구분       | Application Server      | Data Server             |
|-----------|-------------------------|-------------------------|
| 공인 IP    | `43.200.1.94`           | `43.203.244.30`         |
| 도메인     | `j14e103a.p.ssafy.io`   | `j14e103.p.ssafy.io`    |
| 역할       | Spring Boot, AI, 모니터링 | Hadoop, Spark, Livy     |

---

### 포트 개방 현황

#### Application Server (43.200.1.94)

| 포트  | 서비스              | 외부 접근 가능 여부  | 설명                               |
|------|--------------------|--------------------|-----------------------------------|
| 22   | SSH                | ✅ 전체             | 서버 접속                          |
| 80   | Nginx HTTP         | ✅ 전체             | HTTPS 리다이렉트                   |
| 443  | Nginx HTTPS        | ✅ 전체             | `/api/`, `/ai/`, `/jenkins/`, `/grafana/` |
| 5432 | PostgreSQL         | ⚠️ Data Server만   | `43.203.244.30` IP만 허용          |
| 8989 | Jenkins            | 🔒 루프백 전용      | `127.0.0.1` 바인딩, Nginx 경유 접근 |
| 9090 | Prometheus         | 🔒 내부망 전용      | app-network 내부만                 |
| 3000 | Grafana            | 🔒 내부망 전용      | app-network 내부만, Nginx 경유 접근 |

#### Data Server (43.203.244.30)

| 포트  | 서비스                | 외부 접근 가능 여부       | 설명                          |
|------|----------------------|--------------------------|------------------------------|
| 22   | SSH                  | ✅ 전체                   | 서버 접속                     |
| 9870 | HDFS NameNode Web UI | ✅ 전체                   | HDFS 웹 모니터링              |
| 18080 | Spark Master Web UI | ✅ 전체                   | Spark 웹 모니터링             |
| 8081 | Spark Worker Web UI  | ✅ 전체                   | Spark Worker 모니터링         |
| 9000 | HDFS RPC             | ⚠️ App Server만           | Spring Boot → NameNode 통신   |
| 8020 | HDFS IPC             | ⚠️ App Server만           | HDFS 내부 통신                |
| 9864 | DataNode HTTP        | ⚠️ App Server만           | 데이터 읽기/쓰기 접근          |
| 9866 | DataNode 데이터 전송  | ⚠️ App Server만           | 실제 블록 데이터 전송          |
| 9867 | DataNode IPC         | ⚠️ App Server만           | DataNode 내부 통신            |
| 7077 | Spark 클러스터 통신  | ⚠️ App Server만           | Spark Master 클러스터 포트    |
| 6066 | Spark REST API       | ⚠️ App Server만           | Spark 직접 Job 제출           |
| 8998 | Livy REST API        | ⚠️ App Server만           | Spring Boot → Spark Job 제출  |

---

## 2. 서버 구성 및 사전 준비

### 2.1 필수 소프트웨어 (두 서버 공통)

각 서버에 SSH 접속 후 Docker를 설치합니다.

```bash
# Docker 설치
curl -fsSL https://get.docker.com | sh

# 현재 사용자를 docker 그룹에 추가 (재로그인 필요)
sudo usermod -aG docker $USER

# Docker Compose plugin 확인
docker compose version
```

### 2.2 UFW 방화벽 설정

#### Application Server (43.200.1.94)

```bash
sudo ufw allow 22
sudo ufw allow 80/tcp
sudo ufw allow 443
sudo ufw allow from 43.203.244.30 to any port 5432
sudo ufw enable
```

#### Data Server (43.203.244.30)

```bash
sudo ufw allow 22
sudo ufw allow 9870/tcp        # HDFS Web UI
sudo ufw allow 18080/tcp       # Spark Master Web UI
sudo ufw allow 8081/tcp        # Spark Worker Web UI
sudo ufw allow from 43.200.1.94 to any port 9000   # HDFS RPC
sudo ufw allow from 43.200.1.94 to any port 8020   # HDFS IPC
sudo ufw allow from 43.200.1.94 to any port 7077   # Spark 클러스터
sudo ufw allow from 43.200.1.94 to any port 6066   # Spark REST
sudo ufw allow from 43.200.1.94 to any port 8998   # Livy REST
sudo ufw allow from 43.200.1.94 to any port 9864   # DataNode HTTP
sudo ufw allow from 43.200.1.94 to any port 9866   # DataNode 데이터
sudo ufw allow from 43.200.1.94 to any port 9867   # DataNode IPC
sudo ufw enable
```

### 2.3 레포지토리 클론

두 서버 모두 동일한 레포지토리를 클론합니다.

```bash
git clone <GitLab 레포지토리 URL>
cd S14P21E103
```

---

## 3. Application Server 배포

> Application Server(43.200.1.94)에서 실행합니다.

### 3.1 Docker 네트워크 생성

```bash
# app-network 생성 (최초 1회)
docker network create app-network
```

### 3.2 SSL 인증서 발급 (Let's Encrypt)

인증서 발급 전, Nginx를 HTTP 전용 임시 설정으로 먼저 실행해야 합니다.

```bash
cd infra/application-server

# 1) certbot webroot 디렉토리 생성
mkdir -p certbot/www certbot/conf

# 2) Nginx를 HTTP 모드로만 실행 (certbot challenge용)
docker compose -f docker-compose.infra.yml up -d nginx

# 3) 인증서 발급
docker run --rm \
  -v ./certbot/conf:/etc/letsencrypt \
  -v ./certbot/www:/var/www/certbot \
  certbot/certbot certonly \
  --webroot -w /var/www/certbot \
  -d j14e103a.p.ssafy.io \
  --email <이메일> \
  --agree-tos --no-eff-email

# 4) 인증서 발급 확인
ls certbot/conf/live/j14e103a.p.ssafy.io/
# fullchain.pem, privkey.pem 파일이 있어야 함
```

인증서 발급 후 Nginx 설정이 HTTPS를 참조하도록 `nginx/conf.d/default.conf`가 이미 구성되어 있습니다.

### 3.3 infra/application-server/.env 생성

```bash
cd infra/application-server
```

`.env` 파일을 아래 항목으로 생성합니다.

```env
# PostgreSQL
POSTGRES_USER=<DB 사용자명>
POSTGRES_PASSWORD=<DB 비밀번호>
POSTGRES_DB=ohmyguide

# Grafana
GRAFANA_ADMIN_PASSWORD=<Grafana 관리자 비밀번호>
```

### 3.4 인프라 컨테이너 실행 (PostgreSQL, Redis, Kafka, Nginx)

```bash
cd infra/application-server

# PostgreSQL, Redis, Kafka, Nginx, Certbot 실행
docker compose -f docker-compose.infra.yml up -d
```

**동작 확인:**

```bash
# PostgreSQL
docker exec postgresql pg_isready
# 출력 예시: /var/run/postgresql:5432 - accepting connections

# Redis
docker exec redis redis-cli ping
# 출력 예시: PONG

# Kafka
docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
# 출력 예시: (빈 목록 또는 topic 목록)
```

### 3.5 모니터링 실행 (Prometheus, Grafana)

```bash
cd infra/application-server

docker compose -f docker-compose.monitoring.yml up -d
```

**동작 확인:**

```bash
# Prometheus (Nginx 경유)
curl -s https://j14e103a.p.ssafy.io/grafana/api/health
# 출력 예시: {"database":"ok"}
```

---

## 4. Data Server 배포

> Data Server(43.203.244.30)에서 실행합니다.

### 4.1 infra/data-server/.env 생성

```bash
cd infra/data-server
```

`.env` 파일을 아래 항목으로 생성합니다.

```env
# DataNode가 외부 클라이언트(Spring Boot)에 광고할 IP
DATANODE_HOSTNAME=43.203.244.30
```

### 4.2 Hadoop, Spark, Livy 실행

```bash
cd infra/data-server

# 최초 실행 시 Livy 이미지 빌드로 약 1~2분 소요
docker compose -f docker-compose.hadoop.yml up -d --build
```

**동작 확인:**

```bash
# NameNode Web UI 접근 확인
curl -s "http://43.203.244.30:9870/webhdfs/v1/?op=LISTSTATUS"
# 출력 예시: {"FileStatuses":{"FileStatus":[...]}}

# DataNode 1개 정상 등록 확인
docker exec namenode hdfs dfsadmin -report | grep -A3 "Live datanodes"
# 출력 예시: Live datanodes (1): ... Hostname: 43.203.244.30

# Spark Master 확인
curl -s http://43.203.244.30:18080 | grep -o "Spark Master"
# 출력 예시: Spark Master

# Spark Worker 확인
curl -s http://43.203.244.30:8081 | grep -o "Spark Worker"
# 출력 예시: Spark Worker

# Livy 확인
curl -s http://43.203.244.30:8998/batches
# 출력 예시: {"from":0,"total":0,"batches":[]}
```

---

## 5. Jenkins CI/CD 설정

> Application Server(43.200.1.94)에서 실행합니다.

### 5.1 Jenkins 실행

```bash
cd infra/jenkins_setup

# 호스트의 Docker GID 확인
DOCKER_GID=$(getent group docker | cut -d: -f3)
echo "Docker GID: $DOCKER_GID"

# Jenkins 실행 (DooD 방식)
DOCKER_GID=$DOCKER_GID docker compose -f docker-compose.jenkins.yml up -d --build
```

Jenkins는 루프백 포트(`127.0.0.1:8989`)로만 바인딩되며, Nginx를 통해 `https://j14e103a.p.ssafy.io/jenkins/`로 접근합니다.

### 5.2 Jenkins 초기 설정

1. `https://j14e103a.p.ssafy.io/jenkins/` 접속
2. 초기 관리자 비밀번호 확인:
   ```bash
   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
3. 추천 플러그인 설치
4. 관리자 계정 생성

### 5.3 Jenkins 플러그인 설치

Jenkins 관리 → Plugin Manager에서 아래 플러그인을 설치합니다.

- **GitLab** (GitLab 연동 및 Webhook 트리거)
- **Credentials** (기본 설치됨)
- **Pipeline** (기본 설치됨)

### 5.4 Jenkins Credentials 등록

Jenkins 관리 → Credentials → System → Global credentials에서 아래 항목을 등록합니다.

| Credential ID          | 종류        | 내용                                      |
|-----------------------|-------------|-------------------------------------------|
| `backend-env-file`    | Secret file | `backend/.env` 파일 (Spring Boot 환경변수) |
| `ai-env-file`         | Secret file | `ai/.env` 파일 (FastAPI 환경변수)          |
| `mattermost-webhook-url` | Secret text | Mattermost Incoming Webhook URL          |
| `data-server-ssh-key` | SSH key     | Data Server SSH 접속용 개인키 (rsync 배포) |

#### backend-env-file 내용 (Secret file)

```env
# ===== PostgreSQL =====
DB_HOST=postgresql
DB_PORT=5432
DB_NAME=ohmyguide
DB_USERNAME=<DB 사용자명>
DB_PASSWORD=<DB 비밀번호>

# ===== Redis =====
REDIS_HOST=redis
REDIS_PORT=6379

# ===== Kafka =====
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_GROUP_ID=oh-my-guide

# ===== OAuth2 =====
GOOGLE_CLIENT_ID=<Google OAuth Client ID>
GOOGLE_CLIENT_SECRET=<Google OAuth Client Secret>

# ===== JWT =====
JWT_SECRET=<256비트 이상의 JWT 시크릿 키>
JWT_EXPIRATION=86400000
```

#### ai-env-file 내용 (Secret file)

```env
# PostgreSQL
DB_HOST=postgresql
DB_PORT=5432
DB_NAME=ohmyguide
DB_USER=<DB 사용자명>
DB_PASSWORD=<DB 비밀번호>

# GMS (SSAFY Gen AI Management System)
GMS_KEY=<GMS API 키>
GMS_BASE_URL=https://gms.ssafy.io/gmsapi
```

### 5.5 GitLab Webhook 설정

Jenkins Pipeline 프로젝트 생성 후 GitLab → Settings → Webhooks에서 등록합니다.

- **URL**: `https://j14e103a.p.ssafy.io/jenkins/project/<파이프라인 이름>`
- **Trigger**: Merge request events (Accepted)
- **SSL verification**: Enable

### 5.6 Pipeline 프로젝트 생성

Jenkins에서 아래 3개의 Pipeline 프로젝트를 생성합니다.

| Pipeline 이름 | Jenkinsfile 경로 | 트리거 조건 |
|-------------|-----------------|------------|
| `oh-my-guide-backend` | `backend/Jenkinsfile` | master MR 승인 시 |
| `oh-my-guide-ai` | `ai/Jenkinsfile` | master MR 승인 시 |
| `oh-my-guide-data` | `infra/data-server/Jenkinsfile` | master MR 승인 시 |

각 프로젝트 설정:
- Pipeline → Definition: **Pipeline script from SCM**
- SCM: Git, Repository URL: GitLab 레포지토리 URL
- Script Path: 위 표의 Jenkinsfile 경로

---

## 6. Spring Boot 배포

Jenkins CI/CD를 통해 자동 배포됩니다. 최초 배포는 Jenkins에서 수동으로 `oh-my-guide-backend` 파이프라인을 실행합니다.

### 파이프라인 동작

| 조건 | 실행 Stage | 동작 |
|-----|-----------|------|
| `backend/**` 변경 또는 `spring-boot` 컨테이너 유실 | Build Backend Image | `docker build -t oh-my-guide-backend:latest ./backend` |
| 동일 | Deploy Backend | `backend-env-file` 시크릿 주입 → `docker-compose.backend.yml up -d` |

배포 후 `./backend/.env`는 자동으로 삭제됩니다.

**헬스체크 확인:**

```bash
curl -s https://j14e103a.p.ssafy.io/api/actuator/health
# 출력 예시: {"status":"UP"}
```

---

## 7. AI Server 배포

Jenkins CI/CD를 통해 자동 배포됩니다. 최초 배포는 Jenkins에서 수동으로 `oh-my-guide-ai` 파이프라인을 실행합니다.

### 파이프라인 동작

| 조건 | 실행 Stage | 동작 |
|-----|-----------|------|
| `ai/**` 변경 또는 `fastapi` 컨테이너 유실 | Build AI Image | `docker build -t oh-my-guide-ai:latest ./ai` |
| 동일 | Deploy AI | `ai-env-file` 시크릿 주입 → `docker-compose.ai.yml up -d` |

배포 후 `./ai/.env`는 자동으로 삭제됩니다.

**헬스체크 확인:**

```bash
curl -s https://j14e103a.p.ssafy.io/ai/health
# 출력 예시: {"status":"ok"}
```

---

## 8. 최종 동작 확인

### Application Server

| 서비스 | URL | 예상 응답 |
|-------|-----|---------|
| Spring Boot | `https://j14e103a.p.ssafy.io/api/actuator/health` | `{"status":"UP"}` |
| FastAPI | `https://j14e103a.p.ssafy.io/ai/health` | `{"status":"ok"}` |
| Jenkins | `https://j14e103a.p.ssafy.io/jenkins/` | Jenkins 로그인 페이지 |
| Grafana | `https://j14e103a.p.ssafy.io/grafana/` | Grafana 로그인 페이지 |

### Data Server

| 서비스 | URL | 예상 응답 |
|-------|-----|---------|
| HDFS NameNode Web UI | `http://43.203.244.30:9870` | HDFS 웹 UI |
| Spark Master Web UI | `http://43.203.244.30:18080` | Spark Master UI |
| Spark Worker Web UI | `http://43.203.244.30:8081` | Spark Worker UI |
| Livy API | `http://43.203.244.30:8998/batches` | `{"from":0,"total":0,"batches":[]}` |

### 컨테이너 상태 전체 확인

**Application Server:**

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

정상 상태 예시:
```
NAMES                STATUS
nginx                Up ... (healthy)
spring-boot          Up ... (healthy)
fastapi              Up ... (healthy)
postgresql           Up ... (healthy)
redis                Up ... (healthy)
kafka                Up ... (healthy)
prometheus           Up ...
grafana              Up ...
jenkins              Up ...
certbot              Up ...
postgres-exporter    Up ...
node-exporter        Up ...
kafka-exporter       Up ...
```

**Data Server:**

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

정상 상태 예시:
```
NAMES           STATUS
namenode        Up ... (healthy)
datanode        Up ... (healthy)
spark-master    Up ... (healthy)
spark-worker    Up ... (healthy)
livy            Up ...
```

---

---

## 9. 로컬 개발 환경 설정

> 개발 시 로컬 단일 컴퓨터에서 전체 인프라를 실행하는 방법입니다.
> Nginx(SSL)는 로컬에서 불필요하므로 제외합니다.

### 9.1 사전 준비

#### infra/application-server/.env 생성

```env
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=your_db
GRAFANA_ADMIN_PASSWORD=your_grafana_password
```

#### infra/data-server/.env 생성

```env
DATANODE_HOSTNAME=localhost
```

#### backend/.env 생성 (Spring Boot 로컬 실행용)

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ohmyguide
DB_USERNAME=<DB 사용자명>
DB_PASSWORD=<DB 비밀번호>

REDIS_HOST=localhost
REDIS_PORT=6379

KAFKA_BOOTSTRAP_SERVERS=localhost:9094
KAFKA_GROUP_ID=oh-my-guide

GOOGLE_CLIENT_ID=<Google OAuth Client ID>
GOOGLE_CLIENT_SECRET=<Google OAuth Client Secret>

JWT_SECRET=<256비트 이상의 JWT 시크릿 키>
JWT_EXPIRATION=86400000
```

> **운영 환경과의 차이점**:
> - 운영: `DB_HOST=postgresql`, `REDIS_HOST=redis`, `KAFKA_BOOTSTRAP_SERVERS=kafka:9092` (컨테이너명, PLAINTEXT 리스너)
> - 로컬: DB/Redis는 `localhost`, Kafka는 `localhost:9094` (EXTERNAL 리스너)
>
> Kafka `9092`(PLAINTEXT)는 Docker 내부 전용으로 `kafka:9092`를 반환하므로, 로컬에서는 반드시 `9094`(EXTERNAL) 사용

### 9.2 로컬 인프라 실행

프로젝트 루트(`S14P21E103/`)에서 실행합니다.

```bash
# app-network 생성 (최초 1회)
docker network create app-network

# ── Application Server 실행 (PostgreSQL, Redis, Kafka) ───────────────────
# Redis(6379), Kafka(9092/9094) 포트 기본 파일에 포함
docker compose -f infra/application-server/docker-compose.infra.yml \
               up -d postgresql redis kafka

# ── 모니터링 실행 (Prometheus, Grafana) ──────────────────────────────────
docker compose -f infra/application-server/docker-compose.monitoring.yml \
               -f infra/application-server/docker-compose.monitoring.local.yml \
               up -d

# ── Data Server 실행 (Hadoop, Spark, Livy) ───────────────────────────────
# 최초 실행 시 Livy 이미지 빌드로 약 1~2분 소요
docker compose -f infra/data-server/docker-compose.hadoop.yml \
               -f infra/data-server/docker-compose.hadoop.local.yml \
               up -d --build
```

이후 Spring Boot / FastAPI는 IDE에서 직접 실행합니다.

### 9.3 로컬 서비스 포트

| 서비스     | 포트  | 접속 URL |
|-----------|-------|---------|
| PostgreSQL | 5432 | `localhost:5432` |
| Redis      | 6379 | `localhost:6379` |
| Kafka (내부용) | 9092 | Docker 내부 전용 (`kafka:9092`) |
| Kafka (외부용) | 9094 | `localhost:9094` ← Spring Boot 로컬 연결 시 사용 |
| Prometheus | 9090 | `http://localhost:9090` |
| Grafana    | 3000 | `http://localhost:3000` |
| HDFS Web UI | 9870 | `http://localhost:9870` |
| Spark Master Web UI | 18080 | `http://localhost:18080` |
| Spark Worker Web UI | 8081 | `http://localhost:8081` |
| Livy       | 8998 | `http://localhost:8998` |

### 9.4 전체 로컬 환경 초기화 후 재시작

> 컨테이너와 볼륨을 전부 삭제하고 처음부터 다시 시작할 때 사용합니다.

```bash
# ── Application Server 컨테이너 & 볼륨 전체 삭제 ──────────────────────────
docker compose -f infra/application-server/docker-compose.infra.yml \
               down -v

# ── 모니터링 컨테이너 & 볼륨 전체 삭제 ──────────────────────────────────
docker compose -f infra/application-server/docker-compose.monitoring.yml \
               -f infra/application-server/docker-compose.monitoring.local.yml \
               down -v

# ── Data Server 컨테이너 & 볼륨 전체 삭제 ────────────────────────────────
docker compose -f infra/data-server/docker-compose.hadoop.yml \
               -f infra/data-server/docker-compose.hadoop.local.yml \
               down -v

# ── Docker 네트워크 생성 (이미 존재하면 무시) ─────────────────────────────
docker network create app-network 2>/dev/null || true

# ── Application Server 실행 (PostgreSQL, Redis, Kafka) ───────────────────
docker compose -f infra/application-server/docker-compose.infra.yml \
               up -d postgresql redis kafka

# ── 모니터링 실행 (Prometheus, Grafana) ──────────────────────────────────
docker compose -f infra/application-server/docker-compose.monitoring.yml \
               -f infra/application-server/docker-compose.monitoring.local.yml \
               up -d

# ── Data Server 실행 (Hadoop, Spark, Livy) ───────────────────────────────
# 최초 실행 시 Livy 이미지 빌드로 약 1~2분 소요
docker compose -f infra/data-server/docker-compose.hadoop.yml \
               -f infra/data-server/docker-compose.hadoop.local.yml \
               up -d --build
```

---

## 부록: 환경변수 파일 위치 요약

| 파일 위치 | 관리 방법 | 주요 항목 |
|----------|---------|---------|
| `infra/application-server/.env` | 서버에 직접 생성 | `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`, `GRAFANA_ADMIN_PASSWORD` |
| `infra/data-server/.env` | 서버에 직접 생성 | `DATANODE_HOSTNAME=43.203.244.30` |
| `backend/.env` | Jenkins Credentials (`backend-env-file`) | DB, Redis, Kafka, OAuth2, JWT |
| `ai/.env` | Jenkins Credentials (`ai-env-file`) | DB, GMS API 키 |

> 모든 `.env` 파일은 `.gitignore`에 등록되어 있습니다. 절대 커밋하지 마세요.
