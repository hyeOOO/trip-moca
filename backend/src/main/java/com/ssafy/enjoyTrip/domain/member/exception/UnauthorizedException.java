package com.ssafy.enjoyTrip.domain.member.exception;

import com.ssafy.enjoyTrip.global.exception.ErrorCode;

public class UnauthorizedException extends RuntimeException {
    private final ErrorCode errorCode;

    public UnauthorizedException(ErrorCode errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    public UnauthorizedException(ErrorCode errorCode, String message, Throwable cause){
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode(){
        return errorCode;
    }
}
