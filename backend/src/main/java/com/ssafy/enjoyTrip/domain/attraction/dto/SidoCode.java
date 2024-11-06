package com.ssafy.enjoyTrip.domain.attraction.dto;

import com.ssafy.enjoyTrip.api.exception.AttractionServiceException;
import com.ssafy.enjoyTrip.api.exception.ErrorCode;

import java.util.stream.Stream;

public enum SidoCode {
    SEOUL("1", "서울"),
    INCHEON("2", "인천"),
    DAEJEON("3", "대전"),
    DAEGU("4", "대구"),
    GWANGJU("5", "광주"),
    BUSAN("6", "부산"),
    ULSAN("7", "울산"),
    SEJONG("8", "세종"),
    GYEONGGI("31", "경기도"),
    GANGWON("32", "강원도"),
    CHUNGBUK("33", "충청북도"),
    CHUNGNAM("34", "충청남도"),
    GYEONGBUK("35", "경상북도"),
    GYEONGNAM("36", "경상남도"),
    JEONBUK("37", "전라북도"),
    JEONNAM("38", "전라남도"),
    JEJU("39", "제주도");

    private final String code;
    private final String name;

    SidoCode(String code, String name) {
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
                .filter(sido -> sido.getCode().equals(code))
                .findFirst()
                .map(SidoCode::getName)
                .orElseThrow(() -> new AttractionServiceException(
                        ErrorCode.INVALID_SIDO_CODE,
                        String.format("유효하지 않은 시도 코드입니다: %s", code)
                ));
    }

    public static Long getCodeAsLong(String code) {
        return Stream.of(values())
                .filter(sido -> sido.getCode().equals(code))
                .findFirst()
                .map(sido -> Long.parseLong(sido.getCode()))
                .orElseThrow(() -> new AttractionServiceException(
                        ErrorCode.INVALID_SIDO_CODE,
                        String.format("유효하지 않은 시도 코드입니다: %s", code)
                ));
    }
}
