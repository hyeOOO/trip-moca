package com.ssafy.enjoyTrip.api.attraction.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiPlanResponseDto {
    private List<DayPlanDto> plans;
}
