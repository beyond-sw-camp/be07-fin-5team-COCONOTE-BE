package com.example.coconote.security.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // DefaultOAuth2UserService 의 loadUser 메서드를 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 로그인 제공자에 따른 사용자 정보 처리 로직
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email;
        String name;

        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
        } else {
            throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다.");
        }

        // 이메일로 DB에서 회원 확인
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 가입된 회원이 없을 경우 새로 생성
                    Member newMember = new Member();
                    newMember.setEmail(email);
                    newMember.setNickname(name);
                    return memberRepository.save(newMember);
                });

        return oAuth2User;
    }
}

