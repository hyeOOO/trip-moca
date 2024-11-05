package com.ssafy.enjoyTrip.api.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final int code;
    private final String message;
    private final LocalDateTime timestamp;

    public static ErrorResponse from(AttractionServiceException e) {
        return ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
