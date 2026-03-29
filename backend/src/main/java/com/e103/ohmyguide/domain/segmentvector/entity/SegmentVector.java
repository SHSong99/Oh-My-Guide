package com.e103.ohmyguide.domain.segmentvector.entity;

import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "segment_vectors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"segment_type", "segment_key"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SegmentVector extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "segment_type", length = 20, nullable = false)
    private String segmentType;

    @Column(name = "segment_key", length = 50, nullable = false)
    private String segmentKey;

    @Column(name = "segment_vector", nullable = false, columnDefinition = "json")
    private String segmentVector;

    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "sample_count")
    private Integer sampleCount;

    @Builder
    private SegmentVector(String segmentType, String segmentKey,
                          String segmentVector, String source, Integer sampleCount) {
        this.segmentType = segmentType;
        this.segmentKey = segmentKey;
        this.segmentVector = segmentVector;
        this.source = source != null ? source : "csv";
        this.sampleCount = sampleCount != null ? sampleCount : 0;
    }
}
