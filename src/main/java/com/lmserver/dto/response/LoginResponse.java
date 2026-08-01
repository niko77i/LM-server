package com.lmserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 登录成功响应。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    private LoginResponse() {}

    public static LoginResponse of(String accessToken, String refreshToken, UserInfo user) {
        LoginResponse r = new LoginResponse();
        r.accessToken = accessToken;
        r.refreshToken = refreshToken;
        r.user = user;
        return r;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public UserInfo getUser() { return user; }

    /**
     * 登录响应中的用户信息。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long id;
        private String username;
        private String role;
        private String platform;
        private String displayName;

        public static UserInfo of(Long id, String username, String role, String platform, String displayName) {
            UserInfo u = new UserInfo();
            u.id = id;
            u.username = username;
            u.role = role;
            u.platform = platform;
            u.displayName = displayName;
            return u;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getPlatform() { return platform; }
        public String getDisplayName() { return displayName; }
    }
}
