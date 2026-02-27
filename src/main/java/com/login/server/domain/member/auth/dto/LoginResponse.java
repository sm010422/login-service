package com.login.server.login.global.member.auth.dto;

import com.login.server.login.global.member.dto.MemberResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private MemberResponse memberResponse;
}
