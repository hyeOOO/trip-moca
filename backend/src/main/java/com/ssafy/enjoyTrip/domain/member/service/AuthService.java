package com.ssafy.enjoyTrip.domain.member.service;

import com.ssafy.enjoyTrip.domain.member.dto.LoginRequest;
import com.ssafy.enjoyTrip.domain.member.dto.MemberSignupRequest;
import com.ssafy.enjoyTrip.domain.member.entity.Member;
import com.ssafy.enjoyTrip.domain.member.exception.UnauthorizedException;
import com.ssafy.enjoyTrip.domain.member.repository.MemberRepository;
import com.ssafy.enjoyTrip.global.exception.ErrorCode;
import com.ssafy.enjoyTrip.global.jwt.JwtTokenProvider;
import com.ssafy.enjoyTrip.global.jwt.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public void signup(MemberSignupRequest request) {
        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new UnauthorizedException(ErrorCode.DUPLICATE_MEMBER_ID,
                    "이미 사용중인 아이디입니다.");
        }

        Member member = request.toEntity();
        member.encodePassword(passwordEncoder);
        memberRepository.save(member);
        log.info("회원가입 완료: {}", member.getMemberId());
    }

    public TokenInfo login(LoginRequest request) {
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.MEMBER_NOT_FOUND,
                        "가입되지 않은 아이디입니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new UnauthorizedException(ErrorCode.INVALID_PASSWORD,
                    "비밀번호가 일치하지 않습니다.");
        }

        TokenInfo tokenInfo = jwtTokenProvider.generateToken(member.getMemberId());
        log.info("로그인 성공: {}", member.getMemberId());
        return tokenInfo;
    }

    public TokenInfo refresh(String refreshToken, String memberId) {
        String savedRefreshToken = redisTemplate.opsForValue().get(memberId);
        if (savedRefreshToken == null) {
            throw new UnauthorizedException(ErrorCode.REFRESH_TOKEN_NOT_FOUND,
                    "저장된 리프레시 토큰이 없습니다.");
        }

        if (!refreshToken.equals(savedRefreshToken)) {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN,
                    "리프레시 토큰이 유효하지 않습니다.");
        }

        TokenInfo newTokenInfo = jwtTokenProvider.generateToken(memberId);
        log.info("토큰 재발급 완료: {}", memberId);
        return newTokenInfo;
    }

    public void logout(String memberId) {
        redisTemplate.delete(memberId);
        log.info("로그아웃 완료: {}", memberId);
    }
}