/**
 * JWT 核心工具 — HMAC-SHA256 Token 生成/解析/校验/字段提取，纯函数无 Spring 依赖
 */

package com.lmserver.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * JWT Token 核心工具（纯函数，无 Spring 依赖）。
 */
public class JwtUtil {

    private final SecretKey signingKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtUtil(String base64Secret, long accessExpirationMs, long refreshExpirationMs) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpiration = accessExpirationMs;
        this.refreshExpiration = refreshExpirationMs;
    }

    /** 生成 Access Token — 有效期1小时 */
    public String createAccessToken(Long userId, String role, String platform, int tokenVersion) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("platform", platform)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpiration))
                .signWith(signingKey)
                .compact();
    }

    /** 生成 Refresh Token — 有效期30天 */
    public String createRefreshToken(Long userId, int tokenVersion) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("tokenVersion", tokenVersion)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(signingKey)
                .compact();
    }

    /** 解析 Token Claims — 无效返回 null */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(token).getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    /** 校验 Token 是否有效 — 签名正确且未过期 */
    public boolean isValid(String token) {
        return parseClaims(token) != null;
    }

    /** 校验 Token 是否有效 — 签名正确且未过期 */
    public boolean isValid(String token, int currentVersion) {
        Claims claims = parseClaims(token);
        if (claims == null) return false;
        return claims.get("tokenVersion", Integer.class) == currentVersion;
    }

    /** 从 Token 提取用户 ID */
    public Long getUserId(String token) {
        Claims c = parseClaims(token);
        return c != null ? Long.parseLong(c.getSubject()) : null;
    }

    /** 从 Token 提取角色 */
    public String getRole(String token) {
        Claims c = parseClaims(token);
        return c != null ? c.get("role", String.class) : null;
    }

    /** 从 Token 提取平台 */
    public String getPlatform(String token) {
        Claims c = parseClaims(token);
        return c != null ? c.get("platform", String.class) : null;
    }

    /** 从 Token 提取版本号 */
    public int getTokenVersion(String token) {
        Claims c = parseClaims(token);
        return c != null ? c.get("tokenVersion", Integer.class) : 0;
    }

    /** 从 Token 提取过期时间 */
    public Date getExpiration(String token) {
        Claims c = parseClaims(token);
        return c != null ? c.getExpiration() : null;
    }

    /** 判断是否为 Refresh Token */
    public boolean isRefreshToken(String token) {
        Claims c = parseClaims(token);
        return c != null && "refresh".equals(c.get("type", String.class));
    }

    /** 获取 Access Token 有效期（毫秒） */
    public long getAccessExpiration() { return accessExpiration; }
}
