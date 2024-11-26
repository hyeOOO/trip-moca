package com.ssafy.enjoyTrip.domain.attraction.dto;

import com.ssafy.enjoyTrip.domain.attraction.entity.SidoList;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class SidoListResponseDto {
    private Long sidoCode;
    private String sidoName;

    public static SidoListResponseDto fromEntity(SidoList entity){
        return SidoListResponseDto.builder()
                .sidoCode(entity.getSidoCode())
                .sidoName(entity.getSidoName())
                .build();
    }

    public static List<SidoListResponseDto> fromEntities(List<SidoList> entities) {
        return entities.stream()
                .map(SidoListResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
