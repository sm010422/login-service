package com.login.server.domain.member.entity;

import com.login.server.domain.member.enums.SocialType;
import com.login.server.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_pk")
    private Long memberPk;

    @Column(name = "member_id", nullable = false, unique = true, length = 255)
    private String memberId;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 255)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name="social_type",nullable = false)
    private SocialType socialType;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    @Column(name = "delete_yn", length = 1)
    private String deleteYn;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public void markAsBlind() {
        this.deleteYn = "B";
    }
}
