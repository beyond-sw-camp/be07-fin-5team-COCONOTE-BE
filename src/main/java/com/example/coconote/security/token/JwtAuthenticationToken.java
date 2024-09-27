package com.example.coconote.security.token;

import com.example.coconote.security.entity.CustomPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;

public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    public JwtAuthenticationToken(CustomPrincipal customPrincipal) {
//        principal: 인증된 사용자(보통 이메일 또는 사용자 ID)를 의미합니다.
//        credentials: JWT 방식에서는 자격 증명이 필요 없기 때문에 null 로 처리합니다.
//        authorities: 사용자 권한 정보가 없거나 필요 없을 경우, 빈 리스트로 처리합니다. 권한이 필요 없을 경우 emptyList 로 처리
        super(customPrincipal, null, Collections.emptyList());
    }
}
