# LM-Server Spring Boot 项目文档

> **项目**: LM-Server (卡天皇运营工具箱后端)  
> **技术栈**: Spring Boot 3.3.0 + JDK 17 + Maven + MySQL 8.0  
> **基础包**: `com.lmserver`  
> **端口**: 8080  
> **数据库**: MySQL 8.0.40 @ localhost:3306 / lmserver

---

## 快速开始

```bash
cd LM-Server

# 1. 启动后端
mvn spring-boot:run

# 2. 启动前端（新终端）
cd frontend && npm run dev
```

| 服务 | 地址 |
|------|------|
| Spring Boot API | http://localhost:8080 |
| Vite 前端 | http://localhost:5173 |
| 原 Python 后端 | http://localhost:5001 (不变) |

## 测试账户

```
POST /api/auth/login
{"username":"carl567", "password":"admin123"}
```

## 项目结构

```
LM-Server/
├── pom.xml                             # Maven (groupId: com.lmserver)
├── frontend/                           # Vue 3 + Vite (端口指向 8080)
├── docs/                               # 项目文档
├── src/main/java/com/lmserver/
│   ├── LmServerApplication.java        # 启动类
│   ├── config/                         # Security / JWT / CORS / Async / Cache
│   ├── security/                       # JWT 过滤器链 + 平台守卫
│   ├── controller/                     # Auth(完整) / Health / 骨架
│   │   ├── HealthController.java
│   │   ├── auth/AuthController.java
│   │   ├── gg/ProductController.java
│   │   └── fb/FbBmController.java
│   ├── service/                        # AuthService 接口 + 内存实现
│   ├── dto/response/                   # ApiResponse / PagedResponse / LoginResponse
│   ├── dto/request/                    # LoginRequest / RegisterRequest
│   ├── enums/UserRole.java
│   ├── exception/                      # 全局异常处理体系
│   └── util/                           # JwtUtil / PasswordUtil / DateUtil
├── src/main/resources/
│   ├── application.yml                 # MySQL 连接配置
│   ├── schema.sql                      # 40 张表 DDL (参考)
│   └── data.sql                        # 测试数据 (参考)
└── src/test/
    └── LmServerApplicationTests.java
```

## API 端点

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 无 | 登录 |
| POST | `/api/auth/register` | 无 | 注册 |
| POST | `/api/auth/refresh` | 无 | 刷新 Token |
| GET | `/api/auth/me` | JWT | 当前用户 |
| GET | `/api/health` | 无 | 健康检查 |

## 数据库

- **MySQL 8.0.40** (D:\work\mysql\mysql-8.0.40-winx64)
- 数据库: `lmserver` (utf8mb4)
- 40 张表 + 6213 行测试数据（从 GG-Server SQLite 迁移）

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SERVER_PORT` | 服务端口 | 8080 |
| `JWT_SECRET` | JWT 签名密钥 | 开发默认值 |

## 迁移阶段

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | 项目骨架搭建 + JWT 安全 | ✅ |
| Phase 2 | 前端拷贝 + MySQL 数据迁移 | ✅ |
| Phase 3 | 40个 Entity + Repository | ✅ |
| Phase 4 | GG 核心业务 (Controller/Service) | 待开始 |
| Phase 5 | FB 核心业务 | 待开始 |
| Phase 6 | 辅助功能 (视频/AI/通知) | 待开始 |
| Phase 7 | 测试与上线 | 待开始 |
