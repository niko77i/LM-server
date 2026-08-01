package com.lmserver.entity.common;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String role;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_by")
    private Long createdBy;

    private String config;

    @Column(name = "custom_name")
    private String customName;

    private String email;

    @Column(name = "telegram_username")
    private String telegramUsername;

    private String platform;

}