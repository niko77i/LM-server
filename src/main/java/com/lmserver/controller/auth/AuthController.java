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

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse result = authService.login(req.getUsername(), req.getPassword());
        if (result == null) {
            return ApiResponse.fail("用户名或密码错误，或账户已被禁用");
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse.UserInfo> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse.UserInfo user = authService.register(
                req.getUsername(), req.getPassword(), req.getDisplayName());
        if (user == null) {
            return ApiResponse.fail("注册失败，用户名可能已存在");
        }
        return ApiResponse.ok(user);
    }

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

    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ApiResponse.fail("未认证");
        LoginResponse.UserInfo user = authService.getCurrentUser(principal.getUserId());
        if (user == null) return ApiResponse.fail("用户不存在");
        return ApiResponse.ok(user);
    }
}
