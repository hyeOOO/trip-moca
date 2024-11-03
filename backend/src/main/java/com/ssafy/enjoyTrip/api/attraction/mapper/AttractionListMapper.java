package com.ssafy.enjoyTrip.api.attraction.mapper;

import com.ssafy.enjoyTrip.api.attraction.dto.AttractionListResponseDto;
import com.ssafy.enjoyTrip.api.attraction.entity.AttractionList;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttractionListMapper {
    List<AttractionListResponseDto> attractionListToAttractionResponseDto(List<AttractionList> attractionLists);
}
