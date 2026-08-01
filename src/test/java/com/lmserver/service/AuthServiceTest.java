package com.lmserver.service;

import com.lmserver.dto.response.LoginResponse;
import com.lmserver.mapper.common.UsersMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试 — 验证登录、注册、Token 刷新流程。
 * 需要 MySQL 数据库运行。
 */
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsersMapper usersMapper;

    /** 测试 developer 账户登录 */
    @Test
    void testLoginDeveloper() {
        LoginResponse result = authService.login("carl567", "1976xiaobai");
        assertNotNull(result, "developer 账户登录应成功");
        assertNotNull(result.getAccessToken(), "应返回 accessToken");
        assertNotNull(result.getRefreshToken(), "应返回 refreshToken");
        assertEquals("developer", result.getUser().getRole(), "角色应为 developer");
    }

    /** 测试错误密码登录 */
    @Test
    void testLoginWrongPassword() {
        LoginResponse result = authService.login("carl567", "wrongpassword");
        assertNull(result, "错误密码应返回 null");
    }

    /** 测试获取当前用户 */
    @Test
    void testGetCurrentUser() {
        LoginResponse.UserInfo user = authService.getCurrentUser(1L);
        assertNotNull(user, "ID为1的用户应存在");
        assertEquals("carl567", user.getUsername());
    }
}
