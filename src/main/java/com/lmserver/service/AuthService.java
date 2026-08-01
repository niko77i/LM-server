/**
 * 认证服务接口 — 登录/注册/Token刷新/获取当前用户
 */

/**
 * 认证服务接口 — 登录/注册/Token刷新/获取当前用户
 */

package com.lmserver.service;

import com.lmserver.dto.response.LoginResponse;

/**
 * 认证服务接口。
 */
public interface AuthService {

    /**
     * 用户登录。
     * @return 登录结果（含 Token 和用户信息），失败返回 null
     */
    LoginResponse login(String username, String password);

    /**
     * 用户注册。
     * @return 新用户信息
     */
    LoginResponse.UserInfo register(String username, String password, String displayName);

    /**
     * 刷新 Token。
     * @param refreshToken 有效的 Refresh Token
     * @return 新的 Access Token，失败返回 null
     */
    String refreshToken(String refreshToken);

    /**
     * 获取当前用户信息。
     */
    LoginResponse.UserInfo getCurrentUser(Long userId);
}
