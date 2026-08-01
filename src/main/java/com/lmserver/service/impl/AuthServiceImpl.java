package com.lmserver.service.impl;

import com.lmserver.dto.response.LoginResponse;
import com.lmserver.dto.response.LoginResponse.UserInfo;
import com.lmserver.entity.common.Users;
import com.lmserver.enums.UserRole;
import com.lmserver.repository.common.UsersRepository;
import com.lmserver.security.JwtTokenProvider;
import com.lmserver.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(String username, String password) {
        Users user = usersRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        UserRole role = UserRole.fromValue(user.getRole());
        if (!role.canLogin()) {
            return null;
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getRole(), user.getPlatform(), 0);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), 0);

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        usersRepository.save(user);

        log.info("用户登录成功: {} (角色: {})", user.getUsername(), user.getRole());
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserInfo(user))
                .build();
    }

    @Override
    public UserInfo register(String username, String password, String displayName) {
        if (usersRepository.findByUsername(username).isPresent()) {
            return null;
        }
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("user");
        user.setPlatform("gg");
        user.setDisplayName(displayName != null ? displayName : username);
        user.setCreatedAt(LocalDateTime.now());
        usersRepository.save(user);

        log.info("用户注册成功: {} (id={})", username, user.getId());
        return toUserInfo(user);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            return null;
        }
        Users user = usersRepository.findById(jwtTokenProvider.getUserId(refreshToken)).orElse(null);
        if (user == null) return null;
        return jwtTokenProvider.createAccessToken(user.getId(), user.getRole(), user.getPlatform(), 0);
    }

    @Override
    public UserInfo getCurrentUser(Long userId) {
        return usersRepository.findById(userId).map(this::toUserInfo).orElse(null);
    }

    private UserInfo toUserInfo(Users u) {
        return UserInfo.builder()
                .id(u.getId()).username(u.getUsername()).role(u.getRole())
                .platform(u.getPlatform()).displayName(u.getDisplayName())
                .build();
    }
}
