package com.ssafy.enjoyTrip.domain.attraction.service;

import com.ssafy.enjoyTrip.domain.attraction.dto.*;
import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import com.ssafy.enjoyTrip.domain.attraction.entity.GugunList;
import com.ssafy.enjoyTrip.domain.attraction.entity.SearchKeyword;
import com.ssafy.enjoyTrip.domain.attraction.entity.SidoList;
import com.ssafy.enjoyTrip.domain.attraction.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        SearchKeyword searchKeyword = searchKeywordRepository
                .findByKeywordAndSearchType(keyword, searchType);

        if (searchKeyword == null) {
            searchKeyword = SearchKeyword.builder()
                    .keyword(keyword)
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

}
