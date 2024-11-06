package com.ssafy.enjoyTrip.domain.attraction.dto;

import com.ssafy.enjoyTrip.domain.attraction.entity.ContentTypeList;
import com.ssafy.enjoyTrip.domain.attraction.entity.SidoList;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ContentTypeListResponseDto {
    private Long contentTypeId;
    private String contentTypeName;

    public static ContentTypeListResponseDto fromEntity(ContentTypeList entity){
        return ContentTypeListResponseDto.builder()
                .contentTypeId(entity.getContentTypeId())
                .contentTypeName(entity.getContentTypeName())
                .build();
    }

    public static List<ContentTypeListResponseDto> fromEntities(List<ContentTypeList> entities) {
        return entities.stream()
                .map(ContentTypeListResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
