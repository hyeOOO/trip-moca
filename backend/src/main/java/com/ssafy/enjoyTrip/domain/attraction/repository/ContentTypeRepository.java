package com.ssafy.enjoyTrip.domain.attraction.repository;

import com.ssafy.enjoyTrip.domain.attraction.entity.ContentTypeList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentTypeRepository extends JpaRepository<ContentTypeList, Long> {
}
