# 인프라 구성 정보

> 최종 업데이트: 2026-03-20

---

## 전체 아키텍처 개요

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
│  [app-network] Docker Bridge Network                 │
│  Spring Boot ─── PostgreSQL                          │
│  Spring Boot ─── Redis                               │
│  Spring Boot ─── Kafka                               │
│  Prometheus ──── Spring Boot / PostgreSQL / Kafka    │
└─────────────────────────────────────────────────────┘
    │
    │  서버 간 통신 (내부 IP)
    │
    ▼
┌─────────────────────────────────────────────────────┐
│              Data Server (43.203.244.30)             │
│                j14e103.p.ssafy.io                    │
│                                                      │
│  [data-network] Docker Bridge Network                │
│  NameNode ─── DataNode                               │
│  Spark Master ─── Spark Worker                       │
│  Livy ─── Spark Master                               │
└─────────────────────────────────────────────────────┘
```

---

## 서버 정보

| 구분             | Application Server          | Data Server                 |
|-----------------|-----------------------------|-----------------------------|
| 공인 IP          | `43.200.1.94`               | `43.203.244.30`             |
| 도메인           | `j14e103a.p.ssafy.io`       | `j14e103.p.ssafy.io`        |
| 역할             | Spring Boot, AI, 모니터링   | Hadoop, Spark, Livy         |

---

## Application Server (43.200.1.94)

### UFW 방화벽 규칙

| 포트       | 허용 대상          | 설명                              |
|-----------|-------------------|-----------------------------------|
| 22        | Anywhere          | SSH                               |
| 80/tcp    | Anywhere          | HTTP (HTTPS 리다이렉트)           |
| 443       | Anywhere          | HTTPS (Nginx SSL)                 |
| 5432      | 43.203.244.30     | PostgreSQL (Data Server만 허용)   |

### Docker 네트워크

| 네트워크명    | 타입          | 설명                                      |
|-------------|--------------|-------------------------------------------|
| app-network | bridge        | 모든 app server 컨테이너가 공유하는 네트워크 |

### 컨테이너 목록

#### Nginx
| 항목          | 값                          |
|--------------|----------------------------|
| Image        | `nginx:latest`              |
| Container    | `nginx`                     |
| 포트 (호스트) | `80:80`, `443:443`          |
| 네트워크      | app-network                 |
| 볼륨          | `./nginx/conf.d`, `./certbot/conf`, `./certbot/www` |

**라우팅 규칙:**
| 외부 경로     | 내부 프록시 대상              |
|-------------|------------------------------|
| `/api/`     | `http://spring-boot:8080/`   |
| `/ai/`      | `http://fastapi:8000/`       |
| `/jenkins/` | `http://jenkins:8080/jenkins/` |
| `/grafana/` | `http://grafana:3000`        |

---

#### Spring Boot
| 항목          | 값                              |
|--------------|--------------------------------|
| Image        | `oh-my-guide-backend:latest`   |
| Container    | `spring-boot`                  |
| 포트 (호스트) | 없음 (Nginx 내부망으로만 접근) |
| 네트워크      | app-network                    |
| env_file     | `backend/.env`                 |
| Health Check | `GET /actuator/health`         |

---

#### FastAPI
| 항목          | 값                          |
|--------------|----------------------------|
| Image        | `oh-my-guide-ai:latest`     |
| Container    | `fastapi`                   |
| 포트 (호스트) | 없음 (Nginx 내부망으로만 접근) |
| 네트워크      | app-network                 |
| env_file     | `ai/.env`                   |
| Health Check | `GET /health`               |

---

#### Jenkins
| 항목          | 값                                         |
|--------------|-------------------------------------------|
| Image        | Custom build (Dockerfile)                  |
| Container    | `jenkins`                                  |
| 포트 (호스트) | `127.0.0.1:8989:8080` (루프백만, 외부 차단) |
| 포트 (Agent) | `50000:50000`                             |
| 네트워크      | app-network                               |
| 볼륨          | `./jenkins_home`, `/var/run/docker.sock` (DooD) |
| 접속 경로     | `https://j14e103a.p.ssafy.io/jenkins/`   |

---

#### PostgreSQL
| 항목          | 값                        |
|--------------|--------------------------|
| Image        | `postgis/postgis:16-3.4` |
| Container    | `postgresql`              |
| 포트 (호스트) | `5432:5432`               |
| 네트워크      | app-network               |
| 볼륨          | `postgres_data`           |
| env_file     | `application-server/.env` |
| Health Check | `pg_isready`              |

