// package com.login.server.global.member.auth.dto;
package com.login.server.login.global.member.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtToken {
    private String accessToken;
    private String refreshToken;
}
