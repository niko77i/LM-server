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

        
    private Long id;

    private String username;

    private String password;

    private String role;

    @TableField("display_name")
    private String displayName;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("last_login")
    private LocalDateTime lastLogin;

    @TableField("created_by")
    private Long createdBy;

    private String config;

    @TableField("custom_name")
    private String customName;

    private String email;

    @TableField("telegram_username")
    private String telegramUsername;

    private String platform;

}