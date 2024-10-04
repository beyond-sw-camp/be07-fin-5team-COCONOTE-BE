package com.example.coconote.api.member.controller;

import com.example.coconote.api.channel.channelMember.dto.response.ChannelMemberListResDto;
import com.example.coconote.api.member.service.MemberService;
import com.example.coconote.api.workspace.workspaceMember.dto.response.WorkspaceMemberResDto;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.util.CustomPrincipal;
import com.example.coconote.security.token.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    @Operation(summary= "나의 워크스페이스 내에서의 정보 반환")
    @GetMapping("/me/workspace/{workspaceId}")
    public ResponseEntity<Object> getWorkspaceMemberInfo(@PathVariable Long workspaceId, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        WorkspaceMemberResDto workspaceMemberResDto = memberService.getWorkspaceMemberInfo(workspaceId, customPrincipal.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member is successfully found", workspaceMemberResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    @Operation(summary= "나의 워크스페이스 내에서의 정보 반환")
    @GetMapping("/me/channel/{channelId}")
    public ResponseEntity<Object> getChannelMemberInfo(@PathVariable Long channelId, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        ChannelMemberListResDto channelMemberListResDto = memberService.getChannelMemberInfo(channelId, customPrincipal.getEmail());
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member is successfully found", channelMemberListResDto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    // 토큰에서 memberId와 email 확인하는 API 추가
    @GetMapping("/check-token")
    public ResponseEntity<String> checkTokenMemberInfo(@RequestHeader("Authorization") String token) {
        // Bearer 토큰 형식에서 "Bearer " 부분 제거
        String jwtToken = token.replace("Bearer ", "");
        System.out.println("jwtToken = " + jwtToken);

        // 토큰에서 memberId와 email 추출
        Long memberId = jwtTokenProvider.getMemberIdFromToken(jwtToken);
        String email = jwtTokenProvider.getEmailFromToken(jwtToken);

        if (memberId != null && email != null) {
            return ResponseEntity.ok("Member ID from token: " + memberId + ", Email: " + email);
        } else if (memberId == null && email != null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("memberId is null.");
        } else if (memberId != null && email == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email is null.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email and memberId is null.");
        }
    }
}
