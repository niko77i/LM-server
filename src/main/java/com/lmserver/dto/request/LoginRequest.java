package com.lmserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * 登录请求 — @NotBlank 校验的 username + password
 */

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
