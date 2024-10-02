package com.example.coconote.security.filter;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.security.entity.CustomPrincipal;
import com.example.coconote.security.token.JwtAuthenticationToken;
import com.example.coconote.security.token.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;  // 사용자 정보를 가져오기 위한 레포지토리 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Authorization 헤더에서 JWT 토큰을 가져옴
        String token = request.getHeader("Authorization");
        System.out.println("token : " + token);
        if (token != null && token.startsWith("Bearer ")) {
            // 'Bearer ' 부분을 제거하고 토큰만 추출
            String jwt = token.substring(7);
            // 토큰에서 이메일을 추출하고 유효성 검사
            String email = jwtTokenProvider.getEmailFromToken(jwt);
            if (email != null && jwtTokenProvider.validateToken(jwt)) {
                // 토큰이 유효하면 SecurityContext 에 인증 정보를 설정
                // SecurityContextHolder 는 인증 객체(Authentication)를 저장한다
                // 스프링 시큐리티에서 현재 인증된 사용자의 정보를 관리하는 컨텍스트입니다.
                // 이 컨텍스트에는 사용자의 **인증 정보(Authentication 객체)**를 저장하여, 애플리케이션 전반에서 사용자 정보를 참조할 수 있도록 합니다.
                // 스프링 시큐리티는 인증된 사용자 정보를 Authentication 객체를 통해 관리하기 때문에, Authentication 객체로 만들어야 합니다.
                // 사용자 정보를 단순히 문자열로 저장하는 것이 아니라, 스프링 시큐리티에서 요구하는 형식에 맞게 Authentication 객체로 래핑
                // JwtAuthenticationToken 클래스는 스프링 시큐리티에서 제공하는 **UsernamePasswordAuthenticationToken**을 상속하여 JWT 인증 처리를 위한 커스텀 객체
//                JwtAuthenticationToken authentication = new JwtAuthenticationToken(email);
//                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 이메일로 DB에서 사용자 정보 조회
                Member member = memberRepository.findByEmail(email).orElse(null);
                if (member != null) {
                    // CustomPrincipal 객체로 사용자 정보를 SecurityContextHolder에 저장
                    CustomPrincipal customPrincipal = new CustomPrincipal(email, member.getId(), member.getNickname());
                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(customPrincipal); // 권한 설정은 나중에 추가
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}

