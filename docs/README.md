# LM-Server Spring Boot 项目文档

> **项目**: LM-Server (卡天皇运营工具箱后端)  
> **技术栈**: Spring Boot 3.3.0 + JDK 17 + Maven  
> **基础包**: `com.lmserver`  
> **端口**: 5001  
> **迁移来源**: Python Flask GG-Server → Java Spring Boot LM-Server

---

## 项目结构

```
LM-Server/
├── pom.xml                             # Maven 项目定义
├── .gitignore
├── docs/                               # 📚 项目文档
│   ├── README.md                       # 文档索引
│   ├── architecture/                   # 架构文档
│   ├── api/                            # API 接口文档
│   ├── development/                    # 开发指南
│   └── superpowers/                    # 设计规格与实现计划
├── src/
│   ├── main/java/com/lmserver/
│   │   ├── LmServerApplication.java    # 启动类
│   │   ├── config/                     # Spring 配置
│   │   │   ├── SecurityConfig.java     # Spring Security 配置
│   │   │   ├── JwtConfig.java          # JWT 属性配置
│   │   │   ├── WebConfig.java          # CORS 跨域配置
│   │   │   ├── AsyncConfig.java        # 异步线程池
│   │   │   └── CacheConfig.java        # Caffeine 缓存
│   │   ├── security/                   # 安全组件
│   │   │   ├── JwtTokenProvider.java   # Token 生成/验证
│   │   │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   │   │   ├── PlatformGuardFilter.java      # GG/FB 平台守卫
│   │   │   └── UserPrincipal.java            # 用户主体
│   │   ├── controller/                 # REST Controller
│   │   │   ├── HealthController.java
│   │   │   ├── auth/AuthController.java
│   │   │   ├── gg/ProductController.java     (骨架)
│   │   │   └── fb/FbBmController.java        (骨架)
│   │   ├── service/                    # 业务层
│   │   │   ├── AuthService.java        # 认证接口
│   │   │   └── impl/AuthServiceImpl.java  # 认证实现
│   │   ├── dto/
│   │   │   ├── request/                # 请求 DTO
│   │   │   │   ├── LoginRequest.java
│   │   │   │   └── RegisterRequest.java
│   │   │   └── response/               # 响应 DTO
│   │   │       ├── ApiResponse.java    # 统一响应
│   │   │       ├── PagedResponse.java  # 分页响应
│   │   │       └── LoginResponse.java  # 登录响应
│   │   ├── enums/UserRole.java         # 用户角色枚举
│   │   ├── exception/                  # 异常处理
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BusinessException.java
│   │   │   ├── UnauthorizedException.java
│   │   │   └── PlatformForbiddenException.java
│   │   └── util/
│   │       ├── JwtUtil.java            # JWT 核心工具
│   │       ├── PasswordUtil.java       # BCrypt 密码
│   │       └── DateUtil.java           # 日期工具
│   ├── main/resources/
│   │   └── application.yml             # 配置
│   └── test/java/com/lmserver/
│       └── LmServerApplicationTests.java
```

## 快速开始

```bash
# 编译
cd LM-Server
mvn compile

# 运行
mvn spring-boot:run

# 测试登录
curl -X POST http://localhost:5001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"carl567","password":"admin123"}'

# 响应示例
# {"success":true,"data":{"accessToken":"eyJ...","refreshToken":"eyJ...","user":{...}}}
```

## API 端点

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 无 | 用户登录 |
| POST | `/api/auth/register` | 无 | 用户注册 |
| POST | `/api/auth/refresh` | 无 | 刷新 Token |
| GET | `/api/auth/me` | JWT | 获取当前用户 |
| GET | `/api/health` | 无 | 健康检查 |

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SERVER_PORT` | 服务端口 | 5001 |
| `JWT_SECRET` | JWT 签名密钥 (Base64) | 开发默认值 |

## 迁移阶段

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | 项目骨架搭建 | ✅ 完成 |
| Phase 2 | 数据库 + JPA Entity | 待开始 |
| Phase 3 | GG 核心业务 | 待开始 |
| Phase 4 | FB 核心业务 | 待开始 |
| Phase 5 | 辅助功能 (视频/AI/通知) | 待开始 |
| Phase 6 | 测试与上线 | 待开始 |
