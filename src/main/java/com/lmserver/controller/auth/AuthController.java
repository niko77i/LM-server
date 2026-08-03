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

import java.util.List;
import java.util.Map;

/**
 * 认证控制器 — /api/auth/*。12 个接口完整实现。
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse result = authService.login(req.getUsername(), req.getPassword());
        if (result == null) return ApiResponse.fail("用户名或密码错误，或账户已被禁用");
        return ApiResponse.ok(result);
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse.UserInfo> register(@Valid @RequestBody RegisterRequest req) {
        LoginResponse.UserInfo user = authService.register(req.getUsername(), req.getPassword(), req.getDisplayName());
        if (user == null) return ApiResponse.fail("注册失败，用户名可能已存在");
        return ApiResponse.ok(user);
    }

    @PostMapping("/refresh")
    public ApiResponse<String> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ApiResponse.fail("缺少 Refresh Token");
        String newToken = authService.refreshToken(authHeader.substring(7));
        if (newToken == null) return ApiResponse.fail("Refresh Token 无效或已过期");
        return ApiResponse.ok(newToken);
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ApiResponse.fail("未认证");
        LoginResponse.UserInfo user = authService.getCurrentUser(principal.getUserId());
        if (user == null) return ApiResponse.fail("用户不存在");
        return ApiResponse.ok(user);
    }

    /** 修改密码 */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        boolean ok = authService.changePassword(principal.getUserId(),
                body.get("old_password"), body.get("new_password"));
        return ok ? ApiResponse.ok() : ApiResponse.fail("旧密码错误");
    }

    /** 修改显示名称 */
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        authService.updateProfile(principal.getUserId(), body.get("display_name"));
        return ApiResponse.ok();
    }

    /** 修改自定义名称 */
    @PutMapping("/custom-name")
    public ApiResponse<Void> updateCustomName(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        authService.updateCustomName(principal.getUserId(), body.get("custom_name"));
        return ApiResponse.ok();
    }

    /** 修改邮箱 */
    @PutMapping("/email")
    public ApiResponse<Void> updateEmail(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        authService.updateEmail(principal.getUserId(), body.get("email"));
        return ApiResponse.ok();
    }

    /** 修改 Telegram 用户名 */
    @PutMapping("/telegram-username")
    public ApiResponse<Void> updateTelegram(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        authService.updateTelegram(principal.getUserId(), body.get("telegram_username"));
        return ApiResponse.ok();
    }

    /** 获取所有用户名列表（下拉选择用） */
    @GetMapping("/names")
    public ApiResponse<List<Map<String, Object>>> userNames(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(authService.getUserNames(principal));
    }

    @GetMapping("/names/{id}")
    public ApiResponse<Map<String, Object>> userNameById(@PathVariable Long id) {
        var u = authService.getCurrentUser(id);
        if (u == null) return ApiResponse.fail("不存在");
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", u.getId()); m.put("username", u.getUsername());
        m.put("display_name", u.getDisplayName()); m.put("platform", u.getPlatform());
        return ApiResponse.ok(m);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() { return ApiResponse.ok(); } // JWT无状态,前端清除token即可
}
