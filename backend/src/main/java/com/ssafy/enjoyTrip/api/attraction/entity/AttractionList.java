package com.ssafy.enjoyTrip.api.attraction.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
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
    private Long contentTypeId;
    private Long areaCode;
    private Long siGunGuCode;
    private String image1;
    private String image2;
    private Integer mapLevel;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String tel;
    private String addr1;
    private String addr2;
    private String homepage;
    private String overview;

    public AttractionList(Long attractionId, Long contentId, String title, Long contentTypeId, Long areaCode, Long siGunGuCode, String image1, String image2, Integer mapLevel, BigDecimal latitude, BigDecimal longitude, String tel, String addr1, String addr2) {
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
}