---

#### Redis
| 항목          | 값              |
|--------------|----------------|
| Image        | `redis:alpine` |
| Container    | `redis`         |
| 포트 (호스트) | 없음 (내부망만) |
| 포트 (내부)  | `6379`          |
| 네트워크      | app-network     |
| 볼륨          | `redis_data`    |
| Health Check | `redis-cli ping` |

---

#### Kafka
| 항목          | 값                        |
|--------------|--------------------------|
| Image        | `apache/kafka:3.9.0`     |
| Container    | `kafka`                   |
| 포트 (호스트) | 없음 (내부망만)           |
| 포트 (내부)  | `9092` (PLAINTEXT), `9093` (CONTROLLER) |
| 네트워크      | app-network               |
| 볼륨          | `kafka_data`              |
| 모드          | KRaft (ZooKeeper 없음)    |
| Health Check | `/opt/kafka/bin/kafka-topics.sh --list` |

---

#### Prometheus
| 항목          | 값                        |
|--------------|--------------------------|
| Image        | `prom/prometheus:latest` |
| Container    | `prometheus`              |
| 포트 (호스트) | `9090:9090`               |
| 네트워크      | app-network               |
| 볼륨          | `./prometheus/prometheus.yml`, `prometheus-data` |

**Scrape 대상:**
| Job                | 대상                        | Metrics 경로              |
|-------------------|-----------------------------|--------------------------|
| spring-backend    | `spring-boot:8080`          | `/actuator/prometheus`   |
| postgres          | `postgres-exporter:9187`    | `/metrics`               |
| node              | `node-exporter:9100`        | `/metrics`               |
| kafka             | `kafka-exporter:9308`       | `/metrics`               |

---

#### Grafana
| 항목          | 값                          |
|--------------|----------------------------|
| Image        | `grafana/grafana:latest`   |
| Container    | `grafana`                   |
| 포트 (호스트) | `3000:3000`                 |
| 네트워크      | app-network                 |
| 접속 경로     | `https://j14e103a.p.ssafy.io/grafana/` |

---

#### Certbot
| 항목     | 값                        |
|---------|--------------------------|
| Image   | `certbot/certbot`        |
| 역할     | Let's Encrypt 인증서 자동 갱신 (12시간 주기) |
| 볼륨     | `./certbot/conf`, `./certbot/www` |

---

### Exporter 컨테이너 (내부망 전용)

| Container          | Image                                      | 포트 (내부) | 수집 대상        |
|-------------------|--------------------------------------------|------------|-----------------|
| postgres-exporter | `prometheuscommunity/postgres-exporter`    | 9187       | PostgreSQL 메트릭 |
| node-exporter     | `prom/node-exporter:latest`                | 9100       | 호스트 시스템 메트릭 |
| kafka-exporter    | `danielqsj/kafka-exporter:latest`          | 9308       | Kafka 메트릭     |

---

## Data Server (43.203.244.30)

### UFW 방화벽 규칙

| 포트       | 허용 대상       | 설명                              |
|-----------|----------------|-----------------------------------|
| 22        | Anywhere       | SSH                               |
| 9870/tcp  | Anywhere       | HDFS NameNode Web UI              |
| 8081/tcp  | Anywhere       | Spark Worker Web UI               |
| 18080/tcp | Anywhere       | Spark Master Web UI               |
| 9000/tcp  | 43.200.1.94    | HDFS RPC (App Server만 허용)      |
| 8020/tcp  | 43.200.1.94    | HDFS IPC (App Server만 허용)      |
| 7077/tcp  | 43.200.1.94    | Spark 클러스터 통신               |
| 6066/tcp  | 43.200.1.94    | Spark REST API                    |
| 8998/tcp  | 43.200.1.94    | Livy REST API (App Server만 허용) |
| 9864/tcp  | 43.200.1.94    | DataNode HTTP (App Server만 허용) |
| 9866/tcp  | 43.200.1.94    | DataNode 데이터 전송              |
| 9867/tcp  | 43.200.1.94    | DataNode IPC                      |

### Docker 네트워크

| 네트워크명    | 타입   | 설명                                      |
|-------------|--------|-------------------------------------------|
| data-network | bridge | 모든 data server 컨테이너가 공유하는 네트워크 |

### 컨테이너 목록

