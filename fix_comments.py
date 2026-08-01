import os, re

SRC = r"D:\server\cc\LM-Server\src\main\java\com\lmserver"

# 类的完整中文描述
CLASS_CMT = {
    "LmServerApplication": "Spring Boot 启动类 — 应用入口，@SpringBootApplication",
    "SecurityConfig": "Spring Security 安全配置 — JWT 无状态认证 + 路由权限规则 + BCrypt 密码编码器",
    "JwtConfig": "JWT 配置属性 — 绑定 application.yml 中 jwt.* 到 Java 对象",
    "WebConfig": "Web MVC 配置 — CORS 跨域规则，允许前端跨域访问，暴露 x-new-access-token 头",
    "AsyncConfig": "异步线程池配置 — @Async 注解的执行器，核心5线程最大20，队列100",
    "CacheConfig": "本地缓存配置 — 基于 Caffeine，写入后60秒过期，最大1000条",
    "MybatisPlusConfig": "MyBatis-Plus 配置 — 注册分页插件（MySQL 方言），自动处理物理分页",
    "JwtAuthenticationFilter": "JWT 认证过滤器 — 从 Authorization 头提取 Bearer Token，校验后设置 SecurityContext，剩余有效期不足30%时自动续签",
    "JwtTokenProvider": "JWT Token 提供者 — 桥接 JwtUtil 与 Spring Security，启动时校验密钥强度，拒绝弱默认密钥",
    "PlatformGuardFilter": "平台守卫过滤器 — FB 平台非 developer 用户禁止访问 GG 专属路由，使用 AntPathMatcher 防路径绕过",
    "UserPrincipal": "Spring Security 用户主体 — 从 JWT Claims 中提取 userId/role/platform/tokenVersion，实现 UserDetails 接口",
    "BusinessException": "业务异常基类 — 携带 HTTP 状态码和可选错误码，由 GlobalExceptionHandler 统一转换为 ApiResponse",
    "UnauthorizedException": "401 未认证异常 — Token 无效、过期或缺失",
    "PlatformForbiddenException": "403 平台禁止异常 — 跨平台越权访问",
    "GlobalExceptionHandler": "全局异常处理器 — @RestControllerAdvice，将各类异常统一转换为 ApiResponse.fail() 格式",
    "UserRole": "用户角色枚举 — developer(最高权限，跨平台) > admin > viewer(只读) > user > hidden(禁止登录)",
    "JwtUtil": "JWT 核心工具 — HMAC-SHA256 Token 生成/解析/校验/字段提取，纯函数无 Spring 依赖",
    "PasswordUtil": "BCrypt 密码工具 — 封装 PasswordEncoder，替代 Python werkzeug pbkdf2",
    "DateUtil": "日期时间工具 — yyyy-MM-dd HH:mm:ss 和 yyyy-MM-dd 格式化",
    "ApiResponse": "统一 API 响应 — {success: bool, data: T, error: string}，与 Python helpers.ok() 兼容",
    "PagedResponse": "分页列表响应 — {items: [...], total, page, size}，字段名 items 匹配前端 res.data.items",
    "LoginResponse": "登录成功响应 — accessToken(1小时) + refreshToken(30天) + 用户信息(id/username/role/platform/displayName)",
    "LoginRequest": "登录请求 — @NotBlank 校验的 username + password",
    "RegisterRequest": "注册请求 — username(4-20字符) + password(最少6字符) + displayName",
    "AuthService": "认证服务接口 — 登录/注册/Token刷新/获取当前用户",
    "AuthController": "认证控制器 — /api/auth/*，处理登录/注册/Token刷新/个人信息，login和register公开访问",
    "HealthController": "健康检查控制器 — GET /api/health，返回服务运行状态",
    "OptionService": "选项管理服务接口 — 统一处理 agents/statuses/mcc-levels/sales-persons/regions 五个选项表",
    "OptionController": "选项管理控制器 — /api/{type}/*，通过路径变量统一分发5个选项表的CRUD",
    "MccService": "MCC 管理服务接口 — 多条件分页查询(名称/ID搜索+等级筛选)",
    "MccController": "MCC 管理控制器 — /api/mcc/*，GG平台MCC的CRUD+下拉选项",
    "ProductService": "产品管理服务接口 — 按名称/地区/状态筛选的产品分页查询",
    "ProductController": "产品管理控制器 — /api/products/*，GG平台产品的完整CRUD+下拉选项",
    "AccountService": "账户管理服务接口 — 多条件筛选(名称/账号ID/状态/MCC/代理)+软删除",
    "AccountController": "账户管理控制器 — /api/accounts/*，GG平台广告账户的CRUD+软删除+下拉选项",
    "RechargeService": "充值管理服务接口 — 按账户ID筛选的充值记录查询",
    "RechargeController": "充值管理控制器 — /api/recharge/*，GG平台充值记录的CRUD",
    "FbService": "FB 平台服务接口 — 统一管理 BM/账户/产品 三个子模块",
    "FbBmController": "FB BM 管理控制器 — /api/fb/bms/*，BM的CRUD+软删除+下拉选项",
    "FbAccountController": "FB 账户管理控制器 — /api/fb/accounts/*，FB广告账户的CRUD+软删除",
    "FbProductController": "FB 产品管理控制器 — /api/fb/products/*，FB产品的CRUD+下拉选项",
    "FbAdReportController": "FB 广告报告控制器 — /api/fb/reports/*，FB广告投放数据的导入查询",
    "AdReportController": "GG 广告报告控制器 — /api/ad-reports/*，GG广告投放数据的CRUD",
    "CopywritingService": "文案管理服务接口 — 按地区和归属用户筛选的文案CRUD",
    "CopywritingController": "文案管理控制器 — /api/copywriting/*，营销文案的CRUD+批量删除",
    "AuditController": "审计日志控制器 — /api/audit-log/*，操作审计记录的查询",
    "ConfigController": "系统配置控制器 — /api/config/*，键值对配置的查询和修改",
    "SettingsController": "系统设置控制器 — /api/settings/*，批量配置保存+标签管理",
    "DelistController": "掉包检测控制器 — /api/delist/*，查询Google Play应用下架检测结果",
    "ScrapeController": "图片抓取控制器 — /api/scrape/*，Google Play截图抓取缓存管理",
    "AdminController": "管理员控制器 — /api/admin/*，用户列表/编辑/禁用，@PreAuthorize控制权限",
}

