package com.e103.ohmyguide.global.init;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.global.api.TourApiClient;
import com.e103.ohmyguide.global.api.TourApiClient.TourApiDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Profile("data-fill")
@RequiredArgsConstructor
public class AttractionDataFiller implements ApplicationRunner {

    private static final int API_CALL_LIMIT = 997;

    private final AttractionRepository attractionRepository;
    private final TourApiClient tourApiClient;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("=== Attraction 이미지 결측치 채우기 시작 ===");

        List<Attraction> attractions = attractionRepository.findBusanGyeongnamAttractionsWithMissingData();

        log.info("대상 Attraction 수: {}", attractions.size());

        int processed = 0;
        int updated = 0;

        for (Attraction attraction : attractions) {
            if (processed >= API_CALL_LIMIT) {
                log.warn("API 호출 제한 {}건에 도달하여 중단합니다.", API_CALL_LIMIT);
                break;
            }

            var detailOpt = tourApiClient.fetchDetail(attraction.getContentId());
            processed++;

            if (detailOpt.isPresent()) {
                TourApiDetail detail = detailOpt.get();
                attraction.fillImageAndOverview(
                        detail.firstImage1(),
                        detail.firstImage2(),
                        detail.overview()
                );
                updated++;
            }

            if (processed % 100 == 0) {
                log.info("진행: {}/{} 호출, {} 업데이트", processed, Math.min(attractions.size(), API_CALL_LIMIT), updated);
            }
        }

        log.info("=== Attraction 결측치 채우기 완료: {}건 호출, {}건 업데이트 ===", processed, updated);
    }
}
