package com.lmserver.security;

import com.lmserver.config.JwtConfig;
import com.lmserver.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * JWT Token 提供者，桥接 JwtUtil 和 Spring Security。
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final JwtConfig jwtConfig;
    private JwtUtil jwtUtil;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    public void init() {
        String secret = jwtConfig.getSecret();
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new IllegalStateException(
                    "【安全错误】JWT_SECRET 不能使用默认值！请设置环境变量 JWT_SECRET。\n"
                    + "生成命令: openssl rand -base64 64");
        }
        this.jwtUtil = new JwtUtil(
                secret,
                jwtConfig.getAccessTokenExpiration(),
                jwtConfig.getRefreshTokenExpiration());
        log.info("JWT TokenProvider 初始化完成");
    }

    public String createAccessToken(Long userId, String role, String platform, int tokenVersion) {
        return jwtUtil.createAccessToken(userId, role, platform, tokenVersion);
    }

    public String createRefreshToken(Long userId, int tokenVersion) {
        return jwtUtil.createRefreshToken(userId, tokenVersion);
    }

    public Authentication getAuthentication(String token) {
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        String platform = jwtUtil.getPlatform(token);
        int tv = jwtUtil.getTokenVersion(token);

        UserPrincipal principal = new UserPrincipal(userId, "user-" + userId, role, platform, tv);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    public boolean validateToken(String token) {
        return jwtUtil.isValid(token);
    }

    public boolean validateToken(String token, int currentVersion) {
        return jwtUtil.isValid(token, currentVersion);
    }

    public Long getUserId(String token) { return jwtUtil.getUserId(token); }
    public String getRole(String token) { return jwtUtil.getRole(token); }
    public String getPlatform(String token) { return jwtUtil.getPlatform(token); }
    public int getTokenVersion(String token) { return jwtUtil.getTokenVersion(token); }
    public java.util.Date getExpiration(String token) { return jwtUtil.getExpiration(token); }
    public boolean isRefreshToken(String token) { return jwtUtil.isRefreshToken(token); }
    public long getAccessExpiration() { return jwtUtil.getAccessExpiration(); }
}
