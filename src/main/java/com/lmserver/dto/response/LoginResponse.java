package com.lmserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
/**
 * 登录成功响应 — accessToken(1小时) + refreshToken(30天) + 用户信息(id/username/role/platform/displayName)
 */

/**
 * 登录成功响应 — accessToken(1小时) + refreshToken(30天) + 用户信息(id/username/role/platform/displayName)
 */

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long id;
        private String username;
        private String role;
        private String platform;
        private String displayName;
    }
}
