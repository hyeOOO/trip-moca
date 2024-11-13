package com.ssafy.enjoyTrip.domain.plan.dto.detail;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlanDetailCreateRequest {
    private Long attractionId;  // 관광지 ID
    private Integer sequence;   // 순서
    private String memo;       // 메모
}
