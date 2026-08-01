package com.lmserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
/**
 * 注册请求 — username(4-20字符) + password(最少6字符) + displayName
 */

/**
 * 注册请求 — username(4-20字符) + password(最少6字符) + displayName
 */

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度 4-20 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少 6 个字符")
    private String password;

    private String displayName;
}
