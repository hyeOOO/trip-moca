package com.ssafy.enjoyTrip.domain.attraction.entity;


import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "popular")
public class SearchKeyword {
    @Id
    private String id;
    private String keyword;
    private Long count;
    private LocalDateTime lastSearched;
    private SearchType searchType; // 검색 타입 (지역, 키워드 등)

    // 검색 타입을 구분하기 위한 enum
    public enum SearchType {
        AREA,       // 지역 검색
        KEYWORD,    // 키워드 검색
        CATEGORY,    // 카테고리 검색
    }

    // 검색 횟수 증가
    public void increaseCount() {
        this.count = this.count + 1;
        this.lastSearched = LocalDateTime.now();
    }
}
