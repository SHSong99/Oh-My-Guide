package com.e103.ohmyguide.domain.attractionvector.entity;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "attraction_vectors")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttractionVector extends BaseEntity {

    @Id
    @Column(name = "attr_id")
    private Integer attrId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "attr_id")
    private Attraction attraction;

    @Column(name = "attraction_vector", nullable = false, columnDefinition = "json")
    private String attractionVector;

    @Builder
    private AttractionVector(Attraction attraction, String attractionVector) {
        this.attraction = attraction;
        this.attractionVector = attractionVector;
    }

    public void updateAttractionVector(String attractionVector) {
        this.attractionVector = attractionVector;
    }
}
