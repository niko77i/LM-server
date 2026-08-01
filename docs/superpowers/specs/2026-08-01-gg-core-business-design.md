# Phase 4: GG 核心业务设计

> **日期**: 2026-08-01  
> **目标**: 实现 GG 平台核心 Controller + Service  
> **覆盖**: 产品管理 → 账户管理 → MCC → 充值 → 选项管理

---

## 实现范围

### 第一批: 选项管理（最简单，无依赖）
| Controller | 接口 | 说明 |
|------|------|------|
| `OptionController` | CRUD × 5 | agents/statuses/mcc-levels/sales-persons/regions 各 4 个 |

### 第二批: MCC 管理
| Controller | 接口 | 说明 |
|------|------|------|
| `MccController` | 8 | CRUD + 列表 + 关联 + 选项 |

### 第三批: 产品管理
| Controller | 接口 | 说明 |
|------|------|------|
| `ProductController` | 17 | CRUD + 包管理 + 在跑人员 + 掉包检测 |

### 第四批: 账户管理
| Controller | 接口 | 说明 |
|------|------|------|
| `AccountController` | 18 | CRUD + 批量操作 + Sheet同步 + MCC历史 |

### 第五批: 充值管理
| Controller | 接口 | 说明 |
|------|------|------|
| `RechargeController` | 5 | 单个/批量充值 + Sheet写入 |

## 分层模式

```
Controller (@RestController)
  → Service (业务逻辑)
    → Repository (JPA 查询)
```

响应格式: `ApiResponse<T>` / `PagedResponse<T>`，完全兼容前端。

## 关键点

- **数据隔离**: 所有查询按 `owner_id` 过滤
- **平台隔离**: GG 路由由 PlatformGuardFilter 保护
- **分页**: `PagedResponse.of(items, total, page, size)`
- **审计**: CUD 操作写 audit_log

先实现第一批选项管理验证模式，然后批量推进。确认后开始？
