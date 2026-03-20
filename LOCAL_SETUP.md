# 로컬 개발 환경 설정 가이드

## 전체 구조

```
infra/
├── application-server/   # PostgreSQL, Redis, Kafka
└── data-server/          # Spark, Livy
```

---

## 1. Application Server (PostgreSQL, Redis, Kafka)

### 위치
```
infra/application-server/
```

### 사전 준비: `.env` 파일 생성
`infra/application-server/.env` 파일을 생성합니다.

```env
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=your_db
```

### 실행
```bash
cd infra/application-server

# app-network 생성 (최초 1회)
docker network create app-network

# 로컬 override 파일을 함께 사용 (Redis 6379, Kafka 9092 포트 외부 노출)
docker compose -f docker-compose.infra.yml -f docker-compose.infra.local.yml up -d postgresql redis kafka
```

### 서비스 포트

| 서비스     | 포트 | 설명            |
|-----------|------|----------------|
| PostgreSQL | 5432 | DB 접속         |
| Redis      | 6379 | 캐시            |
| Kafka      | 9092 | 메시지 브로커    |

### 동작 확인
```bash
# PostgreSQL 연결 확인
docker exec postgresql pg_isready
# 출력 예시: /var/run/postgresql:5432 - accepting connections

# Redis 연결 확인
docker exec redis redis-cli ping
# 출력 예시: PONG

# Kafka 연결 확인 (topic 목록 조회)
docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
# 출력 예시: (등록된 topic 목록 또는 빈 출력)
```

### 종료
```bash
docker compose -f docker-compose.infra.yml -f docker-compose.infra.local.yml down
```

---

## 2. Data Server (Hadoop, Spark, Livy)

### 위치
```
infra/data-server/
```

### 사전 준비: `.env` 파일 생성
`infra/data-server/.env` 파일을 생성합니다.

```env
DATANODE_HOSTNAME=localhost
```

### 실행
```bash
cd infra/data-server

docker compose -f docker-compose.hadoop.yml -f docker-compose.hadoop.local.yml up -d --build
```

> 최초 실행 시 Livy 이미지 빌드로 시간이 걸립니다. (약 1~2분)

### 서비스 포트

| 서비스        | 포트  | 설명                        |
|--------------|-------|-----------------------------|
| NameNode     | 9870  | HDFS Web UI                 |
| NameNode     | 9000  | HDFS RPC (Spring Boot 접근) |
| NameNode     | 8020  | HDFS IPC                    |
| DataNode     | 9864  | DataNode HTTP               |
| DataNode     | 9866  | DataNode 데이터 전송        |
| DataNode     | 9867  | DataNode IPC                |
| Spark Master | 18080 | Spark Web UI                |
| Spark Master | 7077  | 클러스터 통신               |
| Spark Worker | 8081  | Worker Web UI               |
| Livy         | 8998  | REST API (Job 제출용)       |

### 동작 확인
```bash
# NameNode 확인
curl -s "http://localhost:9870/webhdfs/v1/?op=LISTSTATUS"
# 출력 예시: {"FileStatuses":{"FileStatus":[...]}}

# DataNode 확인 (1개 DataNode가 보고되면 정상)
docker exec namenode hdfs dfsadmin -report | grep -A3 "Live datanodes"
# 출력 예시: Live datanodes (1): ... Hostname: localhost

# Spark Master 확인
curl -s http://localhost:18080 | grep -o "Spark Master"
# 출력 예시: Spark Master

# Spark Worker 확인
curl -s http://localhost:8081 | grep -o "Spark Worker"
# 출력 예시: Spark Worker

# Livy 확인
curl -s http://localhost:8998/batches
# 출력 예시: {"from":0,"total":0,"batches":[]}

# HDFS 파일 쓰기 테스트
curl -s -L -X PUT "http://localhost:9870/webhdfs/v1/test.txt?op=CREATE&overwrite=true" \
  -H "Content-Type: application/octet-stream" \
  --data-binary "hello hdfs"

# HDFS 파일 읽기 테스트
curl -s -L "http://localhost:9870/webhdfs/v1/test.txt?op=OPEN"

# Spark Job 제출 테스트 (로컬 파일)
curl -X POST http://localhost:8998/batches \
  -H "Content-Type: application/json" \
  -d '{"file": "file:///opt/spark-jobs/hello_job.py"}'

# Spark Job 제출 테스트 (HDFS 파일)
curl -X POST http://localhost:8998/batches \
  -H "Content-Type: application/json" \
  -d '{"file": "file:///opt/spark-jobs/hdfs_test_job.py"}'

# Job 상태 확인 (id는 응답에서 확인)
curl http://localhost:8998/batches/0
```

### Job 상태값
| 상태       | 설명          |
|-----------|---------------|
| starting  | 시작 중        |
| running   | 실행 중        |
| success   | 성공           |
| dead      | 실패           |

### 종료
```bash
docker compose -f docker-compose.hadoop.yml -f docker-compose.hadoop.local.yml down
```

---

## 3. Spring Boot에서 Livy 호출

```java
// Job 제출
Map<String, String> body = Map.of("file", "file:///opt/spark-jobs/hello_job.py");
ResponseEntity<Map> response = restTemplate.postForEntity(
    "http://localhost:8998/batches",
    body,
    Map.class
);
int batchId = (int) response.getBody().get("id");

// 상태 확인
ResponseEntity<Map> status = restTemplate.getForEntity(
    "http://localhost:8998/batches/" + batchId,
    Map.class
);
String state = (String) status.getBody().get("state");
```

---

## 4. 전체 로컬 환경 초기화 후 재시작

> 프로젝트 루트(`S14P21E103/`)에서 실행합니다.

```bash
# ── Application Server 컨테이너 & 볼륨 전체 삭제 ──────────────────────────
docker compose -f infra/application-server/docker-compose.infra.yml \
               -f infra/application-server/docker-compose.infra.local.yml \
               down -v

# ── Data Server 컨테이너 & 볼륨 전체 삭제 ────────────────────────────────
docker compose -f infra/data-server/docker-compose.hadoop.yml \
               -f infra/data-server/docker-compose.hadoop.local.yml \
               down -v

# ── Docker 네트워크 생성 (이미 존재하면 무시) ─────────────────────────────
docker network create app-network 2>/dev/null || true

# ── Application Server 실행 (PostgreSQL, Redis, Kafka) ───────────────────
# .env 파일이 infra/application-server/.env 에 있어야 합니다
docker compose -f infra/application-server/docker-compose.infra.yml \
               -f infra/application-server/docker-compose.infra.local.yml \
               up -d postgresql redis kafka

# ── Data Server 실행 (Hadoop, Spark, Livy) ───────────────────────────────
# 최초 실행 시 Livy 이미지 빌드로 약 1~2분 소요
docker compose -f infra/data-server/docker-compose.hadoop.yml \
               -f infra/data-server/docker-compose.hadoop.local.yml \
               up -d --build
```
