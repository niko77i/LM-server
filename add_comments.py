import os, re

SRC = r"D:\server\cc\LM-Server\src\main\java\com\lmserver"

METHOD_DESC = {
    'login': '用户登录 — 验证用户名密码，返回 JWT Token',
    'register': '用户注册 — 创建新账户',
    'refresh': '刷新 Token — 用 Refresh Token 换取新 Access Token',
    'me': '获取当前用户信息',
    'health': '健康检查 — 确认服务运行状态',
    'list': '分页列表查询',
    'detail': '获取单条记录详情',
    'getById': '按 ID 查询',
    'create': '新增记录',
    'update': '更新记录',
    'delete': '删除记录',
    'options': '获取下拉选项列表',
    'batchDelete': '批量删除',
    'batchUpdate': '批量更新',
    'save': '保存配置',
    'get': '获取配置',
    'getCache': '获取缓存',
    'clearCache': '清除缓存',
    'getAllConfig': '获取所有配置项',
    'saveConfig': '批量保存配置',
    'getTags': '获取标签列表',
    'saveTag': '保存标签',
    'getByProduct': '按产品查询',
    'listUsers': '获取用户列表',
    'updateUser': '修改用户信息',
    'deleteUser': '删除用户（软删除）',
    'listBms': 'BM 列表查询',
    'getBmById': '按 ID 查询 BM',
    'createBm': '创建 BM',
    'updateBm': '更新 BM',
    'deleteBm': '删除 BM（软删除）',
    'bmOptions': 'BM 下拉选项',
    'listAccounts': '账户列表查询',
    'getAccountById': '按 ID 查询账户',
    'createAccount': '创建账户',
    'updateAccount': '更新账户',
    'deleteAccount': '删除账户（软删除）',
    'listProducts': '产品列表查询',
    'getProductById': '按 ID 查询产品',
    'createProduct': '创建产品',
    'updateProduct': '更新产品',
    'deleteProduct': '删除产品',
    'productOptions': '产品下拉选项',
}

