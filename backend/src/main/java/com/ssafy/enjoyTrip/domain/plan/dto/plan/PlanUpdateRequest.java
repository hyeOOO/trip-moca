package com.ssafy.enjoyTrip.domain.plan.dto.plan;

import com.ssafy.enjoyTrip.domain.plan.dto.detail.DayPlanRequest;
import com.ssafy.enjoyTrip.domain.plan.entity.PlanStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlanUpdateRequest {
    private String planTitle;              // 여행 계획 제목
    private LocalDate startDate;           // 여행 시작일
    private LocalDate endDate;             // 여행 종료일
    private PlanStatus status;             // 여행 상태
    private String planProfileImg;          // 여행 계획 대표 이미지
    private Long areaCode;
    private List<DayPlanRequest> dayPlans; // 일자별 계획들
}
