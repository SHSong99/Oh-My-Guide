package com.e103.ohmyguide.domain.attraction.repository;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.guide.dto.NearbyPlaceResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    @Query("SELECT a FROM Attraction a WHERE a.latitude BETWEEN :minLat AND :maxLat AND a.longitude BETWEEN :minLng AND :maxLng AND a.id <> :excludeId")
    List<Attraction> findWithinBoundingBox(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng, Long excludeId);

    // 경량 조회: placeId·좌표만 SELECT (overview 등 미로딩) → 메시지 크기/DB 부하 감소
    @Query("SELECT new com.e103.ohmyguide.domain.guide.dto.NearbyPlaceResponse(a.id, a.latitude, a.longitude) " +
           "FROM Attraction a WHERE a.latitude BETWEEN :minLat AND :maxLat AND a.longitude BETWEEN :minLng AND :maxLng AND a.id <> :excludeId")
    List<NearbyPlaceResponse> findNearbyPlacesWithinBoundingBox(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng, Long excludeId);

    @Query("SELECT a FROM Attraction a WHERE a.addr1 LIKE '서울특별시%' AND a.contentType.contentTypeId = 12 AND a.contentId IS NOT NULL AND (a.firstImage1 = '' OR a.overview = '')")
    List<Attraction> findSeoulAttractionsWithMissingData();

    @Query("SELECT a FROM Attraction a WHERE a.addr1 LIKE '경기도%' AND a.contentType.contentTypeId = 12 AND a.contentId IS NOT NULL AND (a.firstImage1 = '' OR a.overview = '')")
    List<Attraction> findGyeonggiAttractionsWithMissingData();

    @Query("SELECT a FROM Attraction a WHERE a.addr1 LIKE '인천광역시%' AND a.contentType.contentTypeId = 12 AND a.contentId IS NOT NULL AND (a.firstImage1 = '' OR a.overview = '')")
    List<Attraction> findIncheonAttractionsWithMissingData();

    @Query("SELECT a FROM Attraction a WHERE (a.addr1 LIKE '경기도%' OR a.addr1 LIKE '서울특별시%' OR a.addr1 LIKE '인천%') AND a.contentType.contentTypeId = 39 AND a.contentId IS NOT NULL AND (a.firstImage1 = '' OR a.overview = '')")
    List<Attraction> findFoodAttractionsWithMissingData();

    @Query("SELECT a FROM Attraction a WHERE (a.addr1 LIKE '경기도%' OR a.addr1 LIKE '서울특별시%' OR a.addr1 LIKE '인천%') AND a.contentType.contentTypeId = 14 AND a.contentId IS NOT NULL AND (a.firstImage1 = '' OR a.overview = '')")
    List<Attraction> findCultureAttractionsWithMissingData();

    @Query("SELECT a FROM Attraction a WHERE (a.addr1 LIKE '경기도%' OR a.addr1 LIKE '서울특별시%' OR a.addr1 LIKE '인천%') AND a.contentType.contentTypeId = 38 AND a.contentId IS NOT NULL AND (a.firstImage1 = '' OR a.overview = '')")
    List<Attraction> findShoppingAttractionsWithMissingData();

    @Query("SELECT a FROM Attraction a WHERE (a.addr1 LIKE '경기도%' OR a.addr1 LIKE '서울특별시%' OR a.addr1 LIKE '인천광역시%') AND a.contentType.contentTypeId = 12 AND a.contentId IS NOT NULL AND a.overview IS NULL")
    List<Attraction> findTourAttractionsWithNullOverview();

    @Query("SELECT a FROM Attraction a WHERE (a.addr1 LIKE '경기도%' OR a.addr1 LIKE '서울특별시%' OR a.addr1 LIKE '인천광역시%') AND a.contentType.contentTypeId = 14 AND a.contentId IS NOT NULL AND a.overview IS NULL")
    List<Attraction> findCultureAttractionsWithNullOverview();

    @Query("SELECT a FROM Attraction a WHERE a.contentType.contentTypeId IN (12, 14, 38, 39) AND a.contentId IS NOT NULL AND (a.overview IS NULL OR a.firstImage1 = '')")
    List<Attraction> findAttractionsWithMissingOverviewOrImage();

    @Query("SELECT a FROM Attraction a WHERE (a.addr1 LIKE '경상남도%' OR a.addr1 LIKE '부산%') AND a.contentType.contentTypeId IN (12, 14, 38, 39) AND a.contentId IS NOT NULL AND (a.firstImage1 = '' OR a.overview IS NULL)")
    List<Attraction> findBusanGyeongnamAttractionsWithMissingData();
}
