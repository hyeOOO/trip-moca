package com.ssafy.enjoyTrip.domain.card.mapper;

import com.ssafy.enjoyTrip.domain.card.dto.CardListResponseDto;
import com.ssafy.enjoyTrip.domain.card.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardListMapper {
    @Mapping(source = "area.sidoName", target = "sidoName")
    CardListResponseDto cardToCardResponseDto(Card card);

    List<CardListResponseDto> cardListToCardResponseDto(List<Card> cardList);
}
