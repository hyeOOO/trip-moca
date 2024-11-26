package com.ssafy.enjoyTrip.domain.attraction.repository;

import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttractionRepository extends JpaRepository<AttractionList, Long> {
    // 기본 검색 - 시도/구군 정보 포함
    @Query("SELECT DISTINCT a FROM ATTRACTION_LIST a " +
            "LEFT JOIN FETCH a.contentType c " +
            "LEFT JOIN FETCH a.sido s " +
            "LEFT JOIN FETCH a.gugun g " +
            "WHERE (:contentTypeId IS NULL OR a.contentTypeId = :contentTypeId) " +
            "AND (:areaCode IS NULL OR a.areaCode = :areaCode) " +
            "AND (:siGunGuCode IS NULL OR a.siGunGuCode = :siGunGuCode) " +
            "AND (:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) ")
    List<AttractionList> searchAttractions(
            @Param("areaCode") Long areaCode,
            @Param("siGunGuCode") Long siGunGuCode,
            @Param("title") String title,
            @Param("contentTypeId") Long contentTypeId
    );

    // 페이징이 필요한 경우를 위한 카운트 쿼리
    @Query("SELECT COUNT(a) FROM ATTRACTION_LIST a " +
            "WHERE (:areaCode IS NULL OR a.areaCode = :areaCode) " +
            "AND (:siGunGuCode IS NULL OR a.siGunGuCode = :siGunGuCode) " +
            "AND (:contentTypeId IS NULL OR a.contentTypeId = :contentTypeId) " +
            "AND (:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) ")
    long countAttractions(
            @Param("areaCode") Long areaCode,
            @Param("siGunGuCode") Long siGunGuCode,
            @Param("title") String title,
            @Param("contentTypeId") Long contentTypeId
    );

    // 페이징 처리된 검색 (fetch join은 제외)
    @Query("SELECT a FROM ATTRACTION_LIST a " +
            "WHERE (:areaCode IS NULL OR a.areaCode = :areaCode) " +
            "AND (:siGunGuCode IS NULL OR a.siGunGuCode = :siGunGuCode) " +
            "AND (:contentTypeId IS NULL OR a.contentTypeId = :contentTypeId) " +
            "AND (:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) " )
    Page<AttractionList> searchAttractionsWithPaging(
            @Param("areaCode") Long areaCode,
            @Param("siGunGuCode") Long siGunGuCode,
            @Param("title") String title,
            @Param("contentTypeId") Long contentTypeId,
            Pageable pageable
    );

    // 지역별 특정 컨텐츠 타입 조회
    @Query("SELECT a FROM ATTRACTION_LIST a " +
            "LEFT JOIN FETCH a.sido s " +
            "WHERE a.areaCode = :areaCode " +
            "AND a.contentTypeId = :contentTypeId")
    List<AttractionList> findByAreaCodeAndContentType(
            @Param("areaCode") Long areaCode,
            @Param("contentTypeId") Long contentTypeId
    );

    // 이미지가 있는 관광지만 조회
    @Query("SELECT a FROM ATTRACTION_LIST a " +
            "LEFT JOIN FETCH a.contentType c " +
            "LEFT JOIN FETCH a.sido s " +
            "LEFT JOIN FETCH a.gugun g " +
                "WHERE a.areaCode = :areaCode " +
            "AND a.image1 IS NOT NULL")
    List<AttractionList> findByAreaCodeWithImages(@Param("areaCode") Long areaCode);

}