def class_desc(rel, cn):
    p = rel.replace('\\', '/')
    if 'Config' in cn:
        if 'Async' in cn: return '异步线程池配置 — 管理 @Async 注解的执行器，限制并发 FFmpeg 等重任务'
        if 'Cache' in cn: return '本地缓存配置 — 基于 Caffeine，默认 60 秒过期'
        if 'Jwt' in cn: return 'JWT 配置属性 — 绑定 application.yml 中 jwt.* 配置项'
        if 'MybatisPlus' in cn: return 'MyBatis-Plus 配置 — 注册分页插件，MySQL 分页方言'
        if 'Security' in cn: return 'Spring Security 安全配置 — JWT 无状态认证 + 权限规则 + CORS 跨域'
        if 'Web' in cn: return 'Web MVC 配置 — CORS 跨域设置，暴露 x-new-access-token 响应头'
        return 'Spring 配置类'
    if 'security' in p:
        if 'JwtAuthenticationFilter' in cn: return 'JWT 认证过滤器 — 提取 Authorization 头中的 Bearer Token，校验后设置 SecurityContext，支持滑动过期续签'
        if 'JwtTokenProvider' in cn: return 'JWT Token 提供者 — 桥接 JwtUtil 与 Spring Security，生成/校验 Access Token 和 Refresh Token，启动时拒绝弱密钥'
        if 'PlatformGuardFilter' in cn: return '平台守卫过滤器 — 使用 AntPathMatcher 匹配 GG 专属路由，阻止 FB 平台非 developer 用户访问'
        if 'UserPrincipal' in cn: return 'Spring Security 用户主体 — 从 JWT 的 Claims 中提取 userId/role/platform/tokenVersion'
        return '安全组件'
    if 'exception' in p:
        if 'GlobalExceptionHandler' in cn: return '全局异常处理器 — @RestControllerAdvice，将 BusinessException/校验异常/未知异常统一转换为 ApiResponse.fail()'
        if 'BusinessException' in cn: return '业务异常基类 — 携带 HTTP 状态码和可选错误码（前端国际化用），由 GlobalExceptionHandler 统一处理'
        if 'UnauthorizedException' in cn: return '401 未认证异常 — Token 无效/过期/缺失时抛出'
        if 'PlatformForbiddenException' in cn: return '403 平台禁止异常 — FB 用户越权访问 GG 路由时抛出'
        return '异常类'
    if 'enums' in p:
        return '用户角色枚举 — developer(最高) > admin > viewer > user > hidden(禁止登录)，提供 getAuthorities() 和从字符串反查'
    if 'util' in p:
        if 'JwtUtil' in cn: return 'JWT 核心工具 — Token 生成/解析/校验/字段提取，纯函数无 Spring 依赖，使用 jjwt 0.12.x HMAC-SHA256'
        if 'PasswordUtil' in cn: return 'BCrypt 密码工具 — 封装 Spring Security BCryptPasswordEncoder，替代 Python werkzeug pbkdf2'
        if 'DateUtil' in cn: return '日期时间工具 — 提供 yyyy-MM-dd HH:mm:ss 和 yyyy-MM-dd 格式化'
        return '工具类'
    if 'LmServerApplication' in cn: return 'Spring Boot 启动类 — @SpringBootApplication 入口，排除 DataSource 自动配置（Phase 1 无需数据库）'
    if 'dto/response/ApiResponse' in p: return '统一 API 响应格式 — {success: bool, data: T, error: string}，与 Python helpers.ok() 完全兼容'
    if 'dto/response/PagedResponse' in p: return '分页列表响应 — {items: [...], total, page, size}，注意字段名是 items 不是 data，匹配前端 res.data.items'
    if 'dto/response/LoginResponse' in p: return '登录响应 — 包含 accessToken（1小时）、refreshToken（30天）和用户信息（id/username/role/platform/displayName）'
    if 'dto/request/LoginRequest' in p: return '登录请求 — 含 @NotBlank 校验的 username 和 password'
    if 'dto/request/RegisterRequest' in p: return '注册请求 — username（4-20字符）、password（最少6字符）、displayName'
    if 'controller/auth' in p: return '认证控制器 — /api/auth/*'
    if 'controller/OptionController' in p: return '选项管理控制器 — 统一 {type} 路径变量处理 agents/statuses/mcc-levels/sales-persons/regions 五个选项表的 CRUD'
    if 'controller/AuditController' in p: return '审计日志控制器 — /api/audit-log，按操作类型筛选'
    if 'controller/ConfigController' in p: return '系统配置控制器 — /api/config，键值对配置的增改查'
    if 'controller/HealthController' in p: return '健康检查控制器 — /api/health'
    if 'controller/gg' in p:
        m = re.search(r'(\w+)Controller', cn)
        name = m.group(1).lower() if m else ''
        descs = {
            'product': 'GG 产品管理', 'account': 'GG 账户管理（支持软删除）',
            'mcc': 'GG MCC 管理', 'recharge': 'GG 充值管理',
            'adreport': 'GG 广告报告管理', 'copywriting': 'GG 文案管理',
            'delist': 'GG 掉包检测', 'scrape': 'GG 图片抓取',
            'settings': 'GG 系统设置'
        }
        dn = descs.get(name, name)
        return f'{dn}控制器 — /api/{name.replace("adreport","ad-reports").replace("copywriting","copywriting")}*'
    if 'controller/fb' in p:
        m = re.search(r'Fb(\w+)Controller', cn)
        name = m.group(1).lower() if m else ''
        descs = {
            'bm': 'FB BM 管理（支持软删除）',
            'account': 'FB 账户管理（支持软删除）',
            'product': 'FB 产品管理',
            'adreport': 'FB 广告报告管理'
        }
        dn = descs.get(name, name)
        return f'{dn}控制器 — /api/fb/{name.replace("adreport","reports")}*'
    if 'controller/admin' in p: return '管理员控制器 — /api/admin/*，@PreAuthorize 控制权限：用户管理需 ADMIN，删除需 DEVELOPER'
    if 'service/impl' in p:
        if 'Auth' in cn: return '认证服务实现 — 基于 MyBatis-Plus 从 MySQL users 表查询，BCrypt 密码验证，登录后更新 last_login'
        if 'Option' in cn: return '选项服务实现 — 使用 Java 21 switch 表达式统一分发 5 个选项表的 CRUD 操作'
        if 'Mcc' in cn: return 'MCC 服务实现 — 多条件动态查询（名称/MCC ID 模糊搜索 + 等级筛选），MyBatis-Plus 分页'
        if 'Product' in cn: return '产品服务实现 — 支持按产品名/地区/状态筛选，分页查询'
        if 'Account' in cn: return '账户服务实现 — 支持按名称/账户 ID/状态/MCC/代理多条件筛选，软删除逻辑'
        if 'Recharge' in cn: return '充值服务实现 — 按账户 ID 筛选充值记录'
        if 'Fb' in cn: return 'FB 平台服务实现 — 统一管理 FB BM/账户/产品三个子模块的 CRUD，均支持软删除'
        if 'Copywriting' in cn: return '文案服务实现 — 按地区和归属用户筛选'
        return '业务服务实现'
    if 'service' in p and 'impl' not in p:
        name = cn.replace('Service', '')
        return f'{name} 业务服务接口'
    if 'mapper' in p:
        t = cn.replace('Mapper', '')
        return f'{t} 表数据访问层 — MyBatis-Plus BaseMapper，提供 selectById/insert/updateById/deleteById'
    if 'entity' in p:
        return f'实体类 — 映射数据库表: {cn.lower()}，使用 MyBatis-Plus @TableName/@TableId/@TableField'
    return ''

