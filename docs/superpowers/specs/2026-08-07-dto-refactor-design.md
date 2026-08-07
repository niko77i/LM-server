# Map\<String, Object\> → DTO 重构设计文档

> **日期**: 2026-08-07  
> **目的**: 全项目消除 `Map<String, Object>` 返回类型，统一改用继承实体的 DTO 类 + Mapper XML 查询  
> **原则**: DTO 继承实体类 + 加 JOIN 字段；Mapper XML 一条 SQL 完成关联，零手动赋值

---

## 1. 背景

当前项目大量 Controller 返回 `ApiResponse<Map<String, Object>>` 或 `PagedResponse<Map<String, Object>>`，Service 层手动循环查数据库拼 Map。问题：

1. 无类型安全，扩展性差
2. 前端改字段名时无法用 IDE 重构
3. Service 代码充满循环+N+1查询

**目标**: 全项目消除 Map 返回类型，改用 DTO 继承实体 + Mapper XML 一条 SQL 完成。

---

## 2. 技术方案

### 2.1 DTO 继承实体

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountDto extends Accounts {
    @TableField(exist = false)  // 不是数据库列，仅用于接收 SQL 别名
    private String mccName;
    @TableField(exist = false)
    private String mccCode;
    @TableField(exist = false)
    private String agentName;
    @TableField(exist = false)
    private String statusName;
}
```

- 父类 `Accounts` 的 `@TableField` 注解被继承，`map-underscore-to-camel-case` 也生效
- 子类字段加 `@TableField(exist = false)` 告诉 MyBatis-Plus 这不是数据库列
- Jackson 序列化按字段名输出，配合 `SNAKE_CASE` 全局配置自动转下划线

### 2.2 Mapper XML 一条 SQL

```xml
<select id="selectAccountDtos" resultType="com.lmserver.dto.response.AccountDto">
    SELECT a.*,
           m.name AS mcc_name,
           m.mcc_id AS mcc_code,
           ag.name AS agent_name,
           s.name AS status_name
    FROM accounts a
    LEFT JOIN mcc m ON a.mcc_id = m.id
    LEFT JOIN agents ag ON a.agent_id = ag.id
    LEFT JOIN account_statuses s ON a.status_id = s.id
    WHERE a.owner_id = #{ownerId}
      AND a.deleted_at IS NULL
    ORDER BY a.created_at DESC
</select>
```

SQL 列别名映射到 DTO 字段：
- `m.name AS mcc_name` → DTO 字段 `mccName`
- `ag.name AS agent_name` → DTO 字段 `agentName`

MyBatis 自动做下划线→驼峰转换，列别名 `mcc_name` 映射到 `mccName`。

### 2.3 Service 简化

```java
// 之前: 手动构建 Map, N+1 查询
List<Map<String, Object>> items = new ArrayList<>();
for (Accounts a : page.getRecords()) {
    Mcc mcc = mccMapper.selectById(a.getMccId());  // N+1!
    Agent agent = agentsMapper.selectById(a.getAgentId());
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", a.getId());
    row.put("name", a.getName());
    row.put("mcc_name", mcc != null ? mcc.getName() : null);
    // ... 大量手动赋值
}

