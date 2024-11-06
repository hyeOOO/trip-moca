package com.ssafy.enjoyTrip.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Entity(name="GUGUN_LIST")
public class GugunList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    @Column(name = "sido_code")
    private Long sidoCode;

    @Column(name = "gugun_code")
    private Long gugunCode;

    @Column(name = "gugun_name")
    private String gugunName;

    // 양방향 관계가 필요한 경우 추가
    @OneToMany(mappedBy = "gugun")
    private List<AttractionList> attractions;
}
