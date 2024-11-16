package com.ssafy.enjoyTrip.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_AI_RESPONSE(1001, "AI 응답 처리 중 오류가 발생했습니다"),
    ATTRACTION_NOT_FOUND(1002, "관광지 정보를 찾을 수 없습니다"),
    AI_SERVICE_ERROR(1003, "AI 서비스 처리 중 오류가 발생했습니다"),
    INVALID_SIDO_CODE(1004, "잘못된 시/도 코드입니다"),
    INTERNAL_SERVER_ERROR(5000, "서버 내부 오류가 발생했습니다"),
    INVALID_TOKEN(4001, "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(4002, "만료된 토큰입니다"),
    INVALID_REFRESH_TOKEN(4003, "유효하지 않은 리프레시 토큰입니다"),
    TOKEN_NOT_FOUND(4004, "토큰이 존재하지 않습니다"),
    REFRESH_TOKEN_NOT_FOUND(4005, "저장된 리프레시 토큰이 없습니다"),
    DUPLICATE_MEMBER_ID(2001, "이미 사용중인 아이디입니다"),
    MEMBER_NOT_FOUND(2002, "가입되지 않은 아이디입니다"),
    INVALID_PASSWORD(2003, "비밀번호가 일치하지 않습니다"),
    DUPLICATE_EMAIL(2004, "이미 사용중인 이메일입니다");


    private final int code;
    private final String defaultMessage;
}
