package com.ssafy.enjoyTrip.api.exception;

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
