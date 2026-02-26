package com.login.server.global.member.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank
    private String memberId;

    @NotBlank
    private String password;
}
