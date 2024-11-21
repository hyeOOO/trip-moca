package com.ssafy.enjoyTrip.domain.attraction.dto;

import com.ssafy.enjoyTrip.api.attraction.exception.AttractionServiceException;
import com.ssafy.enjoyTrip.global.exception.ErrorCode;

import java.util.stream.Stream;

public enum ContentType {
    ATTRACTION("12", "관광지"),
    CULTURE("14", "문화시설"),
    FESTIVAL("15", "축제공연행사"),
    DAEGU("25", "여행코스"),
    GWANGJU("28", "레포츠"),
    BUSAN("32", "숙박"),
    ULSAN("38", "쇼핑"),
    SEJONG("39", "음식점");

    private final String code;
    private final String name;

    ContentType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        return Stream.of(values())
                .filter(contentType -> contentType.getCode().equals(code))
                .findFirst()
                .map(ContentType::getName)
                .orElseThrow(() -> new AttractionServiceException(
                        ErrorCode.INVALID_CONTENT_TYPE_CODE,
                        String.format("유효하지 않은 컨텐츠 타입 코드입니다: %s", code)
                ));
    }

    public static Long getCodeAsLong(String code) {
        return Stream.of(values())
                .filter(contentType -> contentType.getCode().equals(code))
                .findFirst()
                .map(contentType -> Long.parseLong(contentType.getCode()))
                .orElseThrow(() -> new AttractionServiceException(
                        ErrorCode.INVALID_CONTENT_TYPE_CODE,
                        String.format("유효하지 않은 컨텐츠 타입 코드입니다: %s", code)
                ));
    }
}
