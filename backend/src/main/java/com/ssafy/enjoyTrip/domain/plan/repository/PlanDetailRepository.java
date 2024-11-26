package com.ssafy.enjoyTrip.domain.plan.repository;

import com.ssafy.enjoyTrip.domain.plan.entity.PlanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanDetailRepository extends JpaRepository<PlanDetail, Long> {
    List<PlanDetail> findByPlanIdAndDayOrderBySequence(Long planId, Integer day);

    List<PlanDetail> findByPlanIdAndDayAndSequenceGreaterThanEqual(
            Long planId, Integer day, Integer sequence);

    List<PlanDetail> findByPlanIdAndDayAndSequenceBetween(
            Long planId, Integer day, Integer startSequence, Integer endSequence);

    void deleteByPlanId(Long planId);
}
