package com.login.server.login.global.member.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.login.server.login.global.member.Entity.Member;
import com.login.server.login.global.member.auth.Entity.Auth;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByMemberAndDeleteYn(Member member, String deleteYn);

    Optional<Auth> findByRefreshTokenAndDeleteYn(String refreshToken, String deleteYn);

    Optional<Auth> findByMemberAndRefreshTokenNotAndDeleteYn(Member member, String refreshToken, String deleteYn);
}
