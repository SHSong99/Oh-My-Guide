package com.e103.ohmyguide.domain.attraction.entity;

import com.e103.ohmyguide.domain.contenttype.entity.ContentType;
import com.e103.ohmyguide.domain.gugun.entity.Gugun;
import com.e103.ohmyguide.domain.sido.entity.Sido;
import com.e103.ohmyguide.domain.themeattraction.entity.ThemeAttraction;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private Long id;

    @Column(name = "content_id")
    private Integer contentId;

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

    @Column(name = "first_image1", length = 500)
    private String firstImage1;

    @Column(name = "first_image2", length = 500)
    private String firstImage2;

    @Column(name = "homepage", length = 1000)
    private String homepage;

    @Column(name = "overview", length = 10000)
    private String overview;

    @Column(name = "overview_tts", length = 10000)
    private String overviewTts;

    @OneToMany(mappedBy = "attraction")
    private List<ThemeAttraction> themeAttractions = new ArrayList<>();

    @Builder
    private Attraction(Integer contentId, String title, ContentType contentType, Sido sido, Integer gugunCode, Gugun gugun,
                       String addr1, String addr2, String tel,
                       BigDecimal latitude, BigDecimal longitude,
                       String firstImage1, String firstImage2,
                       String homepage, String overview, String overviewTts) {
        this.contentId = contentId;
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
        this.overviewTts = overviewTts;
    }

    public void fillImageAndOverview(String firstImage1, String firstImage2, String overview) {
        if (this.firstImage1 == null || this.firstImage1.isEmpty()) this.firstImage1 = firstImage1;
        if (this.firstImage2 == null || this.firstImage2.isEmpty()) this.firstImage2 = firstImage2;
        if (this.overview == null || this.overview.isEmpty()) this.overview = overview;
    }

    public void update(String title, BigDecimal latitude, BigDecimal longitude, String firstImage1, String overview, String overviewTts) {
        if (title != null) this.title = title;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (firstImage1 != null) this.firstImage1 = firstImage1;
        if (overview != null) this.overview = overview;
        if (overviewTts != null) this.overviewTts = overviewTts;
    }
}
