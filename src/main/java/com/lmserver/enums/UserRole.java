/**
 * 用户角色枚举 — developer(最高权限，跨平台) > admin > viewer(只读) > user > hidden(禁止登录)
 */

/**
 * 用户角色枚举 — developer(最高权限，跨平台) > admin > viewer(只读) > user > hidden(禁止登录)
 */

package com.lmserver.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
public enum UserRole {
    DEVELOPER("developer"),
    ADMIN("admin"),
    VIEWER("viewer"),
    USER("user"),
    HIDDEN("hidden");

    private final String value;
    UserRole(String value) { this.value = value; }
    public String getValue() { return value; }
    /** 获取 Spring Security 权限列表 — 格式 ROLE_DEVELOPER/ROLE_ADMIN/... */
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }
    /** 判断是否为特权角色 — developer 或 admin */
    public boolean isPrivileged() { return this == DEVELOPER || this == ADMIN; }
    /** 判断是否允许登录 — hidden 角色禁止 */
    public boolean canLogin() { return this != HIDDEN; }

    public static UserRole fromValue(String value) {
        if (value == null || value.isEmpty()) return USER;
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) return role;
        }
        return USER;
    }
}
