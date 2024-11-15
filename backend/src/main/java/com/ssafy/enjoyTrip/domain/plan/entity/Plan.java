package com.ssafy.enjoyTrip.domain.plan.entity;

import com.ssafy.enjoyTrip.domain.attraction.entity.SidoList;
import com.ssafy.enjoyTrip.domain.member.entity.Member;
import com.ssafy.enjoyTrip.domain.plan.dto.plan.PlanUpdateRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "PLAN")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    private String planTitle;

    @Column(name = "area_code", nullable = false)
    private Long areaCode;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    private LocalDateTime createDate; // 플랜 생성일
    private LocalDate startDate; // 여행 시작일
    private LocalDate endDate; //여행 종료일

    @Enumerated(EnumType.STRING)
    private PlanStatus status; // 여행계획 상태(작성중, 여행확정, 여행완료, 취소)

    private String planProfileImg;

    // 연관관계 매핑을 위한 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_code", referencedColumnName = "sido_code",
            insertable = false, updatable = false)
    private SidoList area;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", insertable = false, updatable = false )
    private Member member; // member와 join

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<PlanDetail> planDetails = new ArrayList<>(); // 계획 상세 테이블이랑 양방향 관계 및 트리거 설정

    @LastModifiedDate
    private LocalDateTime updatedAt; // 계획 수정일 자동 생성

    @Builder
    public Plan(String planTitle, Long areaCode, String memberId, LocalDate startDate, LocalDate endDate, String planProfileImg) { // 계획 생성일과 계획 상태를 초기화하기 위한 생성자
        this.planTitle = planTitle;
        this.areaCode = areaCode;
        this.memberId = memberId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createDate = LocalDateTime.now();
        this.status = PlanStatus.DRAFT;
        this.planProfileImg = planProfileImg;
    }

    // 전체 정보 수정을 위한 메서드
    public void updatePlan(PlanUpdateRequest request) {
        if (request.getPlanTitle() != null && !request.getPlanTitle().isEmpty()) {
            this.planTitle = request.getPlanTitle();
        }
        if (request.getStartDate() != null) {
            this.startDate = request.getStartDate();
        }
        if (request.getEndDate() != null) {
            this.endDate = request.getEndDate();
        }
        if (request.getStatus() != null) {
            this.status = request.getStatus();
        }
        if(request.getAreaCode() != null) {
            this.areaCode = request.getAreaCode();
        }

        if(request.getPlanProfileImg() != null){
            this.planProfileImg = request.getPlanProfileImg();
        }
    }

}
