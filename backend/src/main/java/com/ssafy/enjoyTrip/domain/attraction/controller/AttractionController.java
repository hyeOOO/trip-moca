package com.ssafy.enjoyTrip.domain.attraction.controller;

import com.ssafy.enjoyTrip.domain.attraction.dto.AttractionListResponseDto;
import com.ssafy.enjoyTrip.domain.attraction.dto.AttractionSearchRequestDto;
import com.ssafy.enjoyTrip.domain.attraction.service.AttractionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Attractions", description = "관광지 조회 API입니다.")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/domain/attraction")
public class AttractionController {
    private final AttractionService attractionService;

    @GetMapping("/search")
    public ResponseEntity<Page<AttractionListResponseDto>> searchAttractions(
            @ModelAttribute AttractionSearchRequestDto searchRequest,
            Pageable pageable) {
        System.out.println("hi");
        return ResponseEntity.ok(
                attractionService.searchAttractions(searchRequest, pageable)
        );
    }

    @GetMapping("/recommended/{areaCode}")
    public ResponseEntity<List<AttractionListResponseDto>> getRecommended(
            @PathVariable Long areaCode) {
        return ResponseEntity.ok(
                attractionService.getRecommendedAttractions(areaCode)
        );
    }
}
