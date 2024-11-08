package com.ssafy.enjoyTrip.domain.card.repository;

import com.ssafy.enjoyTrip.domain.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    // JPQL을 사용하여 N+1 문제 해결
    @Query("SELECT c FROM CARD_LIST c JOIN FETCH c.area WHERE c.areaCode = :areaCode")
    Optional<Card> findByAreaCode(@Param("areaCode") Long areaCode);

    // 전체 조회 시에도 N+1 문제 해결
    @Query("SELECT c FROM CARD_LIST c JOIN FETCH c.area")
    List<Card> findAllWithArea();
}
