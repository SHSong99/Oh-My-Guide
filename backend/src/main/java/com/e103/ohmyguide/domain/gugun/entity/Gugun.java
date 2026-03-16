package com.e103.ohmyguide.domain.gugun.entity;

import com.e103.ohmyguide.domain.sido.entity.Sido;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "guguns",
        uniqueConstraints = @UniqueConstraint(columnNames = {"gugun_code", "sido_code"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gugun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guguns_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sido_code", referencedColumnName = "sido_code", nullable = false)
    private Sido sido;

    @Column(name = "gugun_code", nullable = false)
    private Integer gugunCode;

    @Column(name = "gugun_name", length = 20)
    private String gugunName;

    @Builder
    private Gugun(Sido sido, Integer gugunCode, String gugunName) {
        this.sido = sido;
        this.gugunCode = gugunCode;
        this.gugunName = gugunName;
    }
}
