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

    @Value("${spark.master.rest-url}")              // Spark Master REST API 주소
    private String sparkRestUrl;

    @Value("${spark.job.app-resource}")             // analyze_logs.py 경로
    private String appResource;

    @Value("${spark.job.jars}")                     // PostgreSQL JDBC 드라이버 경로
    private String jars;

    @Value("${spring.datasource.url}")              // DB 접속 URL
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final RestTemplate restTemplate = new RestTemplate();

    // 매일 새벽 4시에 자동 실행 (scheduleEnabled가 true일 때만)
    @Scheduled(cron = "${spark.schedule.cron:0 0 4 * * *}")
    public void scheduledAnalysis() {
        if (!scheduleEnabled) {
            return;                                  // 꺼져있으면 아무것도 안 함
        }
        log.info("=== Scheduled Spark analysis started ===");
        Map<String, Object> result = submitAnalysisJob();
        log.info("=== Scheduled Spark analysis finished: {} ===", result.get("status"));
    }

    // Spark REST API에 분석 작업 제출
    public Map<String, Object> submitAnalysisJob() {
        String submitUrl = sparkRestUrl + "/v1/submissions/create";

        // DB 접속 URL에서 host, port, dbname을 추출
        String dbHost = extractDbHost(dbUrl);
        String dbPort = extractDbPort(dbUrl);
        String dbName = extractDbName(dbUrl);

        // Spark REST API 요청 본문
        Map<String, Object> requestBody = Map.of(
                "appResource", appResource,
                "sparkProperties", Map.of(
                        "spark.master", "spark://spark-master:7077",
                        "spark.app.name", "TravelLogAnalysis",
                        "spark.jars", jars
                ),
                "clientSparkVersion", "3.5.0",
                "mainClass", "org.apache.spark.deploy.PythonRunner",
                "environmentVariables", Map.of(         // Spark에 DB 정보를 환경변수로 전달
                        "DB_HOST", dbHost,
                        "DB_PORT", dbPort,
                        "DB_NAME", dbName,
                        "DB_USER", dbUsername,
                        "DB_PASSWORD", dbPassword
                ),
                "action", "CreateSubmissionRequest",
                "appArgs", new String[]{appResource}
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(submitUrl, entity, Map.class);
            log.info("Spark job submitted successfully: {}", response.getBody());
            return Map.of(
                    "status", "submitted",
                    "sparkResponse", response.getBody() != null ? response.getBody() : Map.of()
            );
        } catch (Exception e) {
            log.error("Failed to submit Spark job", e);
            return Map.of(
                    "status", "failed",
                    "error", e.getMessage()
            );
        }
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