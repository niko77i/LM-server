package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.LoginResponse;
import com.lmserver.dto.response.LoginResponse.UserInfo;
import com.lmserver.entity.common.Users;
import com.lmserver.enums.UserRole;
import com.lmserver.mapper.common.UsersMapper;
import com.lmserver.security.JwtTokenProvider;
import com.lmserver.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersMapper usersMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    /** 用户登录 — 验证用户名密码，成功返回 JWT Token 和用户信息 */
    public LoginResponse login(String username, String password) {
        Users user = usersMapper.selectOne(
                new LambdaQueryWrapper<Users>().eq(Users::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) return null;
        if (!UserRole.fromValue(user.getRole()).canLogin()) return null;

        int tv = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole(), user.getPlatform(), tv);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), tv);

        user.setLastLogin(LocalDateTime.now());
        usersMapper.updateById(user);

        log.info("用户登录成功: {} (角色: {})", user.getUsername(), user.getRole());
        return LoginResponse.builder()
                .accessToken(accessToken).refreshToken(refreshToken).user(toUserInfo(user)).build();
    }

    @Override
    /** 用户注册 — 创建新账户，默认角色 user，平台 gg */
    public UserInfo register(String username, String password, String displayName) {
        if (usersMapper.selectOne(new LambdaQueryWrapper<Users>().eq(Users::getUsername, username)) != null)
            return null;
        Users u = new Users();
        u.setUsername(username); u.setPassword(passwordEncoder.encode(password));
        u.setRole("user"); u.setPlatform("gg");
        u.setDisplayName(displayName != null ? displayName : username);
        u.setCreatedAt(LocalDateTime.now());
        usersMapper.insert(u);
        log.info("用户注册成功: {} (id={})", username, u.getId());
        return toUserInfo(u);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken))
            return null;
        Users u = usersMapper.selectById(jwtTokenProvider.getUserId(refreshToken));
        if (u == null) return null;

        // Refresh Token 轮换：递增 tokenVersion 使旧 Token 失效
        int newVersion = u.getTokenVersion() != null ? u.getTokenVersion() + 1 : 1;
        u.setTokenVersion(newVersion);
        usersMapper.updateById(u);

        return jwtTokenProvider.createAccessToken(u.getId(), u.getRole(), u.getPlatform(), newVersion);
    }

    @Override
    /** 获取当前登录用户 — 从数据库查询完整用户信息 */
    public UserInfo getCurrentUser(Long userId) {
        Users u = usersMapper.selectById(userId);
        return u != null ? toUserInfo(u) : null;
    }

    @Override
    public boolean changePassword(Long userId, String oldPwd, String newPwd) {
        Users u = usersMapper.selectById(userId);
        if (u == null || !passwordEncoder.matches(oldPwd, u.getPassword())) return false;
        u.setPassword(passwordEncoder.encode(newPwd));
        u.setTokenVersion((u.getTokenVersion() != null ? u.getTokenVersion() : 0) + 1);
        usersMapper.updateById(u);
        log.info("用户 {} 密码已修改, tokenVersion++", u.getUsername());
        return true;
    }

    @Override
    public void updateProfile(Long userId, String displayName) {
        Users u = usersMapper.selectById(userId);
        if (u != null) { u.setDisplayName(displayName); usersMapper.updateById(u); }
    }

    @Override
    public void updateCustomName(Long userId, String customName) {
        Users u = usersMapper.selectById(userId);
        if (u != null) { u.setCustomName(customName); usersMapper.updateById(u); }
    }

    @Override
    public void updateEmail(Long userId, String email) {
        Users u = usersMapper.selectById(userId);
        if (u != null) { u.setEmail(email); usersMapper.updateById(u); }
    }

    @Override
    public void updateTelegram(Long userId, String telegramUsername) {
        Users u = usersMapper.selectById(userId);
        if (u != null) { u.setTelegramUsername(telegramUsername); usersMapper.updateById(u); }
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getUserNames(com.lmserver.security.UserPrincipal principal) {
        var users = usersMapper.selectList(null);
        return users.stream().map(u -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("display_name", u.getDisplayName());
            m.put("platform", u.getPlatform());
            return m;
        }).toList();
    }

    private UserInfo toUserInfo(Users u) {
        return UserInfo.builder().id(u.getId()).username(u.getUsername())
                .role(u.getRole()).platform(u.getPlatform()).displayName(u.getDisplayName()).build();
    }
}
