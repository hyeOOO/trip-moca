package com.ssafy.enjoyTrip.domain.member.controller;

import com.ssafy.enjoyTrip.domain.member.dto.MemberResponseDto;
import com.ssafy.enjoyTrip.domain.member.dto.MemberUpdateRequest;
import com.ssafy.enjoyTrip.domain.member.dto.PasswordCheckRequest;
import com.ssafy.enjoyTrip.domain.member.service.MemberService;
import com.ssafy.enjoyTrip.global.annotation.CurrentMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Member", description = "회원 관리 관련 API 입니다")
@RestController
@RequestMapping("/domain/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "회원정보 조회")
    @GetMapping("/info")
    public ResponseEntity<MemberResponseDto> userInfo(@CurrentMemberId String memberId) {
        MemberResponseDto memberInfo = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(memberInfo);
    }

    @Operation(summary = "회원정보 수정")
    @PatchMapping("/update")
    public ResponseEntity<String> userUpdate(
            @CurrentMemberId String memberId,
            @RequestBody MemberUpdateRequest request) {
        memberService.updateMember(memberId, request);
        return ResponseEntity.ok("회원정보가 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/delete")
    public ResponseEntity<String> userDelete(@CurrentMemberId String memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    @Operation(summary = "비밀번호 일치여부")
    @PostMapping("/check-password")  // POST로 변경 (보안상 GET 부적절)
    public ResponseEntity<Boolean> passwordValidCheck(
            @CurrentMemberId String memberId,
            @RequestBody PasswordCheckRequest request) {
        boolean isValid = memberService.checkPassword(memberId, request.getPassword());
        return ResponseEntity.ok(isValid);
    }
}
