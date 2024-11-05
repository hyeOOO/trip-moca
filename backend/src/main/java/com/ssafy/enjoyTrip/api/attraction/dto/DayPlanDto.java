package com.ssafy.enjoyTrip.api.attraction.dto;

import lombok.Data;

import java.util.List;

@Data
public class DayPlanDto { //일자별 관광지 리스트를 담을 dto
    private int day; // 일자
    private List<AttractionTitleDto> attractions; // 관광지명
    private List<AttractionListResponseDto> attractionDetails; // 관광지 상세 정보
}
