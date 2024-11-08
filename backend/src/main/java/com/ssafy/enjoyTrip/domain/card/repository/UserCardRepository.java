package com.ssafy.enjoyTrip.domain.card.repository;

import com.ssafy.enjoyTrip.domain.card.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    List<UserCard> findByMemberId(String memberId);
    boolean existsByMemberIdAndCardId(String memberId, Long cardId);
}
