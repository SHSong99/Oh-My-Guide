package com.e103.ohmyguide.domain.popularplace.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class SparkJobService {

    @Value("${spark.schedule.enabled:false}")       // 스케줄 활성화 여부 (기본: 꺼짐)
    private boolean scheduleEnabled;

    @Value("${livy.url}")                           // Livy REST API 주소
    private String livyUrl;

    @Value("${spark.job.app-resource}")             // analyze_logs.py 경로
    private String appResource;

    @Value("${spark.job.jars}")                     // PostgreSQL JDBC 드라이버 경로
    private String jars;

    @Value("${spark.job.db-host:}")                 // Spark용 DB 호스트 (SPARK_DB_HOST 환경변수)
    private String sparkDbHost;

    @Value("${spring.datasource.url}")              // DB 접속 URL
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    // 매일 새벽 4시에 자동 실행 (scheduleEnabled가 true일 때만)
    @Scheduled(cron = "${spark.schedule.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void scheduledAnalysis() {
        if (!scheduleEnabled) {
            return;                                  // 꺼져있으면 아무것도 안 함
        }
        log.info("=== Scheduled Spark analysis started ===");
        Map<String, Object> result = submitAnalysisJob();
        log.info("=== Scheduled Spark analysis finished: {} ===", result.get("status"));
    }

    // Livy REST API에 분석 작업 제출
    public Map<String, Object> submitAnalysisJob() {
        String submitUrl = livyUrl + "/batches";

        // SPARK_DB_HOST가 설정된 경우 우선 사용 (Data 서버에서 App 서버 DB에 접근하는 경우)
        // 미설정 시 Spring Boot JDBC URL에서 추출
        String dbHost = (sparkDbHost != null && !sparkDbHost.isBlank())
                ? sparkDbHost
                : resolveSparkDbHost(extractDbHost(dbUrl));
        String dbPort = extractDbPort(dbUrl);
        String dbName = extractDbName(dbUrl);

        // Livy 배치 요청 본문
        // args: Python 스크립트에 순서대로 전달 (sys.argv[1]~[5])
        Map<String, Object> requestBody = Map.of(
                "file", appResource,
                "args", new String[]{dbHost, dbPort, dbName, dbUsername, dbPassword},
                "conf", Map.of(
                        "spark.jars", jars
                )
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(submitUrl, entity, Map.class);
            log.info("Spark job submitted successfully: {}", response.getBody());
            return Map.of(
                    "status", "submitted",
                    "livyResponse", response.getBody() != null ? response.getBody() : Map.of()
            );
        } catch (Exception e) {
            log.error("Failed to submit Spark job", e);
            return Map.of(
                    "status", "failed",
                    "error", e.getMessage()
            );
        }
    }

    // 로컬 개발 시 localhost → host.docker.internal 변환
    // (Spark 컨테이너 내부에서 localhost는 컨테이너 자신을 가리키므로)
    private String resolveSparkDbHost(String host) {
        if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
            return "host.docker.internal";
        }
        return host;
    }

    // jdbc:postgresql://localhost:5432/ohmyguide 에서 localhost 추출
    private String extractDbHost(String jdbcUrl) {
        String afterProtocol = jdbcUrl.split("//")[1];
        return afterProtocol.split(":")[0];
    }

    // 5432 추출
    private String extractDbPort(String jdbcUrl) {
        String afterProtocol = jdbcUrl.split("//")[1];
        String hostPort = afterProtocol.split("/")[0];
        return hostPort.contains(":") ? hostPort.split(":")[1] : "5432";
    }

    // ohmyguide 추출
    private String extractDbName(String jdbcUrl) {
        String afterProtocol = jdbcUrl.split("//")[1];
        return afterProtocol.split("/")[1].split("\\?")[0];
    }
}