# 方法的中文描述
METHOD_CMT = {
    "login": "用户登录 — 验证用户名密码，成功返回 JWT Token 和用户信息",
    "register": "用户注册 — 创建新账户，默认角色 user，平台 gg",
    "refresh": "刷新 Token — 用 Refresh Token 换取新的 Access Token",
    "me": "获取当前用户信息 — 从 JWT 解析用户 ID 后查询数据库",
    "health": "健康检查 — 返回服务运行状态",
    "list": "分页列表查询 — 支持多条件筛选",
    "detail": "获取单条记录详情 — 按主键 ID 查询",
    "getById": "按 ID 查询 — 返回单条记录",
    "create": "新增记录 — 返回创建后的完整对象",
    "update": "更新记录 — 部分字段更新，只改传入的非 null 字段",
    "delete": "删除记录",
    "options": "获取下拉选项 — 返回 id + name 的简略列表",
    "batchDelete": "批量删除 — 按 ID 列表批量删除",
    "save": "保存配置 — 存在则更新，不存在则插入",
    "get": "获取单个配置 — 按 key 查询",
    "getAllConfig": "获取所有配置项 — 返回全部键值对",
    "saveConfig": "批量保存配置 — 遍历 Map 逐个 upsert",
    "getTags": "获取标签列表 — 返回所有标签键值对",
    "saveTag": "保存标签 — 按 key upsert",
    "getByProduct": "按产品 ID 查询 — 获取指定产品的检查结果",
    "listUsers": "获取用户列表 — 管理员功能，密码字段脱敏",
    "updateUser": "修改用户 — 可改角色/平台/显示名称",
    "deleteUser": "删除用户 — 软删除，设置角色为 hidden",
    "listBms": "BM 列表查询 — 支持名称/ID搜索和状态筛选",
    "getBmById": "按 ID 查询 BM",
    "createBm": "创建 BM — 新建商务管理平台记录",
    "updateBm": "更新 BM — 可改名和备注",
    "deleteBm": "删除 BM — 软删除，记录删除时间",
    "bmOptions": "BM 下拉选项 — 返回当前用户可见的 BM 列表",
    "listAccounts": "账户列表查询 — 多条件筛选（名称/账号ID/状态）",
    "getAccountById": "按 ID 查询账户",
    "createAccount": "创建账户 — 新建广告账户记录",
    "updateAccount": "更新账户 — 可改名称/状态/时区",
    "deleteAccount": "删除账户 — 软删除",
    "listProducts": "产品列表查询 — 支持名称搜索和地区筛选",
    "getProductById": "按 ID 查询产品",
    "createProduct": "创建产品 — 新建产品记录",
    "updateProduct": "更新产品 — 可改名称/KPI/地区/状态/商务/MCC/代理比例",
    "deleteProduct": "删除产品",
    "productOptions": "产品下拉选项 — 返回当前用户可见的产品列表",
    "listCache": "获取抓取缓存列表",
    "getCache": "按包名查询缓存",
    "clearCache": "清除指定包名的缓存",
    "getCurrentUser": "获取当前登录用户 — 从数据库查询完整用户信息",
    "refreshToken": "刷新 Access Token — 验证 Refresh Token 后签发新 Token",
    "printBcrypt": "临时方法：启动时打印 BCrypt 哈希，用于密码迁移",
    "contextLoads": "Spring 上下文加载测试",
    "validateSecret": "启动时校验 JWT 密钥 — 拒绝弱默认密钥，强制生产环境设置",
    "init": "初始化 JwtUtil — 从配置读取密钥和过期时间",
    "getAuthentication": "从 Token 构建 Authentication — 设置 SecurityContext",
    "validateToken": "校验 Token 签名和有效期",
    "createAccessToken": "生成 Access Token — 有效期1小时",
    "createRefreshToken": "生成 Refresh Token — 有效期30天",
    "isValid": "校验 Token 是否有效 — 签名正确且未过期",
    "parseClaims": "解析 Token Claims — 无效返回 null",
    "getUserId": "从 Token 提取用户 ID",
    "getRole": "从 Token 提取角色",
    "getPlatform": "从 Token 提取平台",
    "getTokenVersion": "从 Token 提取版本号",
    "getExpiration": "从 Token 提取过期时间",
    "isRefreshToken": "判断是否为 Refresh Token",
    "isAccessToken": "判断是否为 Access Token",
    "getAccessExpiration": "获取 Access Token 有效期（毫秒）",
    "encode": "BCrypt 加密 — 对明文密码进行哈希",
    "matches": "BCrypt 验证 — 比对明文与哈希是否匹配",
    "now": "当前时间 — yyyy-MM-dd HH:mm:ss 格式",
    "today": "今天日期 — yyyy-MM-dd 格式",
    "format": "格式化 LocalDateTime",
    "formatDate": "格式化 LocalDate",
    "filterChain": "安全过滤器链配置 — 定义公开路由、权限规则、过滤器注册",
    "passwordEncoder": "BCrypt 密码编码器 Bean",
    "getAsyncExecutor": "异步线程池 — core=5, max=20, queue=100",
    "getAsyncUncaughtExceptionHandler": "异步异常处理器 — 记录日志",
    "cacheManager": "Caffeine 缓存管理器 — 60秒过期，最大1000条",
    "addCorsMappings": "CORS 跨域映射 — 允许所有来源访问 /api/**",
    "mybatisPlusInterceptor": "MyBatis-Plus 拦截器 — 注册 MySQL 分页方言",
    "handleBusiness": "处理业务异常 — 按异常的 HTTP 状态码返回",
    "handleAccessDenied": "处理权限不足 — 返回 403",
    "handleValidation": "处理参数校验失败 — 聚合字段错误信息返回 400",
    "handleUnknown": "处理未知异常 — 返回 500 服务器内部错误",
    "getAuthorities": "获取 Spring Security 权限列表 — 格式 ROLE_DEVELOPER/ROLE_ADMIN/...",
    "isPrivileged": "判断是否为特权角色 — developer 或 admin",
    "canLogin": "判断是否允许登录 — hidden 角色禁止",
    "fromValue": "从字符串反查枚举 — 找不到返回 USER",
    "isDeveloper": "判断是否为 developer 角色",
    "isAdmin": "判断是否为管理员角色 — developer 也是管理员",
    "isFbUser": "判断是否为 FB 平台用户",
}