count = 0
for root, dirs, files in os.walk(SRC):
    for f in files:
        if not f.endswith('.java'): continue
        path = os.path.join(root, f)
        if 'AuthController.java' in path: continue  # already hand-edited

        with open(path, 'r', encoding='utf-8') as fp:
            content = fp.read()

        cls_match = re.search(r'public (?:class|interface|enum) (\w+)', content)
        if not cls_match: continue
        cn = cls_match.group(1)
        rel = os.path.relpath(path, SRC)

        # Replace old javadoc with new Chinese one
        desc = class_desc(rel, cn)
        if not desc: continue

        new_jd = '/**\n * ' + desc + '\n */\n'
        old_jd = re.search(r'/\*\*[^*]*\*/\s*\n', content)
        if old_jd:
            content = content.replace(old_jd.group(0), new_jd)
        else:
            idx = content.find('public class ' + cn)
            if idx < 0: idx = content.find('public interface ' + cn)
            if idx < 0: idx = content.find('public enum ' + cn)
            if idx >= 0:
                content = content[:idx] + new_jd + content[idx:]

        # Add method comments
        lines = content.split('\n')
        new_lines = []
        for i, line in enumerate(lines):
            stripped = line.strip()
            m = re.search(r'public\s+\S+\s+(\w+)\s*\(', stripped)
            if m and m.group(1) in METHOD_DESC:
                method = m.group(1)
                # Check no existing comment above
                has_comment = False
                for j in range(max(0, i-3), i):
                    if '/**' in lines[j] or '*/' in lines[j] or lines[j].strip().startswith('*'):
                        has_comment = True
                        break
                if not has_comment:
                    # Find where to insert (before annotations or before method)
                    insert_at = i
                    for j in range(i-1, max(0, i-6), -1):
                        if lines[j].strip().startswith('@'):
                            insert_at = j
                        else:
                            break
                    indent = ' ' * (len(line) - len(line.lstrip()))
                    new_lines.append(f'{indent}/** {METHOD_DESC[method]} */')
            new_lines.append(line)

        content = '\n'.join(new_lines)

        with open(path, 'w', encoding='utf-8') as fp:
            fp.write(content)
        count += 1

print(f'Updated {count} files')
PYEOF