package com.lmserver.security;

import com.lmserver.config.JwtConfig;
import com.lmserver.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
/**
 * JWT Token 提供者 — 桥接 JwtUtil 与 Spring Security，启动时校验密钥强度，拒绝弱默认密钥
 */

@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private JwtUtil jwtUtil;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    /** 初始化 JwtUtil — 从配置读取密钥和过期时间 */
    public void init() {
        String secret = jwtConfig.getSecret();
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new IllegalStateException(
                    "【安全错误】JWT_SECRET 不能使用默认值！\n"
                    + "生成命令: openssl rand -base64 64");
        }
        this.jwtUtil = new JwtUtil(secret,
                jwtConfig.getAccessTokenExpiration(),
                jwtConfig.getRefreshTokenExpiration());
        log.info("JWT TokenProvider 初始化完成");
    }
    /** 生成 Access Token — 有效期1小时 */
    public String createAccessToken(Long userId, String role, String platform, int tokenVersion) {
        return jwtUtil.createAccessToken(userId, role, platform, tokenVersion);
    }
    /** 生成 Refresh Token — 有效期30天 */
    public String createRefreshToken(Long userId, int tokenVersion) {
        return jwtUtil.createRefreshToken(userId, tokenVersion);
    }
    /** 从 Token 构建 Authentication — 设置 SecurityContext */
    public Authentication getAuthentication(String token) {
        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        String platform = jwtUtil.getPlatform(token);
        int tv = jwtUtil.getTokenVersion(token);
        UserPrincipal principal = new UserPrincipal(userId, "user-" + userId, role, platform, tv);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
    /** 校验 Token 签名和有效期 */
    public boolean validateToken(String token) { return jwtUtil.isValid(token); }
    /** 校验 Token 签名和有效期 */
    public boolean validateToken(String token, int v) { return jwtUtil.isValid(token, v); }
    /** 从 Token 提取用户 ID */
    public Long getUserId(String token) { return jwtUtil.getUserId(token); }
    /** 从 Token 提取角色 */
    public String getRole(String token) { return jwtUtil.getRole(token); }
    /** 从 Token 提取平台 */
    public String getPlatform(String token) { return jwtUtil.getPlatform(token); }
    /** 从 Token 提取版本号 */
    public int getTokenVersion(String token) { return jwtUtil.getTokenVersion(token); }
    /** 从 Token 提取过期时间 */
    public Date getExpiration(String token) { return jwtUtil.getExpiration(token); }
    /** 判断是否为 Refresh Token */
    public boolean isRefreshToken(String token) { return jwtUtil.isRefreshToken(token); }
    /** 获取 Access Token 有效期（毫秒） */
    public long getAccessExpiration() { return jwtUtil.getAccessExpiration(); }
}