#### NameNode (HDFS)
| 항목          | 값                        |
|--------------|--------------------------|
| Image        | `apache/hadoop:3`        |
| Container    | `namenode`                |
| Hostname     | `namenode`                |
| 포트 (호스트) | `9870:9870`, `9000:9000`, `8020:8020` |
| 네트워크      | data-network              |
| 볼륨          | `namenode-data:/opt/hadoop/data/nameNode`, `./hadoop-config` |
| 역할          | HDFS 메타데이터 관리 (파일명, 블록 위치) |
| Health Check | `nc -z localhost 9870`    |

---

#### DataNode (HDFS)
| 항목          | 값                              |
|--------------|--------------------------------|
| Image        | `apache/hadoop:3`              |
| Container    | `datanode`                     |
| Hostname     | `datanode`                     |
| 포트 (호스트) | `9864:9864`, `9866:9866`, `9867:9867` |
| 네트워크      | data-network                   |
| 볼륨          | `datanode-data:/opt/hadoop/data/dataNode`, `./hadoop-config` |
| 역할          | 실제 데이터 블록 저장           |
| Health Check | `nc -z localhost 9864`         |
| 환경변수      | `DATANODE_HOSTNAME` (`.env`에서 주입) |

> **주의**: `DATANODE_HOSTNAME`은 외부 클라이언트(Spring Boot)에게 광고할 IP.
> - 운영: `43.203.244.30` / 로컬: `localhost`

---

#### Spark Master
| 항목          | 값                          |
|--------------|----------------------------|
| Image        | `apache/spark:3.5.3`       |
| Container    | `spark-master`              |
| Hostname     | `spark-master`              |
| 포트 (호스트) | `18080:18080`, `7077:7077`, `6066:6066` |
| 네트워크      | data-network                |
| 볼륨          | `./spark-jobs:/opt/spark-jobs` |
| Health Check | `curl -sf http://localhost:18080` |

---

#### Spark Worker
| 항목          | 값                          |
|--------------|----------------------------|
| Image        | `apache/spark:3.5.3`       |
| Container    | `spark-worker`              |
| Hostname     | `spark-worker`              |
| 포트 (호스트) | `8081:8081`                 |
| 네트워크      | data-network                |
| 리소스        | Memory: 2g, Cores: 2        |
| Health Check | `curl -sf http://localhost:8081` |

---

#### Livy
| 항목          | 값                                            |
|--------------|----------------------------------------------|
| Image        | Custom build (`apache/spark:3.5.3` 기반)     |
| Container    | `livy`                                        |
| Hostname     | `livy`                                        |
| 포트 (호스트) | `8998:8998`                                  |
| 네트워크      | data-network                                  |
| 볼륨          | `./spark-jobs:/opt/spark-jobs`, `./hadoop-config`, `./livy/livy.conf` |
| 역할          | Spark REST API 서버 — Spring Boot에서 HTTP로 Spark Job 제출 |

**livy.conf 설정:**
| 설정 키                        | 값                        |
|-------------------------------|--------------------------|
| `livy.spark.master`           | `spark://spark-master:7077` |
| `livy.spark.deployMode`       | `client`                  |
| `livy.server.port`            | `8998`                    |
| `livy.file.local-dir-whitelist` | `/opt/spark-jobs`       |
| `livy.repl.enable-hive-context` | `false`                 |

---

### Hadoop 설정 파일

#### core-site.xml
| 설정 키          | 값                        |
|----------------|--------------------------|
| `fs.defaultFS` | `hdfs://namenode:9000`   |
| `hadoop.tmp.dir` | `/opt/hadoop/data/tmp` |

#### hdfs-site.xml
| 설정 키                              | 값                              |
|-------------------------------------|---------------------------------|
| `dfs.replication`                   | `1`                             |
| `dfs.namenode.name.dir`             | `/opt/hadoop/data/nameNode`     |
| `dfs.datanode.data.dir`             | `/opt/hadoop/data/dataNode`     |
| `dfs.client.use.datanode.hostname`  | `false`                         |
| `dfs.datanode.hostname`             | `${env.DATANODE_HOSTNAME}`      |
| `dfs.datanode.http.address`         | `0.0.0.0:9864`                  |
| `dfs.datanode.address`              | `0.0.0.0:9866`                  |
| `dfs.datanode.ipc.address`          | `0.0.0.0:9867`                  |
| `dfs.permissions.enabled`           | `false`                         |
| `dfs.namenode.safemode.threshold-pct` | `0.0`                        |

---

## 서버 간 통신 흐름

