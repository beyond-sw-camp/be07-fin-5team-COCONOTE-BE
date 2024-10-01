package com.example.coconote.security.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.workspace.workspace.dto.request.WorkspaceCreateReqDto;
import com.example.coconote.api.workspace.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final WorkspaceService workspaceService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // DefaultOAuth2UserService 의 loadUser 메서드를 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 로그인 제공자에 따른 사용자 정보 처리 로직
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes()); // 불변 Map을 수정 가능한 Map으로 복사
        String email;
        String name;

        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if ("kakao".equals(registrationId)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
            @SuppressWarnings("unchecked")
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
        } else {
            throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다.");
        }

        // 이메일로 DB 에서 회원 확인
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 가입된 회원이 없을 경우 새로 생성
                    Member newMember = Member.builder()
                            .email(email)
                            .nickname(name)
                            .build();
                    Member tempMember = memberRepository.save(newMember);
                    workspaceService.workspaceCreate(new WorkspaceCreateReqDto("My Workspace", ""), email);
                    return tempMember;
                });

        attributes.put("email", email);  // email 속성 추가
        attributes.put("memberId", member.getId()); // 사용자 정보에 memberId 추가

        // memberId가 포함된 OAuth2User 반환
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "email"  // OAuth2User 의 식별자로 사용할 속성
        );
    }
}