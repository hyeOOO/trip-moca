package com.ssafy.enjoyTrip.domain.attraction.repository;

import com.ssafy.enjoyTrip.domain.attraction.entity.GugunList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GugunRepository extends JpaRepository<GugunList, Long> {
}