// 之后: 直接拿 DTO 列表
List<AccountDto> items = accountsMapper.selectAccountDtos(ownerId, page, size, search);
```

---

## 3. 需要新建的 DTO 类清单

所有 DTO 放在 `src/main/java/com/lmserver/dto/response/` 下，文件名 = 类名。

### 3.1 AccountDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 继承 | Accounts 全部字段 | id, name, accountId, mccId, agentId, statusId, timezone, acquiredDate, deathDate, statusChangedDate, createdAt, ownerId, deletedAt |
| 新增 | `String mccName` | MCC 名称 |
| 新增 | `String mccCode` | MCC 编码(mcc.mcc_id) |
| 新增 | `String agentName` | 代理名称 |
| 新增 | `String statusName` | 状态名称 |

### 3.2 ProductDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 继承 | Products 全部字段 | id, productName, kpi, region, status, customer, ownerId, salesPersonId, mccId, agencyRatio, runnerIds, isArchived, createdAt, deletedAt |
| 新增 | `String mccName` | MCC 名称 |
| 新增 | `String mccCode` | MCC 编码 |
| 新增 | `String salesPersonName` | 商务名称 |
| 新增 | `int assetCount` | 成效素材数 |
| 新增 | `int relatedAccountCount` | 关联账户数 |
| 新增 | `List<PackageDto> packages` | 包列表 |
| 新增 | `List<Long> runnerIdList` | 在跑人员 ID 列表 |

### 3.3 PackageDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 新建 | 全部字段 | id, productId, seriesName, packageName, url, status, createdAt |

### 3.4 MccDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 继承 | Mcc 全部字段 | id, name, mccId, parentMccId, levelId, ownerId, createdAt |
| 新增 | `String levelName` | 等级名称 |
| 新增 | `long directCount` | 直属账户数 |
| 新增 | `long totalAccountCount` | 子树账户总数 |
| 新增 | `boolean isOwner` | 是否为当前用户所有 |

### 3.5 FbAccountDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 继承 | FbAccounts 全部字段 | id, name, accountId, timezone, statusId, acquiredDate, statusChangedDate, ownerId, deletedAt, createdAt |
| 新增 | `String statusName` | 状态名称 |
| 新增 | `List<BmBriefDto> bms` | 关联的 BM 列表 |

### 3.6 FbBmDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 继承 | FbBms 全部字段 | id, name, bmId, note, status, ownerId, createdAt |
| 新增 | `String bmType` | 类型: normal/pixel_bm (unified 接口用) |
| 新增 | `int accountCount` | 关联账户数 |
| 新增 | `int pixelCount` | 关联像素数 |

### 3.7 FbProductDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 继承 | FbProducts 全部字段 | id, productName, kpi, region, status, salesPersonId, agencyRatio, ownerId, isArchived |
| 新增 | `String salesPersonName` | 商务名称 |
| 新增 | `List<BmBriefDto> bms` | 关联 BM |
| 新增 | `List<Long> runnerIds` | 在跑人员 |
| 新增 | `List<LineBriefDto> lines` | 广告线 |

### 3.8 FbPixelDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 新建 | 全部字段 | id, pixelName, pixelId, pixelBmId, createdAt |
| 新增 | `String bmName` | 所属 BM 名称 |

### 3.9 FontDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 新建 | `String id` | 字体 ID |
| 新建 | `String name` | 字体名称 |
| 新建 | `String source` | 来源: user / system |

### 3.10 DashboardDto

| 类型 | 字段 | 说明 |
|------|------|------|
| 新建 | `DashboardSummaryDto summary` | 汇总指标 |
| 新建 | `List<CampaignStatDto> campaigns` | 系列分组 |
| 新建 | `List<AnomalyDto> anomalies` | 异常检测 |
| 新建 | `Map<String, Double> periodCompare` | 环比 |

### 3.11 辅助 DTO

| DTO | 字段 | 用于 |
|-----|------|------|
| `DashboardSummaryDto` | totalCost, totalImpressions, totalClicks, totalInstalls, totalInApp, avgCpi, avgCtr, avgCvr | dashboard.summary |
| `CampaignStatDto` | campaign, totalCost, totalInstalls, totalImpressions, totalClicks, totalInApp, avgCpi, ctr, cvr | dashboard.campaigns |
| `AnomalyDto` | campaign, date, type(cost_spike/cpi_spike), detail | dashboard.anomalies |
| `TrendSeriesDto` | name, data(List of TrendPointDto) | trends |
| `TrendPointDto` | date, value | 内嵌在 TrendSeriesDto |
| `CompareItemDto` | name, totalCost, totalImpressions, totalClicks, totalInstalls, totalInApp, cpi, ctr, cvr | compare |
| `CrossUserDto` | userId, displayName, username, totalCost, totalInstalls, totalInApp, avgCpi, reportDays | cross-user |
| `ScatterPointDto` | name, x, y, size, totalCost | multi-analysis.points |
| `BmBriefDto` | id, name, bmId | 内嵌在 FbAccountDto 等 |
| `LineBriefDto` | id, lineName, link, pixelId | 内嵌在 FbProductDto |
| `UserBriefDto` | id, username, displayName, platform, role | auth/names, fb/users |

---

## 4. 需要新建的 Mapper XML

XML 文件放在对应 Mapper 同目录下。如 `AccountsMapper.xml` 放在 `mapper/gg/` 同目录。

### 4.1 AccountsMapper.xml — `selectAccountDtos`

```sql
SELECT a.*,
       m.name AS mcc_name,
       m.mcc_id AS mcc_code,
       ag.name AS agent_name,
       s.name AS status_name
