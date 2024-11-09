package com.ssafy.enjoyTrip.domain.plan.entity;

import com.ssafy.enjoyTrip.domain.attraction.entity.AttractionList;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PLAN_DETAIL")
public class PlanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planDetailId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "attraction_id")
    private Long attractionId;

    private Integer day;
    private Integer sequence;

    private String memo;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", referencedColumnName = "planId",
            insertable = false, updatable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id", referencedColumnName = "attractionId",
            insertable = false, updatable = false)
    private AttractionList attractions;

    @Builder
    public PlanDetail(Long planId, Long attractionId, Integer day,
                      Integer sequence, String memo) {
        this.planId = planId;
        this.attractionId = attractionId;
        this.day = day;
        this.sequence = sequence;
        this.memo = memo;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }
}