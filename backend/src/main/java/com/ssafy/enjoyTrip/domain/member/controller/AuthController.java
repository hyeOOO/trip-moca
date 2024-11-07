package com.ssafy.enjoyTrip.domain.member.controller;

import com.ssafy.enjoyTrip.domain.member.dto.LoginRequest;
import com.ssafy.enjoyTrip.domain.member.dto.MemberSignupRequest;
import com.ssafy.enjoyTrip.domain.member.exception.UnauthorizedException;
import com.ssafy.enjoyTrip.domain.member.service.AuthService;
import com.ssafy.enjoyTrip.global.exception.ErrorCode;
import com.ssafy.enjoyTrip.global.jwt.JwtTokenProvider;
import com.ssafy.enjoyTrip.global.jwt.TokenInfo;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody MemberSignupRequest request) {
        authService.signup(request);
        log.info("회원가입 요청 성공: {}", request.getMemberId());
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }


    @PostMapping("/refresh")
    public ResponseEntity<TokenInfo> refresh(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("Refresh-Token") String refreshToken) {

        try {
            // Bearer 제거
            accessToken = accessToken.substring(7);

            // Redis에서 refreshToken 검증 후 새 토큰 발급
            String memberId = jwtTokenProvider.getMemberId(accessToken);
            String savedRefreshToken = redisTemplate.opsForValue().get(memberId);

            if (savedRefreshToken == null) {
                throw new UnauthorizedException(ErrorCode.TOKEN_NOT_FOUND, "저장된 리프레시 토큰이 없습니다.");
            }

            if (!refreshToken.equals(savedRefreshToken)) {
                throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN, "리프레시 토큰이 일치하지 않습니다.");
            }

            // 새 토큰 발급
            TokenInfo newTokenInfo = jwtTokenProvider.generateToken(memberId);

            log.debug("Token refreshed for user: {}", memberId);
            return ResponseEntity.ok(newTokenInfo);

        } catch (JwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "토큰 처리 중 오류가 발생했습니다.", e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal String memberId) {
        authService.logout(memberId);
        return ResponseEntity.ok().build();
    }
}