FROM accounts a
LEFT JOIN mcc m ON a.mcc_id = m.id
LEFT JOIN agents ag ON a.agent_id = ag.id
LEFT JOIN account_statuses s ON a.status_id = s.id
WHERE a.owner_id = #{ownerId}
  AND a.deleted_at IS NULL
ORDER BY a.created_at DESC
```

支持参数: `ownerId`, `search`(name/accountId LIKE), `statusId`, `mccId`, `agentId`

### 4.2 ProductsMapper.xml — `selectProductDtos`

```sql
SELECT p.*,
       m.name AS mcc_name,
       m.mcc_id AS mcc_code,
       sp.name AS sales_person_name,
       (SELECT COUNT(*) FROM product_assets pa WHERE pa.product_id = p.id) AS asset_count,
       (SELECT COUNT(*) FROM accounts ac WHERE ac.mcc_id = p.mcc_id AND ac.deleted_at IS NULL) AS related_account_count
FROM products p
LEFT JOIN mcc m ON p.mcc_id = m.id
LEFT JOIN sales_persons sp ON p.sales_person_id = sp.id
WHERE p.owner_id = #{ownerId}
  AND p.deleted_at IS NULL
ORDER BY p.created_at DESC
```

### 4.3 MccMapper.xml — `selectMccDtos`

```sql
SELECT m.*,
       ml.name AS level_name,
       (SELECT COUNT(*) FROM accounts a WHERE a.mcc_id = m.id AND a.deleted_at IS NULL) AS direct_count
FROM mcc m
LEFT JOIN mcc_levels ml ON m.level_id = ml.id
WHERE m.owner_id = #{ownerId}
ORDER BY m.created_at DESC
```

### 4.4 FbAccountsMapper.xml — `selectFbAccountDtos`

```sql
SELECT a.*,
       s.name AS status_name
FROM fb_accounts a
LEFT JOIN account_statuses s ON a.status_id = s.id
WHERE a.owner_id = #{ownerId}
  AND a.deleted_at IS NULL
ORDER BY a.created_at DESC
```

### 4.5 FbBmsMapper.xml — `selectFbBmDtos`

```sql
SELECT b.*,
       'normal' AS bm_type,
       (SELECT COUNT(*) FROM fb_account_bm ab WHERE ab.bm_id = b.id) AS account_count,
       0 AS pixel_count
FROM fb_bms b
WHERE b.owner_id = #{ownerId}
  AND b.deleted_at IS NULL
UNION ALL
SELECT pb.*,
       'pixel_bm' AS bm_type,
       0 AS account_count,
       (SELECT COUNT(*) FROM fb_pixels p WHERE p.pixel_bm_id = pb.id) AS pixel_count
FROM fb_pixel_bms pb
WHERE pb.owner_id = #{ownerId}
  AND pb.deleted_at IS NULL
```

### 4.6 FbPixelsMapper.xml — `selectFbPixelDtos`

```sql
SELECT p.*,
       pb.name AS bm_name