### Spring Boot → HDFS 로그 적재
```
Spring Boot (app server: 43.200.1.94)
    │
    │ 1. NameNode RPC (9000): "파일 어디에 저장?"
    ▼
NameNode (data server: 43.203.244.30:9000)
    │
    │ 2. "DataNode 43.203.244.30:9866에 써"
    ▼
Spring Boot
    │
    │ 3. DataNode 직접 연결 (9866): 파일 쓰기
    ▼
DataNode (data server: 43.203.244.30:9866)
```

### Spring Boot → Spark Job 트리거
```
Spring Boot (app server)
    │
    │ POST http://43.203.244.30:8998/batches
    ▼
Livy REST API (data server: 43.203.244.30:8998)
    │
    │ spark://spark-master:7077
    ▼
Spark Master → Spark Worker → Job 실행
```

### 외부 사용자 → API 접근
```
Android 앱 / Browser
    │
    │ HTTPS (443)
    ▼
Nginx (j14e103a.p.ssafy.io)
    ├── /api/      → Spring Boot:8080
    ├── /ai/       → FastAPI:8000
    ├── /jenkins/  → Jenkins:8080
    └── /grafana/  → Grafana:3000
```

---

## 포트 요약

### Application Server (43.200.1.94)

| 포트  | 서비스          | 외부 접근              |
|------|----------------|----------------------|
| 22   | SSH            | 전체 허용             |
| 80   | Nginx HTTP     | 전체 허용 (HTTPS 리다이렉트) |
| 443  | Nginx HTTPS    | 전체 허용             |
| 5432 | PostgreSQL     | Data Server만 허용    |
| 8989 | Jenkins        | 루프백 전용 (Nginx 경유) |
| 9090 | Prometheus     | 내부망 전용            |
| 3000 | Grafana        | 내부망 전용 (Nginx 경유) |

### Data Server (43.203.244.30)

| 포트  | 서비스                | 외부 접근              |
|------|----------------------|----------------------|
| 22   | SSH                  | 전체 허용             |
| 9870 | HDFS Web UI          | 전체 허용 (모니터링용)  |
| 9000 | HDFS RPC             | App Server만 허용     |
| 8020 | HDFS IPC             | App Server만 허용     |
| 9864 | DataNode HTTP        | App Server만 허용     |
| 9866 | DataNode 데이터 전송  | App Server만 허용     |
| 9867 | DataNode IPC         | App Server만 허용     |
| 18080 | Spark Master Web UI | 전체 허용 (모니터링용)  |
| 7077 | Spark 클러스터 통신   | App Server만 허용     |
| 6066 | Spark REST API       | App Server만 허용     |
| 8081 | Spark Worker Web UI  | 전체 허용 (모니터링용)  |
| 8998 | Livy REST API        | App Server만 허용     |

---

## 환경변수 파일 (.env)

| 위치                                  | 필요한 변수                                             |
|--------------------------------------|--------------------------------------------------------|
| `infra/application-server/.env`      | `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`, `GRAFANA_ADMIN_PASSWORD` |
| `infra/data-server/.env`             | `DATANODE_HOSTNAME` (운영: `43.203.244.30` / 로컬: `localhost`) |
| `backend/.env`                       | Spring Boot 환경변수                                   |
| `ai/.env`                            | FastAPI 환경변수                                       |

> 모든 `.env` 파일은 `.gitignore`에 등록되어 있습니다.

---

## Spark Jobs 디렉토리

위치: `infra/data-server/spark-jobs/`

| 파일                | 설명                              |
|--------------------|----------------------------------|
| `hello_job.py`     | Livy 동작 확인용 테스트 Job        |
| `hdfs_test_job.py` | HDFS 연결 확인용 테스트 Job        |

> Jenkins가 `infra/data-server/spark-jobs/` 변경 감지 시 data server로 자동 배포.

---

## CI/CD (Jenkins)

Jenkins 파이프라인은 GitLab master 브랜치 MR 승인 시 자동 트리거됩니다.

### Data Server 파이프라인 (`infra/data-server/Jenkinsfile`)

| 변경 경로                              | 실행 Stage          | 동작                              |
|--------------------------------------|--------------------|------------------------------------|
| `infra/data-server/spark-jobs/**`    | Deploy Spark Jobs  | rsync로 spark-jobs/ 동기화         |
| `infra/data-server/livy/**`          | Deploy Infra       | rsync + `docker compose up --build` |
| `infra/data-server/docker-compose.hadoop.yml` | Deploy Infra | rsync + `docker compose up --build` |
| `infra/data-server/hadoop-config/**` | Deploy Infra       | rsync + `docker compose up --build` |
