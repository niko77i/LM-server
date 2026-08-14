package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户简要信息 DTO — 用于 auth/names、fb/users 等端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBriefDto {

    private Long id;

    private String username;

    /** 显示名称 (display_name → displayName，Jackson SNAKE_CASE 输出为 display_name) */
    private String displayName;

    private String platform;

    private String role;
}
