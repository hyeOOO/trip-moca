package com.ssafy.enjoyTrip.domain.member.service;

import com.ssafy.enjoyTrip.domain.member.dto.MemberResponseDto;
import com.ssafy.enjoyTrip.domain.member.dto.MemberUpdateRequest;
import com.ssafy.enjoyTrip.domain.member.dto.PasswordResetRequestDto;
import com.ssafy.enjoyTrip.domain.member.entity.Member;
import com.ssafy.enjoyTrip.domain.member.exception.UnauthorizedException;
import com.ssafy.enjoyTrip.domain.member.repository.MemberRepository;
import com.ssafy.enjoyTrip.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender emailSender;

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

    @Transactional
    public void resetPassword(PasswordResetRequestDto request) {
        Member member = memberRepository.findByMemberIdAndEmail(request.getMemberId(), request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.MEMBER_NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."));

        String temporaryPassword = generateTemporaryPassword();
        member.updatePassword(passwordEncoder.encode(temporaryPassword));
        memberRepository.save(member);

        sendTemporaryPasswordEmail(member.getEmail(), temporaryPassword);
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder temporaryPassword = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            temporaryPassword.append(chars.charAt(random.nextInt(chars.length())));
        }

        return temporaryPassword.toString();
    }

    private void sendTemporaryPasswordEmail(String email, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("트립모카 - 임시 비밀번호가 발급되었습니다.");
        message.setText("안녕하세요. 트립모카입니다.\n\n" +
                "요청하신 임시 비밀번호가 발급되었습니다.\n\n" +
                "임시 비밀번호: " + temporaryPassword + "\n\n" +
                "보안을 위해 로그인 후 반드시 비밀번호를 변경해주세요.");

        emailSender.send(message);
    }
}
