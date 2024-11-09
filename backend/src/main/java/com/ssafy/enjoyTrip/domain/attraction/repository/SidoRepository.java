package com.ssafy.enjoyTrip.domain.attraction.repository;

import com.ssafy.enjoyTrip.domain.attraction.entity.GugunList;
import com.ssafy.enjoyTrip.domain.attraction.entity.SidoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SidoRepository extends JpaRepository<SidoList, Long> {
    Optional<SidoList> findBySidoCode(Long sidoCode);
}
