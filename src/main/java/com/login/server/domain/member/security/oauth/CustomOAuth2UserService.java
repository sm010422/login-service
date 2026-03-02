package com.login.server.domain.member.security.oauth;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.login.server.domain.member.entity.Member;
import com.login.server.domain.member.enums.SocialType;
import com.login.server.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String providerId = null;
        String email = null;
        String nickname = null;
        SocialType socialType = null;

        if ("google".equals(registrationId)) {
            socialType = SocialType.GOOGLE;
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
        } 
        else if ("kakao".equals(registrationId)) {
            socialType = SocialType.KAKAO;
            providerId = String.valueOf(attributes.get("id")); // 카카오는 Long 타입이라 문자열 변환 필요

            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    nickname = (String) profile.get("nickname");
                }
            }
        } 
        else if ("naver".equals(registrationId)) {
            socialType = SocialType.NAVER;
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null) {
                providerId = (String) response.get("id");
                email = (String) response.get("email");
                nickname = (String) response.get("name");
            }
        }

        // 필수 정보 검증: email이 없으면 로그인을 진행할 수 없음
        if (email == null || providerId == null) {
            throw new OAuth2AuthenticationException("소셜 로그인 실패: 필수 정보(이메일/아이디) 누락");
        }

        final SocialType finalSocialType = socialType;
        final String finalProviderId = providerId;
        final String finalEmail = email;
        final String finalNickname = (nickname != null) ? nickname : "임시닉네임";

        // 기존 회원이면 조회, 신규 회원이면 빌더로 생성 후 저장
        Member member = memberRepository.findByEmail(finalEmail)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .email(finalEmail)
                                .socialType(finalSocialType)
                                .memberId(finalProviderId)
                                .password("temp_password") // OAuth 유저는 패스워드가 불필요하지만 Not Null 대비
                                .nickname(finalNickname)
                                .role("ROLE_USER")
                                .useYn("Y")
                                .deleteYn("N")
                                .build()
                ));

        return new CustomOAuth2User(member, attributes);
    }
}
