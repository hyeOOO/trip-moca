package com.ssafy.enjoyTrip.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_AI_RESPONSE(1001, "AI 응답 처리 중 오류가 발생했습니다"),
    ATTRACTION_NOT_FOUND(1002, "관광지 정보를 찾을 수 없습니다"),
    AI_SERVICE_ERROR(1003, "AI 서비스 처리 중 오류가 발생했습니다"),
    INVALID_SIDO_CODE(1004, "잘못된 시도 코드입니다");

    private final int code;
    private final String defaultMessage;
}
