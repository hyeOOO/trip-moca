package com.ssafy.enjoyTrip.domain.attraction.service;

import com.ssafy.enjoyTrip.domain.attraction.dto.*;
import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import com.ssafy.enjoyTrip.domain.attraction.entity.GugunList;
import com.ssafy.enjoyTrip.domain.attraction.entity.SearchKeyword;
import com.ssafy.enjoyTrip.domain.attraction.entity.SidoList;
import com.ssafy.enjoyTrip.domain.attraction.repository.*;
import com.ssafy.enjoyTrip.domain.attraction.util.SearchKeywordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttractionService {
    private final AttractionRepository attractionRepository;
    private final ContentTypeRepository contentTypeRepository;
    private final SidoRepository sidoRepository;
    private final GugunRepository gugunRepository;
    private final SearchKeywordRepository searchKeywordRepository;

    // 컨텐츠 타입 리스트 조회
    public List<ContentTypeListResponseDto> getAllContentTypeList(){
        return ContentTypeListResponseDto.fromEntities(contentTypeRepository.findAll());
    }

    // 전체 지역 리스트 조회
    public List<SidoListResponseDto> getAllAreaList(){
        return SidoListResponseDto.fromEntities(sidoRepository.findAll());
    }

    // 전체 시/군/구 리스트 조회
    public List<GugunListResponseDto> getAllGugunList(){
        return GugunListResponseDto.fromEntities(gugunRepository.findAll());
    }

    // 관광지 검색 메서드
    public Page<AttractionListResponseDto> searchAttractions(
            AttractionSearchRequestDto searchRequest,
            Pageable pageable) {
        // 검색 키워드 통계 저장
        saveSearchStatistics(searchRequest);

        return attractionRepository.searchAttractionsWithPaging(
                searchRequest.getAreaCode(),
                searchRequest.getSiGunGuCode(),
                searchRequest.getTitle(),
                searchRequest.getContentTypeId(),
                pageable
        ).map(AttractionListResponseDto::fromEntity);
    }

    // 관광지 검색 메서드(페이징처리 x)
    public List<AttractionListResponseDto> searchAttractionsNoPage(
            AttractionSearchRequestDto searchRequest) {
        // 검색 키워드 통계 저장
        saveSearchStatistics(searchRequest);

        List<AttractionList> attractions = attractionRepository.searchAttractions(
                searchRequest.getAreaCode(),
                searchRequest.getSiGunGuCode(),
                searchRequest.getTitle(),
                searchRequest.getContentTypeId()
        );

        return AttractionListResponseDto.fromEntities(attractions);
    }

    // 지역, 컨텐츠 별 관광지 조회(ex. 부산의 음식집 조회)
    public List<AttractionListResponseDto> getAttractionsByAreaAndType(
            Long areaCode,
            Long contentTypeId) {
        return AttractionListResponseDto.fromEntities(
                attractionRepository.findByAreaCodeAndContentType(areaCode, contentTypeId)
        );
    }

    // 추천 관광지 관련 메서드(=이미지 있는 관광지만 검색)
    public List<AttractionListResponseDto> getRecommendedAttractions(Long areaCode) {
        return AttractionListResponseDto.fromEntities(
                attractionRepository.findByAreaCodeWithImages(areaCode)
        );
    }

    public void saveSearchStatistics(AttractionSearchRequestDto searchRequest) {
        boolean notTrashValue = false;
        // 지역 검색 통계
        if (searchRequest.getAreaCode() != null) {
            notTrashValue = true;
            updateSearchKeyword(
                    SidoCode.getNameByCode(String.valueOf(searchRequest.getAreaCode())),
                    SearchKeyword.SearchType.AREA
            );
        }

        // 키워드 검색 통계
        if (searchRequest.getTitle() != null && !searchRequest.getTitle().trim().isEmpty()) {
            notTrashValue = true;
            updateSearchKeyword(
                    searchRequest.getTitle(),
                    SearchKeyword.SearchType.KEYWORD
            );
        }

        // 카테고리 검색 통계
        if (searchRequest.getContentTypeId() != null) {
            notTrashValue = true;
            updateSearchKeyword(
                    ContentType.getNameByCode(String.valueOf(searchRequest.getContentTypeId())),
                    SearchKeyword.SearchType.CATEGORY
            );
        }
    }

    public void updateSearchKeyword(String keyword, SearchKeyword.SearchType searchType) {
        // 키워드 정제
        String sanitizedKeyword = SearchKeywordFilter.sanitizeKeyword(keyword);

        // 유효성 검사
        if (!SearchKeywordFilter.isValidKeyword(sanitizedKeyword)) {
            return; // 유효하지 않은 키워드는 저장하지 않음
        }

        SearchKeyword searchKeyword = searchKeywordRepository
                .findByKeywordAndSearchType(sanitizedKeyword, searchType);

        if (searchKeyword == null) {
            searchKeyword = SearchKeyword.builder()
                    .keyword(sanitizedKeyword)
                    .searchType(searchType)
                    .count(1L)
                    .lastSearched(LocalDateTime.now())
                    .build();
        } else {
            searchKeyword.increaseCount();
        }

        searchKeywordRepository.save(searchKeyword);
    }

    // 인기 검색어 조회 메서드
    public List<SearchKeyword> getPopularKeywords(SearchKeyword.SearchType searchType) {
        return searchKeywordRepository.findTop10BySearchTypeOrderByCountDesc(searchType);
    }

    @Transactional(readOnly = true)
    public List<AttractionListResponseDto> getPopularAttractionsByKeywords() {
        // 1. 인기 검색어 30개 조회
        List<SearchKeyword> popularKeywords = searchKeywordRepository
                .findTop30BySearchTypeOrderByCountDesc(SearchKeyword.SearchType.KEYWORD);

        // 2. 각 검색어로 관광지 검색 및 결과 저장을 위한 리스트
        List<AttractionListResponseDto> allAttractions = new ArrayList<>();

        // 3. 각 키워드별로 관광지 검색
        for (SearchKeyword keyword : popularKeywords) {
            // 키워드로 관광지 검색
            List<AttractionList> attractions = attractionRepository.searchAttractions(
                    null,  // areaCode
                    null,  // siGunGuCode
                    keyword.getKeyword(),  // title
                    null   // contentTypeId
            );

            // 검색 결과가 있는 경우, 첫 번째 관광지만 추가
            if (!attractions.isEmpty()) {
                allAttractions.add(AttractionListResponseDto.fromEntity(attractions.get(0)));
            }

            // 이미 5개의 관광지를 찾았다면 중단
            if (allAttractions.size() >= 5) {
                break;
            }
        }

        // 4. 최대 5개까지만 반환
        return allAttractions.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

}
