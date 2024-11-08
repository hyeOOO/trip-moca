package com.ssafy.enjoyTrip.domain.member.controller;

import com.ssafy.enjoyTrip.domain.member.dto.LoginRequest;
import com.ssafy.enjoyTrip.domain.member.dto.MemberSignupRequest;
import com.ssafy.enjoyTrip.domain.member.exception.UnauthorizedException;
import com.ssafy.enjoyTrip.domain.member.service.AuthService;
import com.ssafy.enjoyTrip.global.exception.ErrorCode;
import com.ssafy.enjoyTrip.global.jwt.JwtTokenProvider;
import com.ssafy.enjoyTrip.global.jwt.TokenInfo;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "로그인 관련 API입니다.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Operation(summary = "회원가입", description = "회원정보 입력 후 회원가입을 위한 API 입니다.(토큰 검사X)")
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

    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인하는 API 입니다.(토큰 검사X)")
    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "로그아웃", description = "레디스의 리프레시 키를 삭제하는 로그아웃 API 입니다.(토큰 검사O)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        String memberId = userDetails.getUsername();
        authService.logout(memberId);
        return ResponseEntity.ok().build();
    }
}