package com.login.server.domain.member.auth.dto;

import lombok.Getter;

@Getter
public class RefreshTokenRequest {
    private String refreshToken;
}
