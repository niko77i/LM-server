package com.lmserver.controller.auth;

import com.lmserver.dto.request.LoginRequest;
import com.lmserver.dto.request.RegisterRequest;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.LoginResponse;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
/**
 * 认证控制器 — /api/auth/*，处理登录/注册/Token刷新/个人信息，login和register公开访问
 */

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    /** 用户登录 — 验证用户名密码，成功返回 JWT Token 和用户信息 */
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse result = authService.login(req.getUsername(), req.getPassword());
        if (result == null) {
            return ApiResponse.fail("用户名或密码错误，或账户已被禁用");
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/register")
    /** 用户注册 — 创建新账户，默认角色 user，平台 gg */
    public ApiResponse<LoginResponse.UserInfo> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse.UserInfo user = authService.register(
                req.getUsername(), req.getPassword(), req.getDisplayName());
        if (user == null) {
            return ApiResponse.fail("注册失败，用户名可能已存在");
        }
        return ApiResponse.ok(user);
    }

    @PostMapping("/refresh")
    /** 刷新 Token — 用 Refresh Token 换取新的 Access Token */
    public ApiResponse<String> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.fail("缺少 Refresh Token");
        }
        String newToken = authService.refreshToken(authHeader.substring(7));
        if (newToken == null) {
            return ApiResponse.fail("Refresh Token 无效或已过期");
        }
        return ApiResponse.ok(newToken);
    }

    @GetMapping("/me")
    /** 获取当前用户信息 — 从 JWT 解析用户 ID 后查询数据库 */
    public ApiResponse<LoginResponse.UserInfo> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ApiResponse.fail("未认证");
        LoginResponse.UserInfo user = authService.getCurrentUser(principal.getUserId());
        if (user == null) return ApiResponse.fail("用户不存在");
        return ApiResponse.ok(user);
    }
}
