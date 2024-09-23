package com.example.coconote.api.member.controller;

import com.example.coconote.security.entity.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    @GetMapping("/me")
    public String getMemberInfo(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return "Logged in user: " + customPrincipal.getEmail() + ", Member ID: " + customPrincipal.getMemberId() + ", Member Nickname: " + customPrincipal.getNickname();
    }
}
