# LM-Server — 卡天皇运营工具箱后端

> **从 Python Flask → Java Spring Boot 完整迁移**  
> 原项目: GG-Server (Python 3 + Flask + SQLite)  
> 新项目: LM-Server (Java 17 + Spring Boot 3.3 + MySQL 8.0)  

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.3.0 |
| 安全 | Spring Security + JWT (jjwt) | 6.x / 0.12.5 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0.40 |
| 连接池 | HikariCP | (Spring Boot 默认) |
| 缓存 | Caffeine | (Spring Cache 抽象) |
| API 文档 | SpringDoc (Swagger UI) | 2.6.0 |
| 映射 | MapStruct | 1.5.5 |
| HTML 解析 | Jsoup | 1.17.2 |
| 限流 | Bucket4j | 8.7.0 |
| 前端 | Vue 3 + Vite + Element Plus + Pinia | 3.5 / 6.0 / 2.9 / 2.3 |
| 构建 | Maven | 3.9+ |

## 架构

```
┌──────────────────┐     ┌──────────────────────────────────────┐
│  Vue 3 前端       │────▶│  Spring Boot 3.3 (Tomcat, 端口 8080) │
│  Vite :5173       │     │                                      │
│  Hash Router      │     │  SecurityConfig                     │
│  baseURL: /api    │     │  └─ SessionCreationPolicy.STATELESS │
└──────────────────┘     │     └─ JWT (无状态认证)               │
                          │     └─ PlatformGuard (GG/FB 隔离)    │
                          │                                      │
                          │  Controller → Service → Mapper       │
                          │  (39 Controller, 15 Service, 40+ Mapper) │
                          │                                      │
                          │  MySQL 8.0 (HikariCP 连接池)         │
                          │  外部: Google Sheets/Ads API, FFmpeg │
                          └──────────────────────────────────────┘
```

### 关键设计决策

- **`SessionCreationPolicy.STATELESS`** — 无状态会话，每次请求携带 JWT Token (Bearer)，不依赖 `HttpSession`
- **MyBatis-Plus** 替代设计文档中的 JPA — 使用 Mapper 模式，更灵活地处理复杂 SQL
- **`com.lmserver`** 基础包名，按 `controller.{gg|fb|auth|admin}` 分包隔离 GG/FB 平台
- **PlatformGuardFilter** — 使用 `AntPathMatcher` 阻止 FB 用户访问 GG 专属路由
- **统一响应** — `ApiResponse<T>` (单对象) / `PagedResponse<T>` (分页列表，字段名 `items` 对齐前端)

## 项目规模

| 维度 | 数量 |
|------|------|
| Java 源文件 | 178 个 |
| 代码行数 | ~8,190 行 |
| API 接口 | 239 个 (计划) |
| 数据库表 | 40 张 |
| Entity 实体 | 40 个 |
| Mapper | 40+ 个 |
| Controller | 39 个 |
| Service 接口 + 实现 | 15 + 10 |
| AI Provider | 4 个 (Atlas / Veo / Doubao / Seedance) |
| 前端页面/组件 | 30+ 个 Vue SFC |
| 用户角色 | 5 级 (developer / admin / viewer / user / hidden) |

## 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.9+**
- **MySQL 8.0.13+** (localhost:3306, 用户 `root`, 密码 `123456`)
- **Node.js 18+** (仅前端开发)
- **FFmpeg** (视频处理, 需在 PATH 中)

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS lmserver CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构 (可选 — Spring Boot 启动时 MyBatis-Plus 会自动创建)
mysql -u root -p123456 lmserver < src/main/resources/schema.sql
```

### 2. 启动后端

```bash
cd LM-Server

# 方式一: Maven 启动
mvn spring-boot:run

