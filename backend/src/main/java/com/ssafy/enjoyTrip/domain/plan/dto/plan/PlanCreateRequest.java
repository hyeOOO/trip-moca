package com.ssafy.enjoyTrip.domain.plan.dto.plan;

import com.ssafy.enjoyTrip.domain.plan.dto.detail.DayPlanRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class PlanCreateRequest {
    @NotBlank(message = "제목 필수입니다")
    private String planTitle;
    @NotBlank(message = "지역 선택은 필수 입니다")
    private Long areaCode;
    private String planProfileImg;
    @NotBlank(message = "여행 시작일은 필수입니다")
    private LocalDate startDate;
    @NotBlank(message = "여행 종료일은 필수입니다")
    private LocalDate endDate;
    private List<DayPlanRequest> dayPlans; // 일자별 계획들

}
