package com.ssafy.enjoyTrip.domain.member.dto;

import com.ssafy.enjoyTrip.domain.member.entity.Gender;
import com.ssafy.enjoyTrip.domain.member.entity.Member;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class MemberResponseDto {
    private String memberId;
    private String password;
    private String memberName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;

    public static MemberResponseDto fromEntity(Member entity){
        return MemberResponseDto.builder()
                .memberId(entity.getMemberId())
                .password(entity.getPassword())
                .memberName(entity.getMemberName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .birthDate(entity.getBirthDate())
                .gender(entity.getGender())
                .build();
    }
}
