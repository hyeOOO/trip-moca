package com.ssafy.enjoyTrip.domain.attraction.dto;

import com.ssafy.enjoyTrip.domain.attraction.entity.GugunList;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class GugunListResponseDto {
    private Long sidoCode;
    private Long gugunCode;
    private String gugunName;

    public static GugunListResponseDto fromEntity(GugunList entity){
        return GugunListResponseDto.builder()
                .sidoCode(entity.getSidoCode())
                .gugunCode(entity.getGugunCode())
                .gugunName(entity.getGugunName())
                .build();
    }

    public static List<GugunListResponseDto> fromEntities(List<GugunList> entities){
        return entities.stream()
                .map(GugunListResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
