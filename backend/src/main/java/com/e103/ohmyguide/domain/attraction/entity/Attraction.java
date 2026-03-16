package com.e103.ohmyguide.domain.attraction.entity;

import com.e103.ohmyguide.domain.contenttype.entity.ContentType;
import com.e103.ohmyguide.domain.gugun.entity.Gugun;
import com.e103.ohmyguide.domain.sido.entity.Sido;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(
        name = "attractions",
        indexes = {
                @Index(name = "idx_attractions_content_type_id", columnList = "content_type_id"),
                @Index(name = "idx_attractions_sido_code", columnList = "sido_code"),
                @Index(name = "idx_attractions_gugun_code", columnList = "gugun_code")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attraction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attr_id")
    private Integer id;

    @Column(name = "title", length = 500)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_type_id", referencedColumnName = "content_type_id")
    private ContentType contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sido_code", referencedColumnName = "sido_code")
    private Sido sido;

    @Column(name = "gugun_code")
    private Integer gugunCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "gugun_code", referencedColumnName = "gugun_code", insertable = false, updatable = false),
            @JoinColumn(name = "sido_code", referencedColumnName = "sido_code", insertable = false, updatable = false)
    })
    private Gugun gugun;

    @Column(name = "addr1", length = 100)
    private String addr1;

    @Column(name = "addr2", length = 100)
    private String addr2;

    @Column(name = "tel", length = 20)
    private String tel;

    @Column(name = "latitude", precision = 20, scale = 17)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 20, scale = 17)
    private BigDecimal longitude;

    @Column(name = "first_image1", length = 100)
    private String firstImage1;

    @Column(name = "first_image2", length = 100)
    private String firstImage2;

    @Column(name = "homepage", length = 1000)
    private String homepage;

    @Column(name = "overview", length = 10000)
    private String overview;

    @Builder
    private Attraction(String title, ContentType contentType, Sido sido, Integer gugunCode, Gugun gugun,
                       String addr1, String addr2, String tel,
                       BigDecimal latitude, BigDecimal longitude,
                       String firstImage1, String firstImage2,
                       String homepage, String overview) {
        this.title = title;
        this.contentType = contentType;
        this.sido = sido;
        this.gugunCode = gugunCode;
        this.gugun = gugun;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.tel = tel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.firstImage1 = firstImage1;
        this.firstImage2 = firstImage2;
        this.homepage = homepage;
        this.overview = overview;
    }
}
