package com.ssafy.enjoyTrip.domain.attraction.dto;

import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class AttractionListResponseDto {
    private Long attractionId;
    private Long contentId;
    private String title;
    private Long contentTypeId;
    private String contentTypeName;

    // 시도/구군 정보
    private Long sidoCode;
    private String sidoName;
    private Long gugunCode;
    private String gugunName;

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

    public static AttractionListResponseDto fromEntity(AttractionList entity) {
        return AttractionListResponseDto.builder()
                .attractionId(entity.getAttractionId())
                .contentId(entity.getContentId())
                .title(entity.getTitle())
                // 컨텐츠 타입 정보
                .contentTypeId(entity.getContentType()!=null?entity.getContentType().getContentTypeId():null)
                .contentTypeName(entity.getContentType()!=null?entity.getContentType().getContentTypeName():null)
                // 시도 정보
                .sidoCode(entity.getSido() != null ? entity.getSido().getSidoCode() : null)
                .sidoName(entity.getSido() != null ? entity.getSido().getSidoName() : null)
                // 구군 정보
                .gugunCode(entity.getGugun() != null ? entity.getGugun().getGugunCode() : null)
                .gugunName(entity.getGugun() != null ? entity.getGugun().getGugunName() : null)
                // 기본 정보
                .image1(entity.getImage1())
                .image2(entity.getImage2())
                .mapLevel(entity.getMapLevel())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .tel(entity.getTel())
                .addr1(entity.getAddr1())
                .addr2(entity.getAddr2())
                .homepage(entity.getHomepage())
                .overview(entity.getOverview())
                .build();
    }

    // 필요한 경우 리스트 변환을 위한 편의 메서드
    public static List<AttractionListResponseDto> fromEntities(List<AttractionList> entities) {
        return entities.stream()
                .map(AttractionListResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}