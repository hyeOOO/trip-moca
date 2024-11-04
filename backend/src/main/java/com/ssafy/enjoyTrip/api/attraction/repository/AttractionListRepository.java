package com.ssafy.enjoyTrip.api.attraction.repository;

import com.ssafy.enjoyTrip.api.attraction.entity.AttractionList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttractionListRepository extends JpaRepository<AttractionList, Long> {
    List<AttractionList> findByOverviewIsNullOrHomepageIsNull();
    long countByOverviewIsNullOrHomepageIsNull();

    // 여러 제목으로 관광지 검색
    @Query("SELECT a FROM ATTRACTION_LIST a WHERE a.title IN :titles")
    List<AttractionList> findByTitleIn(@Param("titles") List<String> titles);

    // 또는 부분 일치 검색이 필요한 경우
    @Query("SELECT a FROM ATTRACTION_LIST a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AttractionList> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);
}
