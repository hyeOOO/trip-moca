package com.ssafy.enjoyTrip.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {
    private String memberId;
    private String email;
}
