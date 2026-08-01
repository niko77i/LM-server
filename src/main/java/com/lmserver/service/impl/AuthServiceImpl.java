package com.lmserver.service.impl;

import com.lmserver.dto.response.LoginResponse;
import com.lmserver.enums.UserRole;
import com.lmserver.security.JwtTokenProvider;
import com.lmserver.service.AuthService;
import com.lmserver.util.PasswordUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 认证服务实现。
 * Phase 1: 使用内存存储，硬编码 developer 账户。
 * 后续阶段替换为 JPA + MySQL。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final JwtTokenProvider jwtTokenProvider;

    /** Phase 1 内存用户存储（后续替换为 UserRepository） */
    private final Map<Long, MemoryUser> users = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public AuthServiceImpl(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostConstruct
    public void init() {
        // Phase 1: 硬编码 developer 账户用于测试
        // 密码: admin123（BCrypt 编码）
        String encodedPwd = PasswordUtil.encode("admin123");
        MemoryUser dev = new MemoryUser(
                idGen.getAndIncrement(), "carl567", encodedPwd,
                "developer", "gg", "系统管理员");
        users.put(dev.id, dev);
        log.info("Phase 1 初始化: 已创建 developer 账户 (carl567 / admin123)");
    }

    @Override
    public LoginResponse login(String username, String password) {
        MemoryUser user = users.values().stream()
                .filter(u -> u.username.equals(username))
                .findFirst()
                .orElse(null);

        if (user == null || !PasswordUtil.matches(password, user.password)) {
            return null;
        }

        UserRole role = UserRole.fromValue(user.role);
        if (!role.canLogin()) {
            return null;
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.id, user.role, user.platform, 0);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.id, 0);

        log.info("用户登录成功: {} (角色: {})", user.username, user.role);
        return LoginResponse.of(
                accessToken, refreshToken,
                LoginResponse.UserInfo.of(
                        user.id, user.username, user.role,
                        user.platform, user.displayName));
    }

    @Override
    public LoginResponse.UserInfo register(String username, String password, String displayName) {
        // 检查用户名唯一性
        boolean exists = users.values().stream()
                .anyMatch(u -> u.username.equals(username));
        if (exists) {
            return null;
        }

        MemoryUser user = new MemoryUser(
                idGen.getAndIncrement(), username,
                PasswordUtil.encode(password),
                "user", "gg",
                displayName != null ? displayName : username);
        users.put(user.id, user);

        log.info("用户注册成功: {} (id={})", username, user.id);
        return LoginResponse.UserInfo.of(
                user.id, user.username, user.role,
                user.platform, user.displayName);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return null;
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            return null;
        }
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        MemoryUser user = users.get(userId);
        if (user == null) {
            return null;
        }
        return jwtTokenProvider.createAccessToken(
                user.id, user.role, user.platform, 0);
    }

    @Override
    public LoginResponse.UserInfo getCurrentUser(Long userId) {
        MemoryUser user = users.get(userId);
        if (user == null) {
            return null;
        }
        return LoginResponse.UserInfo.of(
                user.id, user.username, user.role,
                user.platform, user.displayName);
    }

    // ──────── Phase 1 内存用户模型 ────────

    private static class MemoryUser {
        final Long id;
        final String username;
        final String password;
        final String role;
        final String platform;
        final String displayName;

        MemoryUser(Long id, String username, String password,
                   String role, String platform, String displayName) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.role = role;
            this.platform = platform;
            this.displayName = displayName;
        }
    }
}
