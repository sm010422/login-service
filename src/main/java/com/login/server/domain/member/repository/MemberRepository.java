package com.login.server.domain.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.login.server.domain.member.entity.Member;
import com.login.server.domain.member.enums.SocialType;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberPk(Long memberPk);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByEmailAndSocialType(String email, SocialType socialType);
    boolean existsByEmail(String email);
    boolean existsByMemberId(String memberId);
    List<Member> findByDeleteYn(String deleteYn);

    /** 관리자 memberId로 특정 멤버 검색**/
    Page<Member> findByMemberIdContaining(String memberId, Pageable pageable);
    Page<Member> findByNicknameContaining(String nickname, Pageable pageable);
    Page<Member> findByEmailContaining(String email, Pageable pageable);
}
