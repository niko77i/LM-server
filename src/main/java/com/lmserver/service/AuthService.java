package com.lmserver.service;

import com.lmserver.dto.response.LoginResponse;
import com.lmserver.security.UserPrincipal;

import java.util.List;
import java.util.Map;

/**
 * 认证服务接口 — 登录/注册/Token/个人信息/密码。
 */
public interface AuthService {

    LoginResponse login(String username, String password);

    LoginResponse.UserInfo register(String username, String password, String displayName);

    String refreshToken(String refreshToken);

    LoginResponse.UserInfo getCurrentUser(Long userId);

    boolean changePassword(Long userId, String oldPwd, String newPwd);

    void updateProfile(Long userId, String displayName);

    void updateCustomName(Long userId, String customName);

    void updateEmail(Long userId, String email);

    void updateTelegram(Long userId, String telegramUsername);

    List<Map<String, Object>> getUserNames(UserPrincipal principal);
}
