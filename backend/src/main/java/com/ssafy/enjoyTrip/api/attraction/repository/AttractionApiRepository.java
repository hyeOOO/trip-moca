package com.ssafy.enjoyTrip.api.attraction.repository;

import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttractionApiRepository extends JpaRepository<AttractionList, Long> {
    @Query(value =
            "SELECT * FROM attraction_list WHERE area_code = :areaCode " +
                    "AND REPLACE(title, ' ', '') IN (:normalizedTitles)",
            nativeQuery = true)
    List<AttractionList> findByAreaCodeAndNormalizedTitles(
            @Param("areaCode") Long areaCode,
            @Param("normalizedTitles") List<String> normalizedTitles
    );

    @Query(value =
            "SELECT a.* FROM attraction_list a " +
                    "LEFT JOIN sido_list s ON a.area_code = s.sido_code " +
                    "LEFT JOIN gugun_list g ON a.area_code = g.sido_code AND a.si_gun_gu_code = g.gugun_code " +
                    "WHERE a.area_code = :areaCode " +
                    "AND REPLACE(LOWER(a.title), ' ', '') LIKE CONCAT('%', REPLACE(LOWER(:title), ' ', ''), '%') " +
                    "LIMIT 1",
            nativeQuery = true)
    List<AttractionList> findByAreaCodeAndTitleFlexible(
            @Param("areaCode") Long areaCode,
            @Param("title") String title
    );

    @Query(value =
            "SELECT a.* FROM attraction_list a " +
                    "WHERE REPLACE(LOWER(a.title), ' ', '') LIKE CONCAT('%', REPLACE(LOWER(:title), ' ', ''), '%') " +
                    "LIMIT 1",
            nativeQuery = true)
    List<AttractionList> findByTitleFlexible(
            @Param("title") String title
    );
}

