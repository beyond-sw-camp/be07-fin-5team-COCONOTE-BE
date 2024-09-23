package com.example.coconote.security.controller;

import com.example.coconote.security.token.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/token")
    public ResponseEntity<String> generateToken( @AuthenticationPrincipal OAuth2User oAuth2User) {

        // SecurityContext에서 Authentication 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String registrationId = null;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            registrationId = oauthToken.getAuthorizedClientRegistrationId();  // 로그인 제공자 ID 가져오기
        }

        String email = null;

        // 구글 사용자
        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
        }
        // 카카오 사용자
        else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
            }
        }

        // OAuth2 로그인 성공 후 사용자의 이메일을 가져옴
        if (email == null) {
            return ResponseEntity.badRequest().body("Failed to retrieve user information.");
        }

        // 액세스 토큰 및 리프레시 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // 성공 응답 전송
        // JSON 형식으로 액세스 토큰과 리프레시 토큰 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return ResponseEntity.ok(tokens.toString());
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(@RequestBody Map<String, String> requestBody) {

        // HTTP 바디에서 리프레시 토큰 추출
        String refreshToken = requestBody.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("refresh token is null");
        } else if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        // 리프레시 토큰이 유효하다면 새로운 액세스 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(jwtTokenProvider.getEmailFromToken(refreshToken));

        // 새로 발급된 액세스 토큰을 클라이언트에게 반환
        return ResponseEntity.ok(newAccessToken);
    }
}
