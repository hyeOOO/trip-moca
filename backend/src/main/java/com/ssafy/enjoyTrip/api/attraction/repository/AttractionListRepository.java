package com.ssafy.enjoyTrip.api.attraction.repository;

import com.ssafy.enjoyTrip.api.attraction.entity.AttractionList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttractionListRepository extends JpaRepository<AttractionList, Long> {
}
