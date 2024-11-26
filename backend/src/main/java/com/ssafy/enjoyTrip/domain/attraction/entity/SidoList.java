package com.ssafy.enjoyTrip.domain.attraction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ssafy.enjoyTrip.domain.card.entity.Card;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Entity(name="SIDO_LIST")
public class SidoList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    @Column(name = "sido_code", unique = true)
    private Long sidoCode;

    @Column(name = "sido_name")
    private String sidoName;

    @OneToOne(mappedBy = "area")
    @JsonIgnore  // JSON 직렬화 시 순환 참조 방지
    private Card card;

    // 양방향 관계가 필요한 경우 추가
    @OneToMany(mappedBy = "sido")
    @JsonIgnore  // JSON 직렬화 시 순환 참조 방지
    private List<AttractionList> attractions;

    // ToString 무한순환 방지
    @Override
    public String toString() {
        return "SidoList{" +
                "no=" + no +
                ", sidoCode=" + sidoCode +
                ", sidoName='" + sidoName + '\'' +
                '}';
    }
}