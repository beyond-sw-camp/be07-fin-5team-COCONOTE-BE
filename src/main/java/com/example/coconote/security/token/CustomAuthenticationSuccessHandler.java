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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // Principal 에서 OAuth2 사용자 정보 가져오기
        DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = (String) oauth2User.getAttribute("email");

        // Access Token 및 Refresh Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // 클라이언트에 리다이렉트할 URL 에 토큰을 쿼리 파라미터로 추가
        String redirectUrl = "http://localhost:8082/oauth2/success?accessToken=" + accessToken + "&refreshToken=" + refreshToken;

        // 해당 URL 로 리다이렉트
        response.sendRedirect(redirectUrl);
//
//        // 리디렉션 없이 JSON 형태로 응답 반환
//        response.setStatus(HttpServletResponse.SC_OK);
//        response.setContentType("application/json");
//
//        // Access Token 과 Refresh Token 을 JSON 으로 반환
//        response.getWriter().write("{\"accessToken\": \"" + accessToken + "\", \"refreshToken\": \"" + refreshToken + "\"}");
    }
}
