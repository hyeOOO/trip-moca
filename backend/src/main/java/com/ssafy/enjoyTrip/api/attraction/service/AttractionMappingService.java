package com.ssafy.enjoyTrip.api.attraction.service;

import com.ssafy.enjoyTrip.api.attraction.repository.AttractionApiRepository;
import com.ssafy.enjoyTrip.domain.attraction.dto.AttractionListResponseDto;
import com.ssafy.enjoyTrip.api.attraction.dto.AttractionTitleDto;
import com.ssafy.enjoyTrip.api.attraction.dto.DayPlanDto;
import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 관광지 매핑 서비스
@Service
@RequiredArgsConstructor
public class AttractionMappingService {
    private final AttractionApiRepository attractionListRepository;

    public List<DayPlanDto> mapAttractionDetails(Long areaCode, List<DayPlanDto> plans) {
        for (DayPlanDto dayPlan : plans) {
            List<String> attractionTitles = dayPlan.getAttractions().stream()
                    .map(AttractionTitleDto::getTitle)
                    .map(title -> title.replaceAll("\\s+", ""))  // 띄어쓰기 제거
                    .collect(Collectors.toList());

            List<AttractionList> flexibleResults = new ArrayList<>();
            for (String title : attractionTitles) {
                flexibleResults.addAll(
                        attractionListRepository.findByAreaCodeAndTitleFlexible(areaCode, title)
                );
            }
            List<AttractionListResponseDto> attractionDetails = flexibleResults.stream()
                    .map(AttractionListResponseDto::fromEntity)
                    .collect(Collectors.toList());

            dayPlan.setAttractionDetails(attractionDetails);
        }
        return plans;
    }

    public List<DayPlanDto> mapAttractionDetails(List<DayPlanDto> plans){
        for (DayPlanDto dayPlan : plans) {
            List<String> attractionTitles = dayPlan.getAttractions().stream()
                    .map(AttractionTitleDto::getTitle)
                    .map(title -> title.replaceAll("\\s+", ""))  // 띄어쓰기 제거
                    .collect(Collectors.toList());

            List<AttractionList> flexibleResults = new ArrayList<>();
            for (String title : attractionTitles) {
                flexibleResults.addAll(
                        attractionListRepository.findByTitleFlexible(title)
                );
            }
            List<AttractionListResponseDto> attractionDetails = flexibleResults.stream()
                    .map(AttractionListResponseDto::fromEntity)
                    .collect(Collectors.toList());

            dayPlan.setAttractionDetails(attractionDetails);
        }
        return plans;
    }
}
