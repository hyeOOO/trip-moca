package com.ssafy.enjoyTrip.domain.member.service;

import com.ssafy.enjoyTrip.domain.member.dto.MemberResponseDto;
import com.ssafy.enjoyTrip.domain.member.dto.MemberUpdateRequest;
import com.ssafy.enjoyTrip.domain.member.entity.Member;
import com.ssafy.enjoyTrip.domain.member.repository.MemberRepository;
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
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    public MemberResponseDto getMemberInfo(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        return MemberResponseDto.fromEntity(member);
    }

    public void updateMember(String memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
        if(passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())){
            member.update(request, passwordEncoder);
            memberRepository.save(member);
        }else{
            throw new RuntimeException("회원정보 수정에 실패했습니다.");
        }
    }

    public void deleteMember(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        redisTemplate.delete(member.getMemberId()); // redis도 삭제
        memberRepository.delete(member);
    }

    public boolean checkPassword(String memberId, String rawPassword) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        return passwordEncoder.matches(rawPassword, member.getPassword());
    }
}
