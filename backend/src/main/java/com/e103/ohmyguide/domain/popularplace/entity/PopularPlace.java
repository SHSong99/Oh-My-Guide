package com.e103.ohmyguide.domain.popularplace.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity                                    // "이 클래스는 DB 테이블과 연결돼"
@Getter                                    // 모든 필드의 getter 자동 생성
@Table(name = "popular_places")            // 연결할 테이블 이름
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 필수: 빈 생성자 (외부에서 직접 생성 막음)
public class PopularPlace {

    @Id                                    // 이 필드가 기본 키(PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // DB가 자동으로 번호 매김
    private Long id;

    @Column(name = "nationality")          // DB 컬럼명 = nationality
    private String nationality;

    @Column(name = "age_group")            // DB는 snake_case, Java는 camelCase → 매핑 필요
    private String ageGroup;

    @Column(name = "gender")
    private String gender;

    @Column(name = "travel_purpose")
    private String travelPurpose;

    @Column(name = "place_id")
    private Long placeId;                  // Long 타입 → attractions 테이블의 ID와 매핑 가능

    @Column(name = "visit_count")
    private Long visitCount;               // 이 군집에서 이 장소를 본/간 횟수

    @Column(name = "total_score")
    private Long totalScore;               // VIEW(1) + GO(3) 등 가중치 합산 점수

    @Column(name = "place_rank")           // "rank"는 PostgreSQL 예약어라서 "place_rank"로!
    private Integer placeRank;             // 군집 내 순위 (1위, 2위, ...)

    @Builder
    private PopularPlace(String nationality, String ageGroup, String gender, String travelPurpose, Long placeId, Long visitCount, Long totalScore, Integer placeRank) {
        this.nationality = nationality;
        this.ageGroup = ageGroup;
        this.gender = gender;
        this.travelPurpose = travelPurpose;
        this.placeId = placeId;
        this.visitCount = visitCount;
        this.totalScore = totalScore;
        this.placeRank = placeRank;
    }
}