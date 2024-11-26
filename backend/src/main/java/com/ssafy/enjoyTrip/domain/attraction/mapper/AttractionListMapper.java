package com.ssafy.enjoyTrip.domain.attraction.mapper;

import com.ssafy.enjoyTrip.domain.attraction.dto.AttractionListResponseDto;
import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttractionListMapper {
    List<AttractionListResponseDto> attractionListToAttractionResponseDto(List<AttractionList> attractionLists);
}
