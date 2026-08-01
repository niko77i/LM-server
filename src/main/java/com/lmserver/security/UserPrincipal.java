package com.lmserver.security;

import com.lmserver.enums.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
/**
 * Spring Security 用户主体 — 从 JWT Claims 中提取 userId/role/platform/tokenVersion，实现 UserDetails 接口
 */

/**
 * Spring Security 用户主体 — 从 JWT Claims 中提取 userId/role/platform/tokenVersion，实现 UserDetails 接口
 */

@Getter
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String role;
    private final String platform;
    private final int tokenVersion;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String username, String role, String platform, int tokenVersion) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.platform = platform;
        this.tokenVersion = tokenVersion;
        this.authorities = UserRole.fromValue(role).getAuthorities();
    }

    @Override public String getPassword() { return null; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return UserRole.fromValue(role).canLogin(); }
    /** 判断是否为 developer 角色 */
    public boolean isDeveloper() { return UserRole.DEVELOPER.name().equalsIgnoreCase(role); }
    /** 判断是否为管理员角色 — developer 也是管理员 */
    public boolean isAdmin() { return isDeveloper() || UserRole.ADMIN.name().equalsIgnoreCase(role); }
    /** 判断是否为 FB 平台用户 */
    public boolean isFbUser() { return "fb".equalsIgnoreCase(platform); }
}
