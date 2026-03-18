package com.e103.ohmyguide.global.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class TourApiClient {

    private final RestTemplate restTemplate;
    private final String serviceKey;
    private final String baseUrl;

    public TourApiClient(RestTemplate restTemplate,
                         @Value("${tour-api.service-key}") String serviceKey,
                         @Value("${tour-api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
    }

    public Optional<TourApiDetail> fetchDetail(int contentId) {
        String url = baseUrl + "/detailCommon2"
                + "?serviceKey=" + serviceKey
                + "&MobileOS=ETC"
                + "&MobileApp=OhMyGuide"
                + "&_type=json"
                + "&contentId=" + contentId
                + "&numOfRows=10"
                + "&pageNo=1";

        try {
            Map<String, Object> response = restTemplate.getForObject(URI.create(url), Map.class);
            if (response == null) return Optional.empty();

            Map<String, Object> responseBody = (Map<String, Object>) response.get("response");
            if (responseBody == null) return Optional.empty();

            Map<String, Object> body = (Map<String, Object>) responseBody.get("body");
            if (body == null) return Optional.empty();
            Object itemsObj = body.get("items");
            if (itemsObj == null || itemsObj instanceof String) return Optional.empty();

            Map<String, Object> items = (Map<String, Object>) itemsObj;
            Object itemObj = items.get("item");
            if (itemObj == null) return Optional.empty();

            Map<String, Object> item;
            if (itemObj instanceof List<?> list) {
                if (list.isEmpty()) return Optional.empty();
                item = (Map<String, Object>) list.get(0);
            } else {
                item = (Map<String, Object>) itemObj;
            }

            return Optional.of(new TourApiDetail(
                    getString(item, "title"),
                    getString(item, "addr1"),
                    getString(item, "addr2"),
                    getString(item, "tel"),
                    getBigDecimal(item, "mapx"),
                    getBigDecimal(item, "mapy"),
                    getString(item, "firstimage"),
                    getString(item, "firstimage2"),
                    getString(item, "homepage"),
                    getString(item, "overview")
            ));
        } catch (Exception e) {
            log.warn("Tour API 호출 실패 (contentId={}): {}", contentId, e.getMessage());
            return Optional.empty();
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        String str = value.toString().trim();
        return str.isEmpty() ? null : str;
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public record TourApiDetail(
            String title,
            String addr1,
            String addr2,
            String tel,
            BigDecimal longitude,
            BigDecimal latitude,
            String firstImage1,
            String firstImage2,
            String homepage,
            String overview
    ) {}
}
