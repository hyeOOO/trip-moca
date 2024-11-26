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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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


    public TokenInfo generateToken(String memberId) {
        long now = System.currentTimeMillis();

        // 액세스토큰은 유저의 memberId를 포함한 채 암호화
        String accessToken = Jwts.builder()
                .subject(memberId)
                .expiration(new Date(now + accessTokenValidityTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();

        // 리프레시 토큰은 액세스토큰 재발급을 위한 키이므로 정보 포함 x
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

    // 토큰에서 memberId 추출
    public String getMemberId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(convertSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN, "만료된 토큰입니다.");
        } catch (JwtException e) {
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }

    private SecretKey convertSecretKey() {
        return new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(convertSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다.");
            throw new UnauthorizedException(ErrorCode.EXPIRED_TOKEN, "만료된 토큰입니다.");
        } catch (JwtException e) {
            log.warn("유효하지 않은 토큰입니다: {}", e.getMessage());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }

    // 만료된 액세스토큰에서 memberId 뽑아내기
    public String getMemberIdFromExpiredToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(convertSecretKey()).build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

}