# 方式二: 打包运行
mvn clean package -DskipTests
java -jar target/lm-server-0.1.0-SNAPSHOT.jar
```

> 服务启动在 **http://localhost:8080**  
> Swagger UI: **http://localhost:8080/swagger-ui.html**

### 3. 启动前端 (开发模式)

```bash
cd LM-Server/frontend
npm install
npm run dev
```

> 前端启动在 **http://localhost:5173**，API 请求代理到 `:8080`

### 4. 生产部署

```bash
# 前端构建产物已包含在 src/main/resources/static/ 下
# 直接访问 http://localhost:8080 即可
mvn clean package -DskipTests
java -jar target/lm-server-0.1.0-SNAPSHOT.jar
```

## 测试账户

```json
POST /api/auth/login
{"username":"carl567", "password":"1976xiaobai"}
```

## 项目结构

```
LM-Server/
├── pom.xml                                     # Maven 依赖
├── README.md                                   # 本文件
├── frontend/                                   # Vue 3 + Vite 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.js                             # 入口
│       ├── App.vue                             # 根组件
│       ├── router/index.js                     # 路由 (Hash 模式)
│       ├── api/                                # API 客户端 (axios)
│       │   ├── client.js                       # 拦截器 + Token 管理
│       │   ├── auth.js, accounts.js, fb.js, ... # 按模块拆分
│       ├── stores/                             # Pinia 状态管理
│       │   ├── auth.js, accounts.js, products.js, ...
│       ├── views/                              # 页面视图
│       │   ├── LoginView.vue, RegisterView.vue
│       │   ├── AccountsView.vue, MccPanel.vue, ...
│       │   ├── YoutubeView.vue, MediaView.vue, ...
│       │   ├── fb/FbAccountPanel.vue, fb/FbBmPanel.vue, ...
│       │   └── admin/UserManageView.vue, ...
│       ├── components/                         # 可复用组件
│       │   ├── AccountModal.vue, MccModal.vue, ...
│       │   └── GlobalTaskPanel.vue, AppSidebar.vue
│       ├── composables/                        # 组合式函数
│       └── utils/                              # 工具函数
├── src/main/java/com/lmserver/
│   ├── LmServerApplication.java               # 启动类 (@EnableScheduling)
│   │
│   ├── config/                                 # Spring 配置
│   │   ├── SecurityConfig.java                 # 安全规则 + SessionCreationPolicy.STATELESS
│   │   ├── JwtConfig.java                      # JWT 属性绑定
│   │   ├── WebConfig.java                      # CORS 跨域
│   │   ├── AsyncConfig.java                    # @Async 线程池
│   │   ├── CacheConfig.java                    # Caffeine 缓存
│   │   ├── MybatisPlusConfig.java              # MyBatis-Plus 配置
│   │   └── ScheduledTasks.java                 # 定时任务
│   │
│   ├── security/                               # 安全组件
│   │   ├── JwtTokenProvider.java              # Token 生成/验证/续签
│   │   ├── JwtAuthenticationFilter.java        # JWT 过滤器 (OncePerRequestFilter)
│   │   ├── PlatformGuardFilter.java            # GG/FB 平台隔离守卫
│   │   └── UserPrincipal.java                 # 认证主体
│   │
│   ├── controller/                             # 控制器 (按模块分包)
│   │   ├── auth/AuthController.java            # 12 个接口: 登录/注册/Token/个人信息/改密
│   │   ├── gg/                                 # GG (Google Ads) 平台
│   │   │   ├── AccountController.java          # 21 接口: 广告账户 CRUD + Sheet 双向同步 + 软删除
│   │   │   ├── MccController.java              # 8 接口: MCC 管理
│   │   │   ├── ProductController.java          # 17 接口: 产品管理
│   │   │   ├── PackageController.java          # 包/素材系列
│   │   │   ├── RechargeController.java         # 5 接口: 充值管理
│   │   │   ├── AdReportController.java         # 17 接口: 广告报告
│   │   │   ├── YoutubeController.java          # 16 接口: YouTube 视频
│   │   │   ├── VideoController.java            # 视频生成/AI
│   │   │   ├── CopywritingController.java      # 5 接口: 文案管理
│   │   │   ├── ScrapeController.java           # 图片抓取
│   │   │   ├── SettingsController.java         # 账户/产品设置
│   │   │   ├── DelistController.java           # 掉包检测
│   │   │   ├── ProductAssetController.java, ProductRunnerController.java, AccountMccHistoryController.java
│   │   ├── fb/                                 # FB (Facebook Ads) 平台
│   │   │   ├── FbBmController.java            # 7 接口: BM 管理 + 封禁迁移
│   │   │   ├── FbAccountController.java       # 8 接口: 账户管理
│   │   │   ├── FbProductController.java       # 10 接口: 产品管理
│   │   │   ├── FbPixelController.java, FbPixelBmController.java  # Pixel 管理
│   │   │   ├── FbLinesController.java          # 广告线/落地页
│   │   │   ├── FbExtractController.java        # 数据提取
│   │   │   ├── FbAdReportController.java       # 9 接口: 报告
│   │   │   └── FbUserController.java           # FB 用户查询
│   │   ├── admin/                              # 管理员
│   │   │   ├── AdminController.java            # 用户管理
│   │   │   ├── AdminDataController.java        # 数据导入导出
│   │   │   └── AdminTriggerController.java     # 定时任务触发
│   │   ├── HealthController.java               # 健康检查
│   │   ├── ConfigController.java               # 系统配置
│   │   ├── OptionController.java               # 选项数据 (代理/状态/商务/地区)
│   │   ├── DataController.java                 # 数据导入导出
│   │   ├── FontController.java                 # 字体管理
│   │   ├── AuditController.java                # 审计日志
│   │   ├── UtilityController.java              # 文件浏览/翻译
│   │   ├── GoogleSheetsController.java         # Sheets 手动触发
│   │   ├── GoogleAdsController.java            # Google Ads API
│   │   ├── ImportHistoryController.java        # 导入历史
│   │   └── SheetsSyncLogController.java        # Sheets 同步日志
│   │
│   ├── service/                                # 业务服务
│   │   ├── AuthService.java / impl/AuthServiceImpl.java
│   │   ├── AccountService.java / impl/AccountServiceImpl.java
│   │   ├── ProductService.java / impl/ProductServiceImpl.java
│   │   ├── FbService.java / impl/FbServiceImpl.java
│   │   ├── MccService.java / impl/MccServiceImpl.java
│   │   ├── RechargeService.java / impl/RechargeServiceImpl.java
│   │   ├── OptionService.java / impl/OptionServiceImpl.java
│   │   ├── CopywritingService.java / impl/CopywritingServiceImpl.java
│   │   ├── NotificationService.java / impl/NotificationServiceImpl.java
│   │   ├── DataImportExportService.java / impl/DataImportExportServiceImpl.java
│   │   ├── GoogleSheetsService.java            # Sheets 核心: upsert/读取/双向同步
│   │   ├── GoogleAdsService.java               # Google Ads API
│   │   ├── FfmpegService.java                  # FFmpeg 视频处理
│   │   ├── FbExtractService.java               # FB 数据提取
│   │   ├── DelistChecker.java                  # 掉包检测 (定时)
│   │   └── ai/                                 # AI 视频生成
│   │       ├── AiVideoService.java             # 策略接口
│   │       ├── AiVideoProvider.java            # 抽象 Provider
│   │       ├── AtlasProvider.java              # Atlas/Seedance
│   │       ├── DoubaoProvider.java             # 豆包
│   │       └── VeoProvider.java                # Google Veo
│   │
│   ├── entity/                                 # MyBatis-Plus 实体
│   │   ├── common/ (11: Users, Config, Agents, AuditLog, ...)
│   │   ├── gg/     (18: Accounts, Mcc, Products, Videos, ...)
│   │   └── fb/     (11: FbAccounts, FbBms, FbProducts, ...)
│   │
│   ├── mapper/                                 # MyBatis-Plus Mapper
│   │   ├── common/ (11)
│   │   ├── gg/     (18)
│   │   └── fb/     (11)
│   │
│   ├── dto/                                    # 数据传输对象
│   │   ├── request/  (LoginRequest, RegisterRequest)
│   │   ├── response/ (ApiResponse, PagedResponse, LoginResponse, ApiError, SyncResult)
│   │   └── sheets/   (ZuobiaoRow, FbReportRow)
│   │
│   ├── enums/UserRole.java                     # developer/admin/viewer/user/hidden
│   ├── exception/                              # 全局异常处理
│   ├── util/                                   # JwtUtil, PasswordUtil(BCrypt), DateUtil
│   │
│   └── resources/
│       ├── application.yml                     # 主配置 (MySQL/JWT/邮件/FFmpeg/AI/Sheets)
│       ├── schema.sql                          # 40 张表 DDL
│       ├── data.sql                            # 测试数据
│       ├── fit-boulevard-503111-u4-812bc02c2000.json  # Google SA 密钥
│       └── static/                             # 前端构建产物 (生产)
│
├── src/test/java/com/lmserver/
│   ├── LmServerApplicationTests.java           # 上下文加载测试
│   └── service/AuthServiceTest.java            # Auth 业务测试
│
└── docs/                                       # 项目文档
    ├── README.md                               # 项目文档索引
    ├── api/api-reference.md                    # API 参考
    ├── architecture/security.md                # 安全架构说明
    ├── development/                            # 开发阶段记录
    └── superpowers/specs/                      # 设计文档
