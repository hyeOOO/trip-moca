package com.ssafy.enjoyTrip.domain.card.entity;

import com.ssafy.enjoyTrip.domain.attraction.entity.SidoList;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity(name="CARD_LIST")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    private Long cardCode;

    private String cardName;

    private String image1;

    @Column(name = "area_code", insertable = false, updatable = false)
    private Long areaCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_code", referencedColumnName = "sido_code")
    private SidoList area;

    // ToString 무한순환 방지
    @Override
    public String toString() {
        return "Card{" +
                "cardId=" + cardId +
                ", cardCode=" + cardCode +
                ", cardName='" + cardName + '\'' +
                ", image1='" + image1 + '\'' +
                ", areaCode=" + areaCode +
                '}';
    }
}
