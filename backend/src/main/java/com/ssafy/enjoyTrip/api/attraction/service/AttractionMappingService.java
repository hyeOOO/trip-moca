package com.ssafy.enjoyTrip.api.attraction.service;

import com.ssafy.enjoyTrip.api.attraction.dto.AttractionListResponseDto;
import com.ssafy.enjoyTrip.api.attraction.dto.AttractionTitleDto;
import com.ssafy.enjoyTrip.api.attraction.dto.DayPlanDto;
import com.ssafy.enjoyTrip.api.attraction.entity.AttractionList;
import com.ssafy.enjoyTrip.api.attraction.repository.AttractionListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 관광지 매핑 서비스
@Service
@RequiredArgsConstructor
public class AttractionMappingService {
    private final AttractionListRepository attractionListRepository;

    public List<DayPlanDto> mapAttractionDetails(Long areaCode, List<DayPlanDto> plans) {
        for (DayPlanDto dayPlan : plans) {
            List<String> attractionTitles = dayPlan.getAttractions().stream()
                    .map(AttractionTitleDto::getTitle)
                    .map(title -> title.replaceAll("\\s+", ""))  // 띄어쓰기 제거
                    .collect(Collectors.toList());

//            List<AttractionListResponseDto> attractionDetails = attractionListRepository
//                    .findByAreaCodeAndNormalizedTitles(areaCode, attractionTitles)
//                    .stream()
//                    .map(AttractionListResponseDto::fromEntity)
//                    .collect(Collectors.toList());

            // 정확한 매칭으로 찾지 못한 경우 유연한 검색 시도
//            if (attractionDetails.isEmpty()) {
                List<AttractionList> flexibleResults = new ArrayList<>();
                for (String title : attractionTitles) {
                    flexibleResults.addAll(
                            attractionListRepository.findByAreaCodeAndTitleFlexible(areaCode, title)
                    );
                }
            List<AttractionListResponseDto> attractionDetails = flexibleResults.stream()
                        .map(AttractionListResponseDto::fromEntity)
                        .collect(Collectors.toList());
//            }

            dayPlan.setAttractionDetails(attractionDetails);
        }
        return plans;
    }
}
