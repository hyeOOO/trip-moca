package com.ssafy.enjoyTrip.domain.member.dto;

import com.ssafy.enjoyTrip.domain.member.entity.Gender;
import com.ssafy.enjoyTrip.domain.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor  // 기본 생성자 추가
@AllArgsConstructor // 모든 필드 생성자 추가
@Builder
public class MemberSignupRequest {
    @NotBlank(message = "아이디는 필수입니다")
    private String memberId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    private String email;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;

    public Member toEntity() {
        return Member.builder()
                .memberId(memberId)
                .password(password)
                .memberName(name)
                .email(email)
                .phone(phone)
                .birthDate(birthDate)
                .gender(gender)
                .build();
    }
}