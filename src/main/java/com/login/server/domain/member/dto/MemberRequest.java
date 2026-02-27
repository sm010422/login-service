package com.login.server.login.global.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberRequest {
    private String nickname;
    private String password;
    private Double latitude;
    private Double longitude;
}
