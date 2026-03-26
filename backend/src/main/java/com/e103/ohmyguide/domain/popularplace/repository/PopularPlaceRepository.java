package com.e103.ohmyguide.domain.popularplace.repository;

import com.e103.ohmyguide.domain.popularplace.entity.PopularPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PopularPlaceRepository extends JpaRepository<PopularPlace, Long> {

    // JPQL: Java 객체 기준으로 쿼리 작성 (SQL과 비슷하지만 테이블명 대신 클래스명 사용)
    @Query("SELECT p FROM PopularPlace p " +
            "WHERE p.nationality = :nationality " +
            "AND p.ageGroup = :ageGroup " +
            "AND p.gender = :gender " +
            "AND p.travelPurpose = :travelPurpose " +
            "ORDER BY p.placeRank ASC " +
            "LIMIT 5")                        // 최대 5개만 반환
    List<PopularPlace> findByCluster(
            @Param("nationality") String nationality,     // :nationality 자리에 이 값이 들어감
            @Param("ageGroup") String ageGroup,
            @Param("gender") String gender,
            @Param("travelPurpose") String travelPurpose
    );
}