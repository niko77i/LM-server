# 安全架构文档

## 认证流程

```
[请求] → JwtAuthenticationFilter → PlatformGuardFilter → SecurityConfig → [Controller]
         提取 Bearer Token          平台隔离检查           URL 权限规则       业务处理
         设置 SecurityContext
```

## 角色权限矩阵

| 角色 | GG 路由 | FB 路由 | 管理路由 | 说明 |
|------|---------|---------|---------|------|
| **developer** | ✅ | ✅ | ✅ | 最高权限，无视平台限制 |
| **admin** | ✅ | ❌ (限平台) | ✅ | 仅本平台 |
| **user** | ✅ | ❌ (限平台) | ❌ | 仅本平台 |
| **viewer** | 只读 | 只读 | ❌ | 仅本平台 |
| **hidden** | ❌ | ❌ | ❌ | 禁止登录 |

## 平台隔离

- **GG 专属路由**: `/api/ad-reports/**`, `/api/accounts/**`, `/api/mcc/**`, `/api/products/**`, `/api/scrape/**`, `/api/video/**`, `/api/youtube/**`, `/api/settings/**`, `/api/google-sheets/**`
- **FB 专属路由**: `/api/fb/**`
- **FB 用户访问 GG 路由**: 非 developer → 403
- **developer**: 无限制，可切换平台

## JWT Token

- 算法: HMAC-SHA256
- Access Token: 1 小时
- Refresh Token: 30 天
- 滑动过期: 剩余 < 30% 时自动续签 (`x-new-access-token` 头)
- tokenVersion: 改密/禁用时递增，强制旧 Token 失效

## 测试账户

| 用户名 | 密码 | 角色 | 平台 |
|--------|------|------|------|
| carl567 | 1976xiaobai | developer | gg |
