package com.ssafy.enjoyTrip.domain.plan.dto.detail;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DayPlanRequest {
    private Integer day;                           // 여행 몇일차
    private List<PlanDetailCreateRequest> details; // 해당 일자의 여행지들
}
