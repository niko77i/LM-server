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

/** Service interface */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersMapper usersMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(String username, String password) {
        Users user = usersMapper.selectOne(
                new LambdaQueryWrapper<Users>().eq(Users::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) return null;
        if (!UserRole.fromValue(user.getRole()).canLogin()) return null;

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole(), user.getPlatform(), 0);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), 0);

        user.setLastLogin(LocalDateTime.now());
        usersMapper.updateById(user);

        log.info("用户登录成功: {} (角色: {})", user.getUsername(), user.getRole());
        return LoginResponse.builder()
                .accessToken(accessToken).refreshToken(refreshToken).user(toUserInfo(user)).build();
    }

    @Override
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
        return jwtTokenProvider.createAccessToken(u.getId(), u.getRole(), u.getPlatform(), 0);
    }

    @Override
    public UserInfo getCurrentUser(Long userId) {
        Users u = usersMapper.selectById(userId);
        return u != null ? toUserInfo(u) : null;
    }

    private UserInfo toUserInfo(Users u) {
        return UserInfo.builder().id(u.getId()).username(u.getUsername())
                .role(u.getRole()).platform(u.getPlatform()).displayName(u.getDisplayName()).build();
    }
}
