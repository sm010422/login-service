package com.login.server.domain.member.auth.dto;

import com.login.server.domain.member.dto.MemberResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private MemberResponse memberResponse;
}
