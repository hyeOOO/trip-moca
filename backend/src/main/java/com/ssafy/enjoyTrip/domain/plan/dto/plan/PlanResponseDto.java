package com.ssafy.enjoyTrip.domain.plan.dto.plan;

import com.ssafy.enjoyTrip.domain.plan.entity.Plan;
import com.ssafy.enjoyTrip.domain.plan.entity.PlanStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlanResponseDto {
    private Long planId;
    private String planTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private PlanStatus status;
    private int totalDays;        // 총 여행 일수
    private int totalSpots;       // 총 여행지 수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlanResponseDto fromEntity(Plan plan) {
        return PlanResponseDto.builder()
                .planId(plan.getPlanId())
                .planTitle(plan.getPlanTitle())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .status(plan.getStatus())
                .totalDays((int) ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1)
                .totalSpots(plan.getPlanDetails().size())
                .createdAt(plan.getCreateDate())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }

}
