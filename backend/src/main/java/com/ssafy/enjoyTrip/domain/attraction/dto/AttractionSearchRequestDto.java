package com.ssafy.enjoyTrip.domain.attraction.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttractionSearchRequestDto {
    private Long areaCode;
    private Long siGunGuCode;
    private String title;
    private Long contentTypeId;
}