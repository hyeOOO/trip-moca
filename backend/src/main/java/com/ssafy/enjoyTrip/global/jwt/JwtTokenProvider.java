package com.ssafy.enjoyTrip.global.jwt;

import com.ssafy.enjoyTrip.domain.member.exception.UnauthorizedException;
import com.ssafy.enjoyTrip.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.syntax.TokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // jwt 키 값 가져오기(사용자 개인 정보 암호화를 위함)
    @Value("${jwt.secret}")
    private String secretKey;

    // 액세스 토큰 만료 시간
    @Value("${jwt.access-token-validity}")
    private Long accessTokenValidityTime;

    // 리프레시 토큰 만료 시간
    @Value("${jwt.refresh-token-validity}")
    private Long refreshTokenValidityTime;

    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());

    }

    public TokenInfo generateToken(String memberId) {
        long now = System.currentTimeMillis();

        String accessToken = Jwts.builder()
                .subject(memberId)
                .expiration(new Date(now + accessTokenValidityTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();

        String refreshToken = Jwts.builder()
                .expiration(new Date(now + refreshTokenValidityTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();

        // Redis에 RefreshToken 저장
        redisTemplate.opsForValue()
                .set(memberId, refreshToken, refreshTokenValidityTime, TimeUnit.MILLISECONDS);

        return TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String getMemberId(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN, "만료된 토큰입니다.");
        } catch (JwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다.");
            throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN, "만료된 토큰입니다.");
        } catch (JwtException e) {
            log.warn("유효하지 않은 토큰입니다: {}", e.getMessage());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }

}


