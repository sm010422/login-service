package com.login.server.domain.member.auth.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.login.server.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authPk;

    @ManyToOne
    @JoinColumn(name = "member_pk")
    private Member member;

    @Column(nullable = false)
    private String refreshToken;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 1)
    private String deleteYn = "N";

    public Auth(Member member, String refreshToken) {
        this.member = member;
        this.refreshToken = refreshToken;
        this.deleteYn = "N";
    }

    public void updateToken(String newToken) {
        this.refreshToken = newToken;
        this.createdAt = LocalDateTime.now();
    }

    public void invalidateToken() {
        this.refreshToken = "logout";
    }

    public void deleted() {
        this.deleteYn = "Y";
    }
}
