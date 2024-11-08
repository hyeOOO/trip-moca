package com.ssafy.enjoyTrip.domain.attraction.service;

import com.ssafy.enjoyTrip.domain.attraction.dto.*;
import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import com.ssafy.enjoyTrip.domain.attraction.entity.GugunList;
import com.ssafy.enjoyTrip.domain.attraction.entity.SidoList;
import com.ssafy.enjoyTrip.domain.attraction.repository.AttractionRepository;
import com.ssafy.enjoyTrip.domain.attraction.repository.ContentTypeRepository;
import com.ssafy.enjoyTrip.domain.attraction.repository.GugunRepository;
import com.ssafy.enjoyTrip.domain.attraction.repository.SidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return attractionRepository.searchAttractionsWithPaging(
                searchRequest.getAreaCode(),
                searchRequest.getSiGunGuCode(),
                searchRequest.getTitle(),
                searchRequest.getContentTypeId(),
                pageable
        ).map(AttractionListResponseDto::fromEntity);
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

}
