package com.example.coconote.security.token;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final String frontUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // Principal 에서 OAuth2 사용자 정보 가져오기
        DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = (String) oauth2User.getAttribute("email");

        // memberId 가져오기
        Long memberId = (Long) oauth2User.getAttribute("memberId");  // CustomOAuth2UserService 에서 추가한 memberId 사용

        // Access Token 및 Refresh Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(email, memberId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email, memberId);

        // 클라이언트에 리다이렉트할 URL 에 토큰을 쿼리 파라미터로 추가
        String redirectUrl = frontUrl + "/oauth2/success?accessToken=" + accessToken + "&refreshToken=" + refreshToken;

        // 해당 URL 로 리다이렉트
        response.sendRedirect(redirectUrl);
    }
}
