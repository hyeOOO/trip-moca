package com.ssafy.enjoyTrip.domain.attraction.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity(name = "CONTENT_TYPE_LIST")
public class ContentTypeList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentTypeId;
    @Column(name = "content_type_name")
    private String contentTypeName;

    @OneToMany(mappedBy = "contentType")
    private List<AttractionList> attractions;
}
