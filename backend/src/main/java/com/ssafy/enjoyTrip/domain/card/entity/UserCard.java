package com.ssafy.enjoyTrip.domain.card.entity;

import com.ssafy.enjoyTrip.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity(name = "USER_CARD_LIST")
public class UserCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberCardId;

    @Column(name="member_id", nullable = false)  // insertable=false, updatable=false 제거
    private String memberId;

    @Column(name="card_id", nullable = false)    // insertable=false, updatable=false 제거
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", referencedColumnName = "cardId", insertable = false, updatable = false)
    private Card card;

    // 생성자 추가
    public UserCard(String memberId, Long cardId) {
        this.memberId = memberId;
        this.cardId = cardId;
    }
}
