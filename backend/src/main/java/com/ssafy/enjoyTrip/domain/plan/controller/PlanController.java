package com.ssafy.enjoyTrip.domain.plan.controller;

import com.ssafy.enjoyTrip.domain.plan.dto.detail.PlanDetailResponseDto;
import com.ssafy.enjoyTrip.domain.plan.dto.detail.PlanDetailsResponseDto;
import com.ssafy.enjoyTrip.domain.plan.dto.plan.PlanCreateRequest;
import com.ssafy.enjoyTrip.domain.plan.dto.plan.PlanResponseDto;
import com.ssafy.enjoyTrip.domain.plan.dto.plan.PlanUpdateRequest;
import com.ssafy.enjoyTrip.domain.plan.service.PlanService;
import com.ssafy.enjoyTrip.global.annotation.CurrentMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Plan", description = "여행 계획 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/domain/plans")
public class PlanController {
    private final PlanService planService;

    @Operation(summary = "여행 계획 생성", description = "상세 여행 계획까지 포함된 여행 계획을 생성하는 API 입니다.")
    @PostMapping
    public ResponseEntity<String> createPlan(
            @RequestBody PlanCreateRequest request,
            @CurrentMemberId String memberId) {
        planService.createPlan(request, memberId);
        return ResponseEntity.ok("여행 계획이 성공적으로 추가되었습니다.");
    }

    @Operation(summary = "여행 계획 수정", description = "상세 여행 계획까지 수정 가능한 API 입니다.")
    @PutMapping("/{planId}")
    public ResponseEntity<String> updatePlan(
            @PathVariable String planId,
            @RequestBody PlanUpdateRequest request,
            @CurrentMemberId String memberId) {
        planService.updatePlan(request, planId, memberId);
        return ResponseEntity.ok("여행 계획이 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "여행 계획 삭제", description = "상세 여행 계획까지 삭제 가능한 API 입니다.")
    @DeleteMapping("/{planId}")
    public ResponseEntity<String> deletePlan(
            @PathVariable String planId,
            @CurrentMemberId String memberId) {
        planService.deletePlan(planId, memberId);
        return ResponseEntity.ok("여행 계획이 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "사용자 여행 계획 조회", description = "사용자의 여행 계획 리스트를 조회하기 위한 API 입니다.")
    @GetMapping
    public ResponseEntity<List<PlanResponseDto>> getAllPlans(
            @CurrentMemberId String memberId) {  // 커스텀 어노테이션 사용
        return ResponseEntity.ok(planService.getAllPlans(memberId));
    }

    @Operation(summary = "사용자의 특정 여행 계획 조회", description = "사용자의 특정 여행 계획을 조회하기 위한 API 입니다. ")
    @GetMapping("/{planId}")
    public ResponseEntity<PlanDetailsResponseDto> getPlanDetails(
            @PathVariable String planId,
            @CurrentMemberId String memberId) {
        return ResponseEntity.ok(planService.getPlanDetails(planId, memberId));
    }

    @Operation(summary = "사용자의 특정 여행의 특정 일자 조회", description = "사용자의 특정 여행 계획 중 특정 일자 데이터를 조회하기 위한 API 입니다.")
    @GetMapping("/{planId}/{day}/details")
    public ResponseEntity<List<PlanDetailResponseDto>> getPlanDetailsByDay(
            @PathVariable String planId,
            @PathVariable Integer day,
            @CurrentMemberId String memberId) {
        return ResponseEntity.ok(planService.getPlanDetailsByDay(planId, day, memberId));
    }
}