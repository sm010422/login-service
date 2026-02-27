package com.login.server.login.global.member.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.login.server.login.global.member.Entity.Member;
import com.login.server.login.global.member.auth.Entity.Auth;
import com.login.server.login.global.member.auth.dto.JwtToken;
import com.login.server.login.global.member.auth.dto.LoginRequest;
import com.login.server.login.global.member.auth.dto.LoginResponse;
import com.login.server.login.global.member.auth.dto.RefreshTokenRequest;
import com.login.server.login.global.member.auth.repository.AuthRepository;
import com.login.server.login.global.member.dto.MemberResponse;
import com.login.server.login.global.member.jwt.JwtTokenProvider;
import com.login.server.login.global.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRepository authRepository;

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        if(!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getMemberId());
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        //기존 Auth 존재하면 refreshToken 업데이트, 없으면 저장
        authRepository.findByMemberAndDeleteYn(member, "N")
                .ifPresentOrElse(
                        auth -> auth.updateToken(refreshToken),
                        () -> authRepository.save(new Auth(member, refreshToken))
                );

        return new LoginResponse(accessToken, MemberResponse.from(member));
    }

    public JwtToken reissue(RefreshTokenRequest request) {
        String oldRefreshToken = request.getRefreshToken();

        if(!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 refreshToken 입니다.");
        }

        Auth auth = authRepository.findByRefreshTokenAndDeleteYn(oldRefreshToken,"N")
                .orElseThrow(() -> new IllegalArgumentException("저장된 refreshToken이 없습니다."));

        if("logout".equals(auth.getRefreshToken())) {
            throw new IllegalArgumentException("이미 로그아웃된 refreshToken 입니다.");
        }

        Member member = auth.getMember();

        String newAccessToken = jwtTokenProvider.generateAccessToken(member.getMemberId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();

        auth.updateToken(newRefreshToken);

        return new JwtToken(newAccessToken, newRefreshToken);
    }

    public void logout(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        authRepository.findByMemberAndRefreshTokenNotAndDeleteYn(member, "logout", "N")
                .ifPresent(Auth::invalidateToken);
    }
}
