package com.example.coconote.api.member.controller;

import com.example.coconote.security.util.CustomPrincipal;
import com.example.coconote.security.token.JwtTokenProvider;
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

    @GetMapping("/me")
    public String getMemberInfo(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return "Logged in user: " + customPrincipal.getEmail() + ", Member ID: " + customPrincipal.getMemberId() + ", Member Nickname: " + customPrincipal.getNickname();
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
