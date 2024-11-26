package com.ssafy.enjoyTrip.domain.attraction.controller;

import com.ssafy.enjoyTrip.domain.attraction.dto.*;
import com.ssafy.enjoyTrip.domain.attraction.entity.SearchKeyword;
import com.ssafy.enjoyTrip.domain.attraction.service.AttractionService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "지역 리스트 조회", description = "전체 지역 조회를 위한 API입니다.")
    @GetMapping("/find/area")
    public ResponseEntity<List<SidoListResponseDto>> getAreaList(){
        return ResponseEntity.ok(attractionService.getAllAreaList());
    }

    @Operation(summary = "컨텐츠 타입(카테고리) 조회", description = "전체 컨텐츠 타입(카테고리) 조회를 위한 API입니다.")
    @GetMapping("/find/content")
    public ResponseEntity<List<ContentTypeListResponseDto>> getContentTypeList(){
        return ResponseEntity.ok(attractionService.getAllContentTypeList());
    }

    @Operation(summary = "시/군/구 조회", description = "전국 시/군/구 조회를 위한 API입니다.")
    @GetMapping("/find/gungu")
    public ResponseEntity<List<GugunListResponseDto>> getGugunList(){
        return ResponseEntity.ok(attractionService.getAllGugunList());
    }

    @Operation(summary = "관광지 검색", description = "지역/시,군,구/카테고리/키워드별 관광지 검색 API입니다.")
    @GetMapping("/search")
    public ResponseEntity<Page<AttractionListResponseDto>> searchAttractions(
            @ModelAttribute AttractionSearchRequestDto searchRequest,
            Pageable pageable) {
        return ResponseEntity.ok(
                attractionService.searchAttractions(searchRequest, pageable)
        );
    }

    @Operation(summary = "관광지 기본 검색", description = "지역/시,군,구/카테고리/키워드별 관광지 검색(페이징 처리x) API입니다.")
    @GetMapping("/search/no-page")
    public ResponseEntity<List<AttractionListResponseDto>> searchAttractionsNoPage(
            @ModelAttribute AttractionSearchRequestDto searchRequest) {
        return ResponseEntity.ok(
                attractionService.searchAttractionsNoPage(searchRequest)
        );
    }

    @Operation(summary = "추천 관광지 조회", description = "이미지가 있는 관광지를 조회하는 API입니다.")
    @GetMapping("/recommended/{areaCode}")
    public ResponseEntity<List<AttractionListResponseDto>> getRecommended(
            @PathVariable Long areaCode) {
        return ResponseEntity.ok(
                attractionService.getRecommendedAttractions(areaCode)
        );
    }

    @Operation(summary = "인기 키워드/지역/카테고리 조회", description = "인기있는(많이 검색되거나 여행 계획에 많이 추가된) 키워드/지역/카테고리를 조회하는 API입니다.")
    @GetMapping("/popular/keywords")
    public ResponseEntity<List<SearchKeyword>> getPopularKeywords(
            @RequestParam SearchKeyword.SearchType searchType) {
        return ResponseEntity.ok(attractionService.getPopularKeywords(searchType));
    }

    @Operation(summary = "인기 검색어 기반 관광지 조회", description = "인기 검색어를 기반으로 인기 관광지 5개를 조회하는 API입니다.")
    @GetMapping("/popular/attractions")
    public ResponseEntity<List<AttractionListResponseDto>> getPopularAttractionsByKeywords() {
        return ResponseEntity.ok(attractionService.getPopularAttractionsByKeywords());
    }
}
