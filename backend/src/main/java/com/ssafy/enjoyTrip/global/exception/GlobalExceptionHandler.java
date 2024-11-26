package com.ssafy.enjoyTrip.global.exception;

import com.ssafy.enjoyTrip.api.attraction.exception.AttractionServiceException;
import com.ssafy.enjoyTrip.domain.member.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(AttractionServiceException.class)
    public ResponseEntity<ErrorResponse> handleAttractionServiceException(
            AttractionServiceException e) {
        log.error("Attraction service error occurred", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(e));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        log.error("Unauthorized error occurred: {}", e.getMessage(), e);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                        .message(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

}
