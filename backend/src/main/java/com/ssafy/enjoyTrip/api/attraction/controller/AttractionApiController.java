package com.ssafy.enjoyTrip.api.attraction.controller;

import com.ssafy.enjoyTrip.api.attraction.dto.DayPlanDto;
import com.ssafy.enjoyTrip.api.attraction.service.AttractionAiPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attraction")
public class AttractionApiController {
    private final AttractionAiPlanService attractionAiPlanService;

    @GetMapping("/ai/plan/{sidoCode}/{day}")
    public ResponseEntity<List<DayPlanDto>> getAiPlan(
            @PathVariable("sidoCode") String sidoCode,
            @PathVariable("day") String day) {
        return ResponseEntity.ok(attractionAiPlanService.generatePlan(sidoCode, day));
    }
}
