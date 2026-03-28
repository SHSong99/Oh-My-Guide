package com.e103.ohmyguide.domain.popularplace.controller;

import com.e103.ohmyguide.domain.popularplace.dto.PopularPlaceResponse;
import com.e103.ohmyguide.domain.popularplace.service.PopularPlaceService;
import com.e103.ohmyguide.domain.popularplace.service.SparkJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pickRecommend")       // 이 컨트롤러의 기본 URL
@RequiredArgsConstructor
public class PopularPlaceController {

    private final PopularPlaceService popularPlaceService;
    private final SparkJobService sparkJobService;

    // GET /api/pickRecommend?nationality=KR&age=25&...
    // → 나와 비슷한 여행자들의 인기 장소 반환
    @GetMapping
    public ResponseEntity<List<PopularPlaceResponse>> getRecommendations(
            @RequestParam String nationality,
            @RequestParam int age,
            @RequestParam String gender,
            @RequestParam String travelPurpose
    ) {
        List<PopularPlaceResponse> results = popularPlaceService.getRecommendations(
                nationality, age, gender, travelPurpose
        );
        return ResponseEntity.ok(results);
    }

    // POST /api/pickRecommend/calculate
    // → Spark 분석 작업 실행 (백엔드 전용)
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> triggerSparkAnalysis() {
        Map<String, Object> result = sparkJobService.submitAnalysisJob();
        if ("submitted".equals(result.get("status"))) {
            return ResponseEntity.accepted().body(result);     // 202: 접수됨
        }
        return ResponseEntity.internalServerError().body(result);  // 500: 실패
    }
}