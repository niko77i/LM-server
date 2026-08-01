package com.lmserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
/**
 * JWT 配置属性 — 绑定 application.yml 中 jwt.* 到 Java 对象
 */

/**
 * JWT 配置属性 — 绑定 application.yml 中 jwt.* 到 Java 对象
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private long accessTokenExpiration = 3_600_000L;
    private long refreshTokenExpiration = 2_592_000_000L;
}
