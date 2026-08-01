package com.lmserver.service.impl;

import com.lmserver.dto.response.LoginResponse;
import com.lmserver.dto.response.LoginResponse.UserInfo;
import com.lmserver.enums.UserRole;
import com.lmserver.security.JwtTokenProvider;
import com.lmserver.service.AuthService;
import com.lmserver.util.PasswordUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final Map<Long, MemoryUser> users = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public AuthServiceImpl(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostConstruct
    public void init() {
        MemoryUser dev = new MemoryUser(
                idGen.getAndIncrement(), "carl567",
                PasswordUtil.encode("admin123"),
                "developer", "gg", "系统管理员");
        users.put(dev.id, dev);
        log.info("Phase 1 初始化: 已创建 developer 账户 (carl567 / admin123)");
    }

    @Override
    public LoginResponse login(String username, String password) {
        MemoryUser user = users.values().stream()
                .filter(u -> u.username.equals(username))
                .findFirst().orElse(null);

        if (user == null || !PasswordUtil.matches(password, user.password)) return null;
        if (!UserRole.fromValue(user.role).canLogin()) return null;

        String accessToken = jwtTokenProvider.createAccessToken(user.id, user.role, user.platform, 0);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.id, 0);

        log.info("用户登录成功: {} (角色: {})", user.username, user.role);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserInfo(user))
                .build();
    }

    @Override
    public UserInfo register(String username, String password, String displayName) {
        boolean exists = users.values().stream().anyMatch(u -> u.username.equals(username));
        if (exists) return null;

        MemoryUser user = new MemoryUser(
                idGen.getAndIncrement(), username,
                PasswordUtil.encode(password),
                "user", "gg",
                displayName != null ? displayName : username);
        users.put(user.id, user);
        log.info("用户注册成功: {} (id={})", username, user.id);
        return toUserInfo(user);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            return null;
        }
        MemoryUser user = users.get(jwtTokenProvider.getUserId(refreshToken));
        if (user == null) return null;
        return jwtTokenProvider.createAccessToken(user.id, user.role, user.platform, 0);
    }

    @Override
    public UserInfo getCurrentUser(Long userId) {
        MemoryUser user = users.get(userId);
        return user != null ? toUserInfo(user) : null;
    }

    private UserInfo toUserInfo(MemoryUser u) {
        return UserInfo.builder()
                .id(u.id).username(u.username).role(u.role)
                .platform(u.platform).displayName(u.displayName)
                .build();
    }

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
