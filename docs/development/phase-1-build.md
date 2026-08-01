# Phase 1: 项目骨架搭建 + JWT 安全

> **日期**: 2026-08-01  
> **状态**: ✅ 完成

---

## 创建文件统计

| 分类 | 文件数 | 说明 |
|------|--------|------|
| 构建配置 | 2 | pom.xml, .gitignore |
| 配置文件 | 1 | application.yml |
| 配置类 | 6 | SecurityConfig, JwtConfig, WebConfig, AsyncConfig, CacheConfig |
| 安全组件 | 4 | JwtTokenProvider, JwtAuthFilter, PlatformGuardFilter, UserPrincipal |
| Controller | 4 | HealthController, AuthController, ProductController(stub), FbBmController(stub) |
| Service | 2 | AuthService, AuthServiceImpl (内存存储) |
| DTO | 5 | ApiResponse, PagedResponse, LoginResponse, LoginRequest, RegisterRequest |
| 枚举 | 1 | UserRole |
| 异常 | 4 | BusinessException, UnauthorizedException, PlatformForbiddenException, GlobalExceptionHandler |
| 工具类 | 3 | JwtUtil, PasswordUtil, DateUtil |
| 测试 | 1 | LmServerApplicationTests |
| **合计** | **33** | |

## 技术选型

| 项 | 选择 |
|------|------|
| 构建 | Maven |
| Spring Boot | 3.3.0 |
| Java | 17 (LTS) |
| 安全 | Spring Security + jjwt 0.12.5 |
| 密码 | BCrypt |
| 缓存 | Caffeine |
| 异步 | @Async + ThreadPoolTaskExecutor |
| BO简化 | Lombok |

## 安全架构

```
[请求] → JwtAuthenticationFilter → PlatformGuardFilter → [Controller]
         提取 Bearer Token           GG/FB 平台隔离        业务处理
         设置 SecurityContext
```

- JWT 滑动过期：剩余 < 30% 时签发新 Token (`x-new-access-token` 头)
- JWT 启动校验：拒绝弱默认密钥
- 分页字段名 `items`：匹配前端 `response.items`
