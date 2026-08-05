# LM-Server Spring Boot 项目文档

> **项目**: LM-Server (卡天皇运营工具箱后端)  
> **技术栈**: Spring Boot 3.3.0 + JDK 17 + Maven + MySQL 8.0  
> **基础包**: `com.lmserver`  
> **端口**: 8080  
> **数据库**: MySQL 8.0.40 @ localhost:3306 / lmserver

---

## 快速开始

### 环境要求

- **JDK 17+** / **Maven 3.9+** / **MySQL 8.0.13+** (localhost:3306, root/123456)
- **Node.js 18+** (仅前端开发)
- **FFmpeg** (视频处理，需在 PATH 中)

### 数据库初始化

```bash
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS lmserver CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

> Spring Boot 启动时 MyBatis-Plus 会自动建表，无需手动导入 schema.sql。

### 开发模式（推荐日常使用）

两个终端分别启动，前端热更新即时生效：

```bash
# 终端 1: 启动后端 (http://localhost:8080)
cd LM-Server
mvn spring-boot:run
```

```bash
# 终端 2: 启动前端 (http://localhost:5174)
cd LM-Server/frontend
npm install   # 首次运行
npm run dev
```

| 服务 | 地址 |
|------|------|
| Spring Boot API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Vite 前端 (开发) | http://localhost:5174 |
| 原 Python 后端 | http://localhost:5001 (不变) |

> **注意**: 开发时访问 **:5174**，不要直接访问 :8080 的静态前端。Vite dev server 自动代理 `/api` → `:8080`，且 HMR 热更新只在 dev server 生效。

### 生产打包

前端构建产物直接写入 `src/main/resources/static/`，随 JAR 一起发布：

```bash
cd LM-Server

# 1. 构建前端
cd frontend && npm run build && cd ..

