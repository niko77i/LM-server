package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class Users {

        
    /** 主键ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码(BCrypt哈希) */
    private String password;

    /** 角色: developer/admin/viewer/user/hidden */
    private String role;

    @TableField("display_name")
    /** 显示名称 */
    private String displayName;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("last_login")
    /** 最后登录时间 */
    private LocalDateTime lastLogin;

    @TableField("created_by")
    /** 创建者用户ID */
    private Long createdBy;

    /** 配置JSON */
    private String config;

    @TableField("custom_name")
    /** 自定义名称 */
    private String customName;

    /** 邮箱 */
    private String email;

    @TableField("telegram_username")
    /** Telegram用户名 */
    private String telegramUsername;

    /** 所属平台: gg/fb */
    private String platform;

    /** JWT Token 版本号 — 改密/禁用时递增，强制旧 Token 失效 */
    @TableField("token_version")
    /** JWT Token版本号(改密/禁用时递增) */
    private Integer tokenVersion;

}