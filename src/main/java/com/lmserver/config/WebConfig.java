package com.lmserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置 — CORS 跨域规则，允许前端跨域访问，暴露 x-new-access-token 头
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    /** CORS 跨域映射 — 允许所有来源访问 /api
    /** CORS 跨域映射 — 允许所有来源访问 /api/** */
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "X-Requested-With")
                .exposedHeaders("x-new-access-token")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
