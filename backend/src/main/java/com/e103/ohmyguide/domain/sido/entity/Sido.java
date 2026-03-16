package com.e103.ohmyguide.domain.sido.entity;

import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "sidos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sido extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sidos_id")
    private Long id;

    @Column(name = "sido_code", nullable = false, unique = true)
    private Integer sidoCode;

    @Column(name = "sido_name", length = 20)
    private String sidoName;

    @Builder
    private Sido(Integer sidoCode, String sidoName) {
        this.sidoCode = sidoCode;
        this.sidoName = sidoName;
    }
}
