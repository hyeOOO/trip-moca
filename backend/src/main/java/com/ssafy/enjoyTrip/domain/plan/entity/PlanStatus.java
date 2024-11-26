package com.ssafy.enjoyTrip.domain.plan.entity;

public enum PlanStatus {
    DRAFT("작성중"),
    CONFIRMED("여행확정"),
    COMPLETED("여행완료"),
    CANCELED("취소됨");

    private final String desc;

    PlanStatus(String desc){
        this.desc = desc;
    }

    public String getDesc(){
        return desc;
    }
}