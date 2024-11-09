package com.ssafy.enjoyTrip.domain.card.controller;

import com.ssafy.enjoyTrip.domain.card.dto.CardListResponseDto;
import com.ssafy.enjoyTrip.domain.card.dto.LocationRequest;
import com.ssafy.enjoyTrip.domain.card.entity.Card;
import com.ssafy.enjoyTrip.domain.card.mapper.CardListMapper;
import com.ssafy.enjoyTrip.domain.card.service.CardService;
import com.ssafy.enjoyTrip.global.annotation.CurrentMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Card", description = "카드 관련 API입니다.")
@RestController
@RequestMapping("/domain/card")
@RequiredArgsConstructor
@Slf4j
public class CardController {
    private final CardService cardService;
    private CardListResponseDto convertToDto(Card card) {
        return CardListResponseDto.builder()
                .cardId(card.getCardId())
                .cardCode(card.getCardCode())
                .cardName(card.getCardName())
                .image1(card.getImage1())
                .areaCode(card.getAreaCode())
                .sidoName(card.getArea() != null ? card.getArea().getSidoName() : null)
                .build();
    }

    @Operation(summary = "전체 카드 리스트 조회", description = "전체 카드 리스트 조회를 위한 API입니다.")
    @GetMapping("/list")
    public ResponseEntity<List<CardListResponseDto>> getCardList() {
        List<Card> cards = cardService.getAllCards();
        List<CardListResponseDto> dtoList = cards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "유저가 가지고 있는 카드 조회", description = "유저가 가지고 있는 카드 리스트 조회를 위한 API입니다.")
    @GetMapping("/user-card")
    public ResponseEntity<List<CardListResponseDto>> getUserCardList(@CurrentMemberId String memberId) {
        List<Card> userCards = cardService.getUserCards(memberId);
        List<CardListResponseDto> dtoList = userCards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "유저의 카드 획득", description = "유저가 일정 지역에 도착했을 때 카드 획득을 위한 API입니다.")
    @PostMapping("/get-card")
    public ResponseEntity<CardListResponseDto> checkLocationAndGetCard(
            @CurrentMemberId String memberId,
            @RequestBody LocationRequest location) {
        Card card = cardService.processUserLocation(location, memberId);
        return card != null ?
                ResponseEntity.ok(convertToDto(card)) :
                ResponseEntity.notFound().build();
    }
}