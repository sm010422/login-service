package com.login.server.domain.member.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtToken {
    private String accessToken;
    private String refreshToken;
}
