package com.ssafy.enjoyTrip.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@Entity(name="ATTRACTION_LIST")
public class AttractionList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attractionId;
    private Long contentId;
    private String title;

    @Column(name = "content_type_id", insertable = false, updatable = false)
    private Long contentTypeId;

    // area_code 컬럼을 위한 필드
    @Column(name = "area_code", insertable = false, updatable = false)
    private Long areaCode;

    @Column(name = "si_gun_gu_code", insertable = false, updatable = false)
    private Long siGunGuCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_type_id", referencedColumnName = "content_type_id")
    private ContentTypeList contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_code", referencedColumnName = "sido_code")
    private SidoList sido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "area_code", referencedColumnName = "sido_code", insertable = false, updatable = false),
            @JoinColumn(name = "si_gun_gu_code", referencedColumnName = "gugun_code", insertable = false, updatable = false)
    })
    private GugunList gugun;

    private String image1;
    private String image2;
    private Integer mapLevel;

    @Column(precision = 20, scale = 17)
    private BigDecimal latitude;

    @Column(precision = 20, scale = 17)
    private BigDecimal longitude;

    private String tel;
    private String addr1;
    private String addr2;

    @Column(length = 1000)
    private String homepage;

    @Column(columnDefinition = "LONGTEXT")
    private String overview;

    // 생성자
    public AttractionList(Long attractionId, Long contentId, String title, Long contentTypeId,
                          Long areaCode, Long siGunGuCode, String image1, String image2,
                          Integer mapLevel, BigDecimal latitude, BigDecimal longitude,
                          String tel, String addr1, String addr2) {
        this.attractionId = attractionId;
        this.contentId = contentId;
        this.title = title;
        this.contentTypeId = contentTypeId;
        this.areaCode = areaCode;
        this.siGunGuCode = siGunGuCode;
        this.image1 = image1;
        this.image2 = image2;
        this.mapLevel = mapLevel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tel = tel;
        this.addr1 = addr1;
        this.addr2 = addr2;
    }

    // 편의 메서드
    public String getContentTypeName() { return contentType != null ? contentType.getContentTypeName() : null; }

    public String getSidoName() {
        return sido != null ? sido.getSidoName() : null;
    }

    public String getGugunName() {
        return gugun != null ? gugun.getGugunName() : null;
    }
}