# 2. 打包（跳过测试）
mvn clean package -DskipTests
```

产物: `target/lm-server-0.1.0-SNAPSHOT.jar`（fat JAR，包含全部依赖 + 前端）

### 生产运行

```bash
java -jar target/lm-server-0.1.0-SNAPSHOT.jar
```

直接访问 **http://localhost:8080**，前后端都在里面。

自定义端口 / JWT 密钥：

```bash
java -jar -DSERVER_PORT=9090 -DJWT_SECRET="$(openssl rand -base64 64)" target/lm-server-0.1.0-SNAPSHOT.jar
```

> **打包前确保先执行 `npm run build`**，否则 JAR 中嵌入的是旧版前端。

## 测试账户

```
POST /api/auth/login
{"username":"carl567", "password":"1976xiaobai"}
```

## 项目规模

| 维度 | 数量 |
|------|------|
| Java 源文件 | 178 个 |
| 代码行数 | ~8,200 行 |
| Controller | 39 个 |
| Service 接口 + 实现 | 15 + 10 |
| Entity / Mapper | 40 / 40+ |
| API 接口 | 239 个 |
| 数据库表 | 40 张 |
| 前端页面/组件 | 30+ 个 Vue SFC |

## 项目结构

```
LM-Server/
├── pom.xml                                # Maven (groupId: com.lmserver)
├── frontend/                              # Vue 3 + Vite + Element Plus + Pinia
│   ├── src/
│   │   ├── api/        (12 模块)          # axios + Token 拦截器
│   │   ├── stores/     (5 模块)           # Pinia 状态管理
│   │   ├── views/      (20+ 页面)         # GG / FB / Admin 视图
│   │   ├── components/ (15+ 组件)         # 模态框/侧边栏/表格
│   │   ├── composables/                   # useDebounce, usePagination
│   │   └── utils/                         # 环境/剪贴板/状态标签
│   └── vite.config.js
├── src/main/java/com/lmserver/
│   ├── LmServerApplication.java           # 启动类 (@EnableScheduling)
│   ├── config/                            # 7 个配置类
│   │   ├── SecurityConfig.java            # SessionCreationPolicy.STATELESS
│   │   ├── JwtConfig / WebConfig / AsyncConfig / CacheConfig
│   │   ├── MybatisPlusConfig / ScheduledTasks
│   ├── security/                          # 4 个安全组件
│   │   ├── JwtTokenProvider.java          # Token 生成/验证/续签 (30%滑动过期)
│   │   ├── JwtAuthenticationFilter.java   # OncePerRequestFilter
│   │   ├── PlatformGuardFilter.java       # AntPathMatcher GG/FB 隔离
│   │   └── UserPrincipal.java
│   ├── controller/                        # 39 个控制器
│   │   ├── auth/AuthController            # 12 接口: 登录/注册/Token/个人信息/改密
│   │   ├── gg/ (15)                       # Account/Mcc/Product/Package/Recharge/
│   │   │                                  # AdReport/Youtube/Video/Copywriting/
│   │   │                                  # Scrape/Settings/Delist/ProductAsset/
│   │   │                                  # ProductRunner/AccountMccHistory
│   │   ├── fb/ (9)                        # FbBm/FbAccount/FbProduct/FbPixel/
│   │   │                                  # FbPixelBm/FbLines/FbExtract/
│   │   │                                  # FbAdReport/FbUser
│   │   ├── admin/ (3)                     # Admin/AdminData/AdminTrigger
│   │   └── 根控制器 (11)                   # Health/Config/Option/Data/Font/
│   │                                      # Audit/Utility/GoogleSheets/
│   │                                      # GoogleAds/ImportHistory/SheetsSyncLog
│   ├── service/                           # 15 接口 + 10 实现 + 5 AI Provider
│   │   ├── AuthService / AccountService / ProductService
│   │   ├── FbService / MccService / RechargeService
│   │   ├── OptionService / CopywritingService
│   │   ├── NotificationService / DataImportExportService
│   │   ├── GoogleSheetsService (upsert/双向同步)
│   │   ├── GoogleAdsService / FfmpegService
│   │   ├── FbExtractService / DelistChecker
│   │   └── ai/ — AiVideoService + 4 Provider (Atlas/Veo/Doubao/Seedance)
│   ├── entity/                            # 40 个 MyBatis-Plus 实体
│   │   ├── common/ (11)  — Users, Config, Agents, AuditLog, ...
│   │   ├── gg/     (18)  — Accounts, Mcc, Products, Videos, ...
│   │   └── fb/     (11)  — FbAccounts, FbBms, FbProducts, ...
│   ├── mapper/                            # 40+ Mapper (MyBatis-Plus)
│   │   ├── common/ (11) / gg/ (18) / fb/ (11)
│   ├── dto/                               # 数据传输对象
│   │   ├── request/  — LoginRequest, RegisterRequest
│   │   ├── response/ — ApiResponse, PagedResponse, LoginResponse, SyncResult, ApiError
│   │   └── sheets/   — ZuobiaoRow, FbReportRow
│   ├── enums/UserRole.java                # developer/admin/viewer/user/hidden
│   ├── exception/                         # 全局异常处理 + 业务异常 + 平台禁止
│   └── util/                              # JwtUtil / PasswordUtil(BCrypt) / DateUtil
├── src/main/resources/
│   ├── application.yml                    # MySQL/JWT/邮件/FFmpeg/AI/Sheets 配置
│   ├── schema.sql                         # 40 张表 DDL
│   ├── data.sql                           # 测试数据
│   ├── fit-boulevard-503111-u4-812bc02c2000.json  # Google SA 密钥
│   └── static/                            # 前端构建产物 (生产部署)
├── src/test/java/com/lmserver/            # 2 个测试类
└── docs/                                  # 本文档目录
    ├── README.md                          # 本文件
    ├── api/api-reference.md               # API 参考
    ├── architecture/security.md           # 安全架构说明
    ├── development/                       # 开发阶段记录 (Phase 1-4)
    └── superpowers/specs/                 # 设计文档
