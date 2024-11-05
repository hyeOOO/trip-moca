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

    // 방법 1: REPLACE 함수 사용
    @Query(value =
            "SELECT * FROM attraction_list WHERE area_code = :areaCode " +
                    "AND REPLACE(title, ' ', '') IN (:normalizedTitles)",
            nativeQuery = true)
    List<AttractionList> findByAreaCodeAndNormalizedTitles(
            @Param("areaCode") Long areaCode,
            @Param("normalizedTitles") List<String> normalizedTitles
    );

    // 방법 2: LIKE를 사용한 유연한 검색
    @Query(value =
            "SELECT * FROM attraction_list WHERE area_code = :areaCode " +
                    "AND REPLACE(LOWER(title), ' ', '') LIKE CONCAT('%', REPLACE(LOWER(:title), ' ', ''), '%') LIMIT 1",
            nativeQuery = true)
    List<AttractionList> findByAreaCodeAndTitleFlexible(
            @Param("areaCode") Long areaCode,
            @Param("title") String title
    );
}
