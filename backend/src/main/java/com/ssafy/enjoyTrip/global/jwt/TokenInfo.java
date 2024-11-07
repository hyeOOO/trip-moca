package com.ssafy.enjoyTrip.global.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenInfo {
    private String accessToken;
    private String refreshToken;
}
