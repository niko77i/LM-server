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

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(token).getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean isValid(String token) {
        return parseClaims(token) != null;
    }

    public boolean isValid(String token, int currentVersion) {
        Claims claims = parseClaims(token);
        if (claims == null) return false;
        return claims.get("tokenVersion", Integer.class) == currentVersion;
    }

    public Long getUserId(String token) {
        Claims c = parseClaims(token);
        return c != null ? Long.parseLong(c.getSubject()) : null;
    }

    public String getRole(String token) {
        Claims c = parseClaims(token);
        return c != null ? c.get("role", String.class) : null;
    }

    public String getPlatform(String token) {
        Claims c = parseClaims(token);
        return c != null ? c.get("platform", String.class) : null;
    }

    public int getTokenVersion(String token) {
        Claims c = parseClaims(token);
        return c != null ? c.get("tokenVersion", Integer.class) : 0;
    }

    public Date getExpiration(String token) {
        Claims c = parseClaims(token);
        return c != null ? c.getExpiration() : null;
    }

    public boolean isRefreshToken(String token) {
        Claims c = parseClaims(token);
        return c != null && "refresh".equals(c.get("type", String.class));
    }

    public long getAccessExpiration() { return accessExpiration; }
}