```

## API 端点汇总

### 认证 (`/api/auth/*`) — 12 个

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/auth/login` | 无 | 登录，返回 accessToken + refreshToken |
| POST | `/api/auth/register` | 无 | 注册 |
| POST | `/api/auth/refresh` | 无 | 刷新 Token |
| GET | `/api/auth/me` | JWT | 当前用户信息 |
| PUT | `/api/auth/password` | JWT | 修改密码 |
| PUT | `/api/auth/profile` | JWT | 更新个人信息 |
| PUT | `/api/auth/custom-name` | JWT | 自定义名称 |
| PUT | `/api/auth/email` | JWT | 更新邮箱 |
| PUT | `/api/auth/telegram-username` | JWT | 更新 Telegram |
| GET | `/api/auth/names` | JWT | 用户名称列表 |
| GET | `/api/auth/config` | JWT | 获取用户配置 |
| PUT | `/api/auth/config` | JWT | 更新用户配置 |

### GG (Google Ads) 平台 — 主要模块

| 前缀 | 接口数 | 说明 |
|------|--------|------|
| `/api/accounts/*` | 21 | 广告账户 CRUD + 双向 Sheet 同步 + 软删除/恢复/物理删除 |
| `/api/products/*` | 17 | 产品管理 + 包 + 在跑人员 |
| `/api/mcc/*` | 8 | MCC 管理 |
| `/api/recharge/*` | 5 | 充值管理 |
| `/api/ad-reports/*` | 17 | 广告报告 |
| `/api/youtube/*` | 16 | YouTube 视频管理 |
| `/api/video/*` + `/api/audio*` | 16 | AI 视频生成 + 音频替换 |
| `/api/copywriting/*` | 5 | 文案管理 |
| `/api/scrape/*` | 5 | Google Play 图片抓取 |
| `/api/delist/*` | 2 | 掉包检测 |

### FB (Facebook Ads) 平台 — 主要模块

| 前缀 | 接口数 | 说明 |
|------|--------|------|
| `/api/fb/bms/*` | 7 | BM 管理 + 封禁迁移 |
| `/api/fb/accounts/*` | 8 | 账户管理 + 软删除/恢复 |
| `/api/fb/products/*` | 10 | 产品 + 线名 + 在跑人员 |
| `/api/fb/pixels/*` | 5 | Pixel 管理 |
| `/api/fb/pixel-bms/*` | 5 | Pixel BM 管理 |
| `/api/fb/extract/*` | 3 | 数据提取 (异步写 Sheet) |
| `/api/fb/reports/*` | 9 | 广告报告 + Sheet 同步 |
| `/api/fb/lines/*` | 3 | 广告线/落地页 |

### 通用 & 管理

| 前缀 | 接口数 | 说明 |
|------|--------|------|
| `/api/admin/*` | 11 | 用户管理、数据导入、定时任务触发 |
| `/api/agents\|statuses\|.../*` | 20 | 选项数据 CRUD |
| `/api/config/*` | 6 | 系统配置 (AI/Sheets 等) |
| `/api/data/*` | 3 | 数据导入导出 |
| `/api/fonts/*` | 7 | 字体管理 |
| `/api/settings/*` | 2 | 账户设置 |
| `/api/browse-*` + `/api/translate` | 5 | 文件浏览、翻译 |
| `/api/audit-log/*` | 2 | 审计日志 |
| `/api/health` | 1 | 健康检查 |

## 数据库

- **MySQL 8.0.40** (`D:\work\mysql\mysql-8.0.40-winx64`)
- 数据库: `lmserver` (utf8mb4)
- 字符集: `utf8mb4_unicode_ci`
- 引擎: InnoDB
- 40 张表 (设计文档 DDL 见 `src/main/resources/schema.sql`)

### 表分类

| 分类 | 表数 | 说明 |
|------|------|------|
| 用户与认证 | 2 | `users`, `config` |
| GG 选项 | 5 | `agents`, `account_statuses`, `mcc_levels`, `sales_persons`, `regions` |
| GG MCC/账户 | 5 | `mcc`, `accounts`, `account_mcc_history`, `recharge_records`, `sheets_sync_log` |
| GG 产品/包 | 6 | `products`, `product_runners`, `packages`, `copywritings`, `product_assets`, `import_history` |
| YouTube/视频 | 7 | `videos`, `tags`, `video_history`, `video_tasks`, `audio_replace_history`, `ad_reports`, `video_consumption` |
| 掉包/审计 | 4 | `delist_checks`, `delist_notifications`, `audit_log`, `scrape_cache` |
| FB 平台 | 11 | `fb_bms`, `fb_accounts`, `fb_account_bm`, `fb_account_bm_history`, `fb_products`, `fb_product_runners`, `fb_product_bms`, `fb_pixel_bms`, `fb_pixels`, `fb_lines`, `fb_ad_reports` |

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SERVER_PORT` | 服务端口 | `8080` |
| `JWT_SECRET` | JWT 签名密钥 (生产必改) | `Z2ctc2VydmVyLWp3dC1zZWNyZXQt...` |

### JWT 安全要求

启动时强制校验 `JWT_SECRET`：
- 长度 ≥ 32 字符
- 不得使用默认值
- 生产环境生成: `openssl rand -base64 64`

### Session 管理

使用 `SessionCreationPolicy.STATELESS`，完全不依赖 HTTP Session。认证通过 `Authorization: Bearer <token>` 头传递，每次请求独立验证。剩余有效期 < 30% 时自动在响应头 `x-new-access-token` 中签发新 Token。

## 定时任务

| 任务 | Cron | 说明 |
|------|------|------|
| 周清 | `0 0 2 * * SUN` | 每周日凌晨 2:00 |
| 掉包检测 | `0 0 * * * *` | 每小时整点 |

## 迁移状态

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | 项目骨架 + Spring Security + JWT | ✅ 完成 |
| Phase 2 | 前端拷贝 + MySQL 数据迁移 | ✅ 完成 |
| Phase 3 | 40 实体 + Mapper (MyBatis-Plus) | ✅ 完成 |
| Phase 4 | GG 核心业务 Controller/Service | ✅ 基本完成 |
| Phase 5 | FB 核心业务 Controller/Service | ✅ 基本完成 |
| Phase 6 | 辅助功能 (AI 视频/通知/抓取) | ✅ 基本完成 |
| Phase 7 | 测试与上线 | 🔄 进行中 |

> **注意**: 当前测试覆盖较少 (2 个测试类)，建议在正式上线前补充核心业务的集成测试。

## 与原 Python 后端的差异

| 维度 | Python GG-Server | Java LM-Server |
|------|-----------------|----------------|
| 框架 | Flask (单文件 ~9600行) | Spring Boot (178文件) |
| 数据库 | SQLite (WAL) | MySQL 8.0 |
| 并发 | Waitress 40线程 + GIL | Tomcat NIO + HikariCP |
| ORM | 原生 SQL (mysql-connector) | MyBatis-Plus |
| 认证 | flask-jwt-extended | Spring Security + jjwt |
| 异步 | `threading.Thread` | `@Async` + `CompletableFuture` |
| 定时任务 | `threading.Timer` | `@Scheduled` |
| 缓存 | 内存 dict + TTL | Caffeine |
| 部署 | pyinstaller EXE | `java -jar` fat JAR |
| 类型 | 动态类型 | 编译期检查 |
| 会话 | JWT 无状态 | `SessionCreationPolicy.STATELESS` |

## 参考文档

- 设计文档: `docs/superpowers/specs/2026-07-31-spring-boot-migration-design.md` (GG-Server 下)
- API 参考: `docs/api/api-reference.md`
- 安全架构: `docs/architecture/security.md`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
