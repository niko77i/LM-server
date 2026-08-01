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

    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }

    public boolean isPrivileged() { return this == DEVELOPER || this == ADMIN; }
    public boolean canLogin() { return this != HIDDEN; }

    public static UserRole fromValue(String value) {
        if (value == null || value.isEmpty()) return USER;
        for (UserRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) return role;
        }
        return USER;
    }
}
