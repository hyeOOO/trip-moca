package com.ssafy.enjoyTrip.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor  // 기본 생성자 추가
@AllArgsConstructor // 모든 필드 생성자 추가
public class LoginRequest {
    @NotBlank(message = "아이디는 필수입니다")
    private String memberId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
