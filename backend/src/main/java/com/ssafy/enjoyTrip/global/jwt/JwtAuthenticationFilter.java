package com.ssafy.enjoyTrip.global.jwt;

import com.ssafy.enjoyTrip.domain.member.exception.UnauthorizedException;
import com.ssafy.enjoyTrip.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Request Header에서 JWT Token(AccessToken) 추출
        String token = resolveToken(request);
        String memberId = null;

        try {
            if (token != null) {
                try {
                    if (jwtTokenProvider.validateToken(token)) { // 액세스 토큰 검증
                        authenticateUser(token); // 액세스 토큰이 유효하면 유저 정보를 Authentication 객체에 저장
                    }
                } catch (UnauthorizedException e) {
                    // 액세스 토큰이 만료된 경우
                    if (e.getErrorCode() == ErrorCode.EXPIRED_TOKEN) {
                        try {
                            // 만료된 액세스 토큰으로 사용자 ID 가져오기
                            memberId = jwtTokenProvider.getMemberIdFromExpiredToken(token);
                            String savedRefreshToken = redisTemplate.opsForValue().get(memberId);

                            // 저장된 리프레시 토큰이 만료되지 않았다면
                             if (savedRefreshToken != null && jwtTokenProvider.validateToken(savedRefreshToken)) {
                                log.debug("액세스 토큰 재발급 시작: memberId = {}", memberId);
                                // 새로운 토큰 발급
                                TokenInfo newTokenInfo = jwtTokenProvider.generateToken(memberId);
                                log.debug("액세스 토큰 재발급 완료");

                                // 응답 헤더에 새로운 토큰 추가
                                response.setHeader("Authorization", BEARER_PREFIX + newTokenInfo.getAccessToken());
                                response.setHeader("Token-Renewed", "true");

                                // 새로운 토큰으로 인증 처리
                                authenticateUser(newTokenInfo.getAccessToken());
                            }
                        } catch (Exception refreshException) {
                            log.error("리프레시 토큰 처리 중 오류 발생", refreshException);
                            // 에러 유형에 따른 구체적인 처리 추가
                            if (refreshException instanceof UnauthorizedException) {
                                assert memberId != null;
                                redisTemplate.delete(memberId);  // 리프레시 토큰 삭제
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token) {
        String memberId = jwtTokenProvider.getMemberId(token);

        UserDetails userDetails = User.builder()
                .username(memberId)
                .password("")
                .authorities(Collections.emptyList())
                .build();

        // SecurityContext에 Authentication 객체 저장
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Security Context에 '{}' 인증 정보를 저장했습니다", memberId);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}