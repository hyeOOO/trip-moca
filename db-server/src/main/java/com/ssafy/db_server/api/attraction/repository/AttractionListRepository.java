package com.ssafy.db_server.api.attraction.repository;

import com.ssafy.db_server.api.attraction.entity.AttractionList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttractionListRepository extends JpaRepository<AttractionList, Long> {
    List<AttractionList> findByOverviewIsNullOrHomepageIsNull();
    boolean existsByAreaCodeAndSiGunGuCode(Long area_code, Long si_gun_gu_code);
    Optional<AttractionList> findByContentId(Long contentId);
}
