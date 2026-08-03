package com.lmserver.config;

import com.lmserver.security.JwtAuthenticationFilter;
import com.lmserver.security.JwtTokenProvider;
import com.lmserver.security.PlatformGuardFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/**
 * Spring Security 安全配置 — JWT 无状态认证 + 路由权限规则 + BCrypt 密码编码器
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    /** 安全过滤器链配置 — 定义公开路由、权限规则、过滤器注册 */
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(CsrfConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login", "/api/auth/register",
                    "/api/auth/refresh", "/api/health",
                    "/swagger-ui/**", "/v3/api-docs/**",
                    "/actuator/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/fonts/**",
                    "/api/video/download", "/api/video/progress", "/api/image"
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("DEVELOPER", "ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                    UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(new PlatformGuardFilter(),
                    JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    /** BCrypt 密码编码器 Bean */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
