package com.lmserver.security;

import com.lmserver.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

/**
 * Spring Security 用户主体，从 JWT Token 构建。
 */
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

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return UserRole.fromValue(role).canLogin(); }

    public Long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getPlatform() { return platform; }
    public int getTokenVersion() { return tokenVersion; }
    public boolean isDeveloper() { return UserRole.DEVELOPER.name().equalsIgnoreCase(role); }
    public boolean isAdmin() { return isDeveloper() || UserRole.ADMIN.name().equalsIgnoreCase(role); }
    public boolean isFbUser() { return "fb".equalsIgnoreCase(platform); }
}
