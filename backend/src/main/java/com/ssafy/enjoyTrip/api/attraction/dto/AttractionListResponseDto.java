package com.ssafy.enjoyTrip.api.attraction.dto;

import com.ssafy.enjoyTrip.api.attraction.entity.AttractionList;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AttractionListResponseDto {
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

    public static AttractionListResponseDto fromEntity(AttractionList entity){
        AttractionListResponseDto dto = new AttractionListResponseDto();
        dto.setAttractionId(entity.getAttractionId());
        dto.setContentId(entity.getContentId());
        dto.setTitle(entity.getTitle());
        dto.setContentTypeId(entity.getContentTypeId());
        dto.setAreaCode(entity.getAreaCode());
        dto.setSiGunGuCode(entity.getSiGunGuCode());
        dto.setImage1(entity.getImage1());
        dto.setImage2(entity.getImage2());
        dto.setMapLevel(entity.getMapLevel());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setTel(entity.getTel());
        dto.setAddr1(entity.getAddr1());
        dto.setAddr2(entity.getAddr2());
        dto.setHomepage(entity.getHomepage());
        dto.setOverview(entity.getOverview());

        return dto;
    }
}
