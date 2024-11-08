package com.ssafy.enjoyTrip.api.attraction.controller;

import com.ssafy.enjoyTrip.api.attraction.dto.DayPlanDto;
import com.ssafy.enjoyTrip.api.attraction.service.AttractionAiPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Attractions API", description = "관광지 정보를 바탕으로 AI API를 호출하기 위한 API입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attraction")
public class AttractionApiController {
    private final AttractionAiPlanService attractionAiPlanService;

    @Operation(summary = "AI 여행 계획 생성", description = "사용자가 입력한 지역과 여행일자에 맞춰서 AI가 여행을 생성해주는 API입니다.")
    @GetMapping("/ai/plan/{sidoCode}/{day}")
    public ResponseEntity<List<DayPlanDto>> getAiPlan(
            @PathVariable("sidoCode") String sidoCode,
            @PathVariable("day") String day) {
        return ResponseEntity.ok(attractionAiPlanService.generatePlan(sidoCode, day));
    }
}
