package com.lmserver.controller.auth;

import com.lmserver.dto.request.LoginRequest;
import com.lmserver.dto.request.RegisterRequest;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.LoginResponse;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录。POST /api/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse result = authService.login(req.getUsername(), req.getPassword());
        if (result == null) {
            return ApiResponse.fail("用户名或密码错误，或账户已被禁用");
        }
        return ApiResponse.ok(result);
    }

    /**
     * 用户注册。POST /api/auth/register
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse.UserInfo> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse.UserInfo user = authService.register(
                req.getUsername(), req.getPassword(), req.getDisplayName());
        if (user == null) {
            return ApiResponse.fail("注册失败，用户名可能已存在");
        }
        return ApiResponse.ok(user);
    }

    /**
     * 刷新 Token。POST /api/auth/refresh
     */
    @PostMapping("/refresh")
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

    /**
     * 获取当前用户信息。GET /api/auth/me
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ApiResponse.fail("未认证");
        }
        LoginResponse.UserInfo user = authService.getCurrentUser(principal.getUserId());
        if (user == null) {
            return ApiResponse.fail("用户不存在");
        }
        return ApiResponse.ok(user);
    }

}
