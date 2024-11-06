package com.ssafy.enjoyTrip.domain.attraction.entity;

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

    // 양방향 관계가 필요한 경우 추가
    @OneToMany(mappedBy = "sido")
    private List<AttractionList> attractions;
}