FROM fb_pixels p
LEFT JOIN fb_pixel_bms pb ON p.pixel_bm_id = pb.id
```

---

## 5. 需要改造的 Service/Controller

按返回类型分类：

### 5.1 PagedResponse<Map> → PagedResponse<XxxDto>

| 接口 | 改为 | Mapper 方法 |
|------|------|------------|
| `AccountService.list()` | `PagedResponse<AccountDto>` | `AccountsMapper.selectAccountDtos` |
| `ProductService.list()` | `PagedResponse<ProductDto>` | `ProductsMapper.selectProductDtos` |
| `MccService.list()` | `PagedResponse<MccDto>` | `MccMapper.selectMccDtos` |
| `FbService.listAccounts()` | `PagedResponse<FbAccountDto>` | `FbAccountsMapper.selectFbAccountDtos` |
| `FbService.listBms()` / `unified` | `PagedResponse<FbBmDto>` | `FbBmsMapper.selectFbBmDtos` |
| `FbPixelController.listPixels()` | `PagedResponse<FbPixelDto>` | `FbPixelsMapper.selectFbPixelDtos` |

### 5.2 ApiResponse<Map> → ApiResponse<XxxDto>

| 接口 | 改为 |
|------|------|
| `AccountController.detail()` | `ApiResponse<AccountDto>` |
| `ProductController.detail()` | `ApiResponse<ProductDto>` (含 packages/runners/accounts 等) |
| `MccController.detail()` | `ApiResponse<MccDto>` |
| `/ad-reports/dashboard` | `ApiResponse<DashboardDto>` |
| `/ad-reports/trends` | `ApiResponse<List<TrendSeriesDto>>` |
| `/ad-reports/compare` | `ApiResponse<List<CompareItemDto>>` |
| `/ad-reports/cross-user` | `ApiResponse<List<CrossUserDto>>` |
| `/ad-reports/multi-analysis` | `ApiResponse<MultiAnalysisDto>` |
| `/fonts/list` | `ApiResponse<List<FontDto>>` |
| `/auth/names` | `ApiResponse<List<UserBriefDto>>` |
| `/fb/users` | `ApiResponse<List<UserBriefDto>>` |
| `/fb/products/list` | `PagedResponse<FbProductDto>` |

### 5.3 ServiceImpl 改造步骤

以 `AccountServiceImpl.list()` 为例：

```java
// 之前 (~40 行手动构建 Map)
List<Map<String, Object>> items = new ArrayList<>();
for (Accounts a : pg.getRecords()) { /* 手动查 mcc/agent/status + put */ }

// 之后 (3 行)
@Override
public PagedResponse<AccountDto> list(Long ownerId, int page, int size, String search,
        Long statusId, Long mccId, Long agentId) {
    Page<AccountDto> pg = new Page<>(page, size);
    List<AccountDto> items = accountsMapper.selectAccountDtos(pg, ownerId, search, statusId, mccId, agentId);
    return PagedResponse.of(items, pg.getTotal(), page, size);
}
```

---

## 6. 改造顺序

按依赖关系从底向上：

1. **新建 DTO 类** (13 个 DTO + 10 个辅助 DTO)
2. **新建 Mapper XML** (6 个 XML)
3. **Mapper 接口加方法** (6 个 Mapper 接口)
4. **Service 接口改签名** (5 个 Service 接口)
5. **ServiceImpl 改实现** (5 个 Impl)
6. **Controller 改返回类型** (对应 Controller)
7. **删除旧的手动 Map 构建代码**
8. **编译验证**

---

## 7. 注意事项

1. `@TableField(exist = false)` 必须加在 DTO 新增字段上，否则 MyBatis-Plus 的 `insert`/`update` 会报错
2. Jackson `SNAKE_CASE` 已全局配置，DTO 字段 `mccName` 自动输出为 `mcc_name`
3. `PagedResponse` 和 `ApiResponse` 已经对齐 Python 格式，不需要改动
4. 分析类 DTO（DashboardDto 等）不需要继承实体，是纯 POJO
5. `ProductDto` 的 `packages`/`runnerIdList` 字段需要额外查询填充（不能一条 SQL 搞定集合字段），在 ServiceImpl 中批量查询后 set
