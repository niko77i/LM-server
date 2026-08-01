package com.lmserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.dto.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
/**
 * JWT 认证过滤器 — 从 Authorization 头提取 Bearer Token，校验后设置 SecurityContext，剩余有效期不足30%时自动续签
 */

/**
 * JWT 认证过滤器 — 从 Authorization 头提取 Bearer Token，校验后设置 SecurityContext，剩余有效期不足30%时自动续签
 */

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/health");

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (PUBLIC_PATHS.contains(path)) return true;
        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) return true;
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            sendUnauthorized(response, token == null ? "缺少认证 Token" : "Token 无效或已过期");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                jwtTokenProvider.getAuthentication(token));

        // 滑动过期：剩余 < 30% 时签发新 Token
        Date expiration = jwtTokenProvider.getExpiration(token);
        if (expiration != null) {
            long remaining = expiration.getTime() - System.currentTimeMillis();
            if (remaining > 0 && remaining < jwtTokenProvider.getAccessExpiration() * 0.3) {
                String newToken = jwtTokenProvider.createAccessToken(
                        jwtTokenProvider.getUserId(token),
                        jwtTokenProvider.getRole(token),
                        jwtTokenProvider.getPlatform(token),
                        jwtTokenProvider.getTokenVersion(token));
                response.setHeader("x-new-access-token", newToken);
            }
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(message));
    }
}
