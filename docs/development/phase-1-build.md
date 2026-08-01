# Phase 1: 项目骨架搭建完成文档

> **日期**: 2026-08-01  
> **状态**: ✅ 完成  
> **项目**: LM-Server (`D:\server\cc\LM-Server`)

---

## 概述

将 Python Flask GG-Server 后端迁移至 Java Spring Boot LM-Server 的第一阶段。
本阶段搭建了完整的项目骨架，包括 Maven 构建系统、Spring Security + JWT 认证、
统一异常处理、CORS 配置等基础设施。

## 创建文件统计

| 分类 | 文件数 | 说明 |
|------|--------|------|
| 构建配置 | 2 | pom.xml, .gitignore |
| 配置文件 | 1 | application.yml |
| 配置类 | 5 | SecurityConfig, JwtConfig, WebConfig, AsyncConfig, CacheConfig |
| 安全组件 | 4 | JwtTokenProvider, JwtAuthenticationFilter, PlatformGuardFilter, UserPrincipal |
| Controller | 4 | HealthController, AuthController, ProductController, FbBmController |
| Service | 2 | AuthService, AuthServiceImpl |
| DTO | 5 | ApiResponse, PagedResponse, LoginResponse, LoginRequest, RegisterRequest |
| 枚举 | 1 | UserRole |
| 异常 | 4 | BusinessException, UnauthorizedException, PlatformForbiddenException, GlobalExceptionHandler |
| 工具类 | 3 | JwtUtil, PasswordUtil, DateUtil |
| 测试 | 1 | LmServerApplicationTests |
| **合计** | **32** | |

## 编译结果

```
mvn compile → BUILD SUCCESS (29 source files compiled)
```

## 关键设计决策

1. **包名 com.lmserver** — 与项目名 LM-Server 一致
2. **Phase 1 无数据库** — 排除 DataSource/JPA 自动配置
3. **分页字段名 items** — 匹配前端 `response.items`
4. **端口 5001** — 与 Flask 一致，前端代理无需改动
5. **硬编码开发者账户** — carl567 / admin123，用于测试认证链路
6. **启动时 JWT Secret 校验** — 拒绝弱默认密钥

## 安全架构

```
[请求] → JwtAuthenticationFilter → PlatformGuardFilter → SecurityConfig → [Controller]
          提取 Bearer Token          阻止FB用户访问GG路由    URL权限规则
          设置 SecurityContext                             角色验证
```

## 测试账户

| 用户名 | 密码 | 角色 | 平台 |
|--------|------|------|------|
| carl567 | admin123 | developer | gg |

## 待实现

- [ ] 数据库连接 (Phase 2)
- [ ] 40 个 JPA Entity + Repository (Phase 2)
- [ ] 236 个 API 接口 (Phase 3-5)
- [ ] 外部集成 (Google Sheets/Ads, FFmpeg, AI, Email, Telegram)
- [ ] 单元测试和集成测试
