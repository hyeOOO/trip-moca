package com.ssafy.enjoyTrip.api.attraction.exception;

import com.ssafy.enjoyTrip.global.exception.ErrorCode;

public class AttractionServiceException extends RuntimeException{
    private final ErrorCode errorCode;

    public AttractionServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AttractionServiceException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