count = 0
for root, dirs, files in os.walk(SRC):
    for f in files:
        if not f.endswith('.java'): continue
        path = os.path.join(root, f)

        with open(path, 'r', encoding='utf-8') as fp:
            content = fp.read()

        cls_match = re.search(r'public (?:class|interface|enum) (\w+)', content)
        if not cls_match: continue
        cn = cls_match.group(1)

        # 1. Remove ALL existing /** ... */ comments
        content = re.sub(r'\s*/\*\*[^*]*\*/\s*\n', '\n', content)
        content = re.sub(r'\n{3,}', '\n\n', content)

        # 2. Add class comment (before the first @ or public)
        class_desc = CLASS_CMT.get(cn)
        if class_desc:
            jd = '/**\n * ' + class_desc + '\n */\n'
            # Find first annotation or public class
            idx = content.find('public class ' + cn)
            if idx < 0: idx = content.find('public interface ' + cn)
            if idx < 0: idx = content.find('public enum ' + cn)
            if idx >= 0:
                # Insert before the line that starts with @ or public
                lines_before = content[:idx].split('\n')
                insert_line = 0
                for i, line in enumerate(lines_before):
                    stripped = line.strip()
                    if stripped.startswith('@') or stripped.startswith('public '):
                        insert_line = i
                        break
                new_lines = lines_before[:insert_line] + [jd] + lines_before[insert_line:]
                content = '\n'.join(new_lines) + content[idx:]

        # 3. Add method comments
        lines = content.split('\n')
        new_lines = []
        for i, line in enumerate(lines):
            stripped = line.strip()
            m = re.search(r'public\s+\S+\s+(\w+)\s*\(', stripped)
            if m:
                method = m.group(1)
                if method in METHOD_CMT:
                    # Check if already has comment
                    has = False
                    for j in range(max(0,i-4), i):
                        if '*/' in lines[j] or lines[j].strip() == '*/':
                            has = True
                            break
                    if not has:
                        # Find where annotations start
                        insert_at = i
                        for j in range(i-1, max(0,i-6), -1):
                            if lines[j].strip().startswith('@'):
                                insert_at = j
                            else:
                                break
                        indent = ' ' * (len(line) - len(line.lstrip()))
                        new_lines.append(f'{indent}/** {METHOD_CMT[method]} */')
            new_lines.append(line)

        content = '\n'.join(new_lines)

        with open(path, 'w', encoding='utf-8') as fp:
            fp.write(content)
        count += 1

print(f'Updated {count} files')
PYEOF