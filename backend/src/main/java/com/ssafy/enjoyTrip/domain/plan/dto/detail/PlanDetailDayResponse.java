package com.ssafy.enjoyTrip.domain.plan.dto.detail;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlanDetailDayResponse {
    private Integer day;
    private String date;
    private List<PlanDetailResponseDto> details;
}