```

## API 端点

### 认证 (`/api/auth/*`) — 12 个

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 无 | 登录 |
| POST | `/api/auth/register` | 无 | 注册 |
| POST | `/api/auth/refresh` | 无 | 刷新 Token |
| GET | `/api/auth/me` | JWT | 当前用户 |
| PUT | `/api/auth/password` | JWT | 修改密码 |
| PUT | `/api/auth/profile` | JWT | 更新个人信息 |
| PUT | `/api/auth/custom-name` | JWT | 自定义名称 |
| PUT | `/api/auth/email` | JWT | 更新邮箱 |
| PUT | `/api/auth/telegram-username` | JWT | 更新 Telegram |
| GET | `/api/auth/names` | JWT | 用户名称列表 |
| GET | `/api/auth/config` | JWT | 获取用户配置 |
| PUT | `/api/auth/config` | JWT | 更新用户配置 |

### GG (Google Ads) 平台

| 前缀 | 接口数 | 核心功能 |
|------|--------|---------|
| `/api/accounts/*` | 21 | 广告账户 CRUD + Sheet 双向同步 + 软删除/恢复/物理删除 |
| `/api/products/*` | 17 | 产品 CRUD + 包管理 + 在跑人员 |
| `/api/mcc/*` | 8 | MCC CRUD + 详情 + 关联 |
| `/api/recharge/*` | 5 | 单个/批量充值 + Sheet 写入 |
| `/api/ad-reports/*` | 17 | 报告上传/去重/分析/导出 |
| `/api/youtube/*` | 16 | 视频导入/列表/编辑/消费追踪 + 标签 |
| `/api/video/*` + `/api/audio*` | 16 | AI 视频生成 + FFmpeg 合成 + 音频替换 |
| `/api/copywriting/*` | 5 | 文案导入/列表/编辑/删除 |
| `/api/scrape/*` | 5 | Google Play 截图抓取/上传 |
| `/api/delist/*` | 2 | 掉包检测/通知 |
| `/api/settings/*` | 2 | 账户设置 |

### FB (Facebook Ads) 平台

| 前缀 | 接口数 | 核心功能 |
|------|--------|---------|
| `/api/fb/bms/*` | 7 | BM CRUD + 封禁迁移 |
| `/api/fb/accounts/*` | 8 | 账户 CRUD + BM 关联 + 软删除/恢复 |
| `/api/fb/products/*` | 10 | 产品 + 线名 + 在跑人员 + BM 关联 |
| `/api/fb/pixels/*` | 5 | Pixel CRUD + 关联 |
| `/api/fb/pixel-bms/*` | 5 | Pixel BM CRUD |
| `/api/fb/lines/*` | 3 | 广告线/落地页 |
| `/api/fb/extract/*` | 3 | 数据解析/去重/保存 (异步写 Sheet) |
| `/api/fb/reports/*` | 9 | 报告 CRUD/统计/导出/Sheet 同步 |
| `/api/fb/users` | 1 | FB 用户查询 |

### 通用 & 管理

| 前缀 | 接口数 | 核心功能 |
|------|--------|---------|
| `/api/admin/*` | 11 | 用户管理 + 数据导入 + 定时任务触发 |
| `/api/{agents\|statuses\|mcc-levels\|sales-persons\|regions}/*` | 20 | 选项数据 CRUD |
| `/api/config/*` | 6 | AI/Sheets 等系统配置 |
| `/api/data/*` | 3 | 数据导入导出 + 历史 |
| `/api/fonts/*` | 7 | 字体导入/预览/上传 |
| `/api/browse-*` + `/api/translate` | 5 | 文件浏览 + 翻译 |
| `/api/audit-log/*` | 2 | 审计日志 |
| `/api/import-history/*` | 1 | 导入历史 |
| `/api/google-sheets/*` | 4 | Sheets 手动触发 |
| `/api/google-ads/*` | 2 | Google Ads API |
| `/api/health` | 1 | 健康检查 |

## 数据库

- **MySQL 8.0.40** (D:\work\mysql\mysql-8.0.40-winx64)
- 数据库: `lmserver` (utf8mb4_unicode_ci)
- 40 张表 + 测试数据（从 GG-Server SQLite 迁移）

### 表分类

| 分类 | 表数 | 主要表 |
|------|------|--------|
| 用户与认证 | 2 | `users`, `config` |
| GG 选项 | 5 | `agents`, `account_statuses`, `mcc_levels`, `sales_persons`, `regions` |
| GG MCC/账户 | 5 | `mcc`, `accounts`, `account_mcc_history`, `recharge_records`, `sheets_sync_log` |
| GG 产品/包 | 6 | `products`, `product_runners`, `packages`, `copywritings`, `product_assets`, `import_history` |
| YouTube/视频 | 7 | `videos`, `tags`, `video_history`, `video_tasks`, `audio_replace_history`, `ad_reports`, `video_consumption` |
| 掉包/审计/抓取 | 4 | `delist_checks`, `delist_notifications`, `audit_log`, `scrape_cache` |
| FB 平台 | 11 | `fb_bms`, `fb_accounts`, `fb_account_bm`, `fb_account_bm_history`, `fb_products`, `fb_product_runners`, `fb_product_bms`, `fb_pixel_bms`, `fb_pixels`, `fb_lines`, `fb_ad_reports` |

## 安全架构

### 认证流程

```
请求 → JwtAuthenticationFilter (OncePerRequestFilter)
     → 从 Authorization: Bearer <token> 提取
     → JwtTokenProvider.validateToken() + tokenVersion 校验
     → 设置 SecurityContext (UserPrincipal)
     → PlatformGuardFilter 检查 GG/FB 平台隔离
     → Controller
```

### Session 管理

使用 `SessionCreationPolicy.STATELESS` — 完全不依赖 HTTP Session，每次请求携带 JWT Token 独立验证。剩余有效期 < 30% 时自动在响应头 `x-new-access-token` 签发新 Token。

### 平台隔离

`PlatformGuardFilter` 使用 `AntPathMatcher` 做路径匹配，阻止 `platform=fb` 的用户访问 GG 专属路由 (`/api/accounts/**`, `/api/products/**`, `/api/mcc/**` 等)。`developer` 角色豁免平台限制。

### JWT Token 包含字段

| 字段 | 说明 |
|------|------|
| `sub` | 用户 ID |
| `role` | 角色 (developer/admin/viewer/user/hidden) |
| `platform` | 平台 (gg/fb) |
| `tokenVersion` | Token 版本号 (改密/禁用时递增，强制旧 Token 失效) |
| `iat` | 签发时间 |
| `exp` | 过期时间 (Access: 1小时, Refresh: 30天) |

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SERVER_PORT` | 服务端口 | 8080 |
| `JWT_SECRET` | JWT 签名密钥 | 开发默认值 (生产必改) |

生产环境生成密钥: `openssl rand -base64 64`

## 迁移阶段

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | 项目骨架搭建 + JWT 安全 | ✅ 完成 |
| Phase 2 | 前端拷贝 + MySQL 数据迁移 | ✅ 完成 |
| Phase 3 | 40 个 Entity + Mapper | ✅ 完成 |
| Phase 4 | GG 核心业务 (Controller/Service) | ✅ 完成 |
| Phase 5 | FB 核心业务 (Controller/Service) | ✅ 完成 |
| Phase 6 | 辅助功能 (视频/AI/通知/抓取/字体/掉包) | ✅ 完成 |
| Phase 7 | 测试与上线 | 🔄 进行中 (目前 2 个测试类，需补充) |

## 与 Python GG-Server 的关键差异

| 维度 | Python GG-Server | Java LM-Server |
|------|-----------------|----------------|
| 框架 | Flask (单文件 ~9,600行) | Spring Boot (178 文件) |
| 数据库 | SQLite (WAL) | MySQL 8.0 (InnoDB) |
| ORM | 原生 SQL | MyBatis-Plus |
| 并发 | Waitress 40线程 + GIL | Tomcat NIO + HikariCP (30连接) |
| 认证 | flask-jwt-extended | Spring Security + jjwt (无状态) |
| 异步 | `threading.Thread` | `@Async` + `CompletableFuture` |
| 定时任务 | `threading.Timer` | `@Scheduled` |
| 缓存 | 内存 dict + TTL | Caffeine |
| 会话 | JWT 无状态 | `SessionCreationPolicy.STATELESS` |
| 类型安全 | 动态类型 | 编译期检查 |
| 部署 | pyinstaller EXE | `java -jar` fat JAR |
