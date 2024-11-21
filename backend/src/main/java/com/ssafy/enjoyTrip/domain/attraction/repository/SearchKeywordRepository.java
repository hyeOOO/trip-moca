package com.ssafy.enjoyTrip.domain.attraction.repository;

import com.ssafy.enjoyTrip.domain.attraction.entity.SearchKeyword;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface SearchKeywordRepository extends MongoRepository<SearchKeyword, String> {
    SearchKeyword findByKeywordAndSearchType(String keyword, SearchKeyword.SearchType searchType);
    List<SearchKeyword> findTop10BySearchTypeOrderByCountDesc(SearchKeyword.SearchType searchType);
}