package com.ssafy.enjoyTrip.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity(name = "CONTENT_TYPE_LIST")
@NoArgsConstructor
public class ContentTypeList {
    @Id
    @Column(name = "content_type_id")  // 컬럼명 명시
    private Long contentTypeId;        // @GeneratedValue 제거

    @Column(name = "content_type_name")
    private String contentTypeName;

    @OneToMany(mappedBy = "contentType")
    private List<AttractionList> attractions;
}
