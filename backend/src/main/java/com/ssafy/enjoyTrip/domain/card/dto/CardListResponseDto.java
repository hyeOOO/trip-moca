package com.ssafy.enjoyTrip.domain.card.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardListResponseDto {
    private Long cardId;
    private Long cardCode;
    private String cardName;
    private String image1;
    private Long areaCode;
    private String sidoName;
}
