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

### 1.1 现状统计（代码审查结果）

| 类别 | 数量 |
|------|------|
| 返回 `ApiResponse<Map<String, Object>>` 的端点 | 27 |
| 返回 `PagedResponse<Map<String, Object>>` 的端点 | 6 |
| 返回 `ApiResponse<List<Map<String, Object>>>` 的端点 | 5 |
| Service 中手动构建 Map 的方法 | ~15 |
| Controller 中手动构建 Map 的方法 | ~20 |
| 现有 Mapper XML 文件 | 0（全注解驱动） |
| 现有 DTO 类 | 9（仅基础响应包装类） |

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

### 3.1 AccountDto — 继承 Accounts

Accounts 实体字段（继承获得）: `id`, `name`, `accountId`, `mccId`, `timezone`, `acquiredDate`, `deathDate`, `createdAt`, `updatedAt`, `ownerId`, `statusChangedDate`, `agentId`, `statusId`, `deletedAt`

| 类型 | 字段 | 说明 |
|------|------|------|
| 新增 | `String mccName` | MCC 名称 (JOIN mcc.name) |
| 新增 | `String mccCode` | MCC 编码 (JOIN mcc.mcc_id) |
| 新增 | `String agentName` | 代理名称 (JOIN agents.name) |
| 新增 | `String statusName` | 状态名称 (JOIN account_statuses.name) |

### 3.2 ProductDto — 继承 Products

Products 实体字段（继承获得）: `id`, `productName`, `kpi`, `region`, `status`, `mccId`, `createdAt`, `ownerId`, `runnerIds`, `isArchived`, `customer`, `deletedAt`, `agencyRatio`, `salesPersonId`, `salesPerson`

| 类型 | 字段 | 说明 |
|------|------|------|
| 新增 | `String mccName` | MCC 名称 |
| 新增 | `String mccCode` | MCC 编码 (mcc.mcc_id) |
| 新增 | `String salesPersonName` | 商务名称 (JOIN sales_persons.name) |
| 新增 | `int assetCount` | 成效素材数 (子查询) |
| 新增 | `int relatedAccountCount` | 关联账户数 (子查询) |
| 新增 | `List<PackageDto> packages` | 包列表（额外查询填充） |
| 新增 | `List<Long> runnerIdList` | 在跑人员 ID 列表（额外查询填充） |

### 3.3 ProductDetailDto — 继承 ProductDto（用于 /detail 端点）

ProductDto 全部字段 + 以下额外字段：

| 类型 | 字段 | 说明 |
|------|------|------|
| 新增 | `List<Map<String, Object>> relatedAccounts` | 关联账户简单列表 |
| 新增 | `Map<String, Long> statusCount` | 账户状态分布 |
| 新增 | `List<ProductRunners> runners` | 在跑人员详情 |

> **注意**: `relatedAccounts`/`statusCount`/`runners` 需要额外查询，详见 Service 改造。

### 3.4 PackageDto — 独立 DTO

Packages 实体字段: `id`, `productId`, `seriesName`, `packageName`, `url`, `status`, `createdAt`

| 类型 | 字段 | 说明 |
|------|------|------|
| 新建 | 全部实体字段 | 直接从 packages 表查询 |

### 3.5 MccDto — 继承 Mcc

Mcc 实体字段（继承获得）: `id`, `name`, `mccId`, `parentMccId`, `createdAt`, `updatedAt`, `ownerId`, `sharedUserIds`, `levelId`

| 类型 | 字段 | 说明 |
|------|------|------|
| 新增 | `String levelName` | 等级名称 (JOIN mcc_levels.name) |
| 新增 | `long directCount` | 直属账户数 (子查询) |
| 新增 | `long totalAccountCount` | 子树账户总数（Java 层递归计算） |
| 新增 | `boolean isOwner` | 是否为当前用户所有 |

> **注意**: `totalAccountCount` 需要 Java 层递归计算，SQL 无法一条完成。MCC 树结构通过 `parent_mcc_id` 关联。

### 3.6 FbAccountDto — 继承 FbAccounts

FbAccounts 实体字段（继承获得）: `id`, `name`, `accountId`, `timezone`, `statusId`, `acquiredDate`, `statusChangedDate`, `ownerId`, `deletedAt`, `createdAt`, `updatedAt`

| 类型 | 字段 | 说明 |
|------|------|------|
| 新增 | `String statusName` | 状态名称 (JOIN account_statuses.name) |
| 新增 | `List<BmBriefDto> bms` | 关联的 BM 列表（额外查询填充，通过 fb_account_bm 中间表） |

> **注意**: `bms` 字段无法一条 SQL 完成，需要通过 `fb_account_bm` 中间表查询。

### 3.7 FbBmDto — 继承 FbBms

FbBms 实体字段（继承获得）: `id`, `name`, `bmId`, `note`, `status`, `ownerId`, `deletedAt`, `createdAt`, `updatedAt`

| 类型 | 字段 | 说明 |
|------|------|------|
| 新增 | `String bmType` | 类型: normal / pixel_bm (unified 接口用) |
| 新增 | `int accountCount` | 关联账户数 |
| 新增 | `int pixelCount` | 关联像素数 |

### 3.8 FbPixelBmDto — 继承 FbPixelBms（Pixel BM 专用）

FbPixelBms 实体字段（继承获得）: `id`, `name`, `bmId`, `note`, `status`, `ownerId`, `deletedAt`, `createdAt`, `updatedAt`

| 类型 | 字段 | 说明 |
|------|------|------|
| 新增 | `int pixelCount` | 关联像素数 |

> **说明**: 设计文档原计划用 FbBmDto 通过 UNION 合并 normal/pixel_bm 两种 BM。但 FbPixelBms 表结构不同（没有 `created_at`/`updated_at`? 实际上有），建议为 Pixel BM 单独建 DTO，unified 端点合并两种结果。

### 3.9 FbProductDto — 继承 FbProducts

FbProducts 实体字段（继承获得）: `id`, `productName`, `kpi`, `region`, `status`, `salesPersonId`, `agencyRatio`, `ownerId`, `isArchived`, `createdAt`, `updatedAt`

| 类型 | 字段 | 说明 |
|------|------|------|
| 新增 | `String salesPersonName` | 商务名称 (JOIN sales_persons.name) |
| 新增 | `List<BmBriefDto> bms` | 关联 BM（额外查询） |
| 新增 | `List<Long> runnerIds` | 在跑人员 ID（额外查询） |
| 新增 | `List<LineBriefDto> lines` | 广告线（额外查询） |

### 3.10 FbPixelDto — 独立 DTO

FbPixels 实体字段: `id`, `pixelBmId`, `pixelName`, `pixelId`, `createdAt`

| 类型 | 字段 | 说明 |
|------|------|------|
| 继承 | 全部实体字段 | 从 fb_pixels 查询 |
| 新增 | `String bmName` | 所属 BM 名称 (JOIN fb_pixel_bms.name) |
| 新增 | `String bmBmId` | 所属 BM 的 bm_id |

### 3.11 FontDto — 独立 DTO

| 类型 | 字段 | 说明 |
|------|------|------|
| 新建 | `String id` | 字体 ID |
| 新建 | `String name` | 字体名称 |
| 新建 | `String source` | 来源: user / system |

### 3.12 广告报告分析 DTO 组

#### DashboardDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `summary` | `DashboardSummaryDto` | 汇总指标 |
| `periodCompare` | `Map<String, Double>` | 环比（key: cost_change_pct/installs_change_pct/cpi_change_pct） |
| `anomalies` | `List<AnomalyDto>` | 异常检测 |
| `campaigns` | `List<CampaignStatDto>` | 系列分组 |
| `assetCount` | `long` | 素材关联数 |

#### DashboardSummaryDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalCost` | `double` | 总花费 |
| `totalImpressions` | `long` | 总展示 |
| `totalClicks` | `long` | 总点击 |
| `totalInstalls` | `long` | 总安装 |
| `totalInApp` | `double` | 总应用内操作 |
| `avgCpi` | `double` | 平均 CPI |
| `avgCtr` | `double` | 平均 CTR |
| `avgCvr` | `double` | 平均 CVR |

#### CampaignStatDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `campaign` | `String` | 系列名 |
| `totalCost` | `double` | 总花费 |
| `totalInstalls` | `double` | 总安装 |
| `totalImpressions` | `long` | 总展示 |
| `totalClicks` | `long` | 总点击 |
| `totalInApp` | `double` | 总应用内操作 |
| `avgCpi` | `double` | 平均 CPI |
| `ctr` | `double` | 点击率 |
| `cvr` | `double` | 转化率 |

#### AnomalyDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `campaign` | `String` | 系列名 |
| `date` | `Object` | 日期 |
| `type` | `String` | 类型: cost_spike / cpi_spike |
| `detail` | `String` | 详情描述 |

#### TrendDto（趋势返回）

| 字段 | 类型 | 说明 |
|------|------|------|
| `series` | `List<TrendSeriesDto>` | 多系列数据 |

#### TrendSeriesDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 系列名 |
| `data` | `List<TrendPointDto>` | 数据点 |

#### TrendPointDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `date` | `Object` | 日期 |
| `value` | `double` | 值 |

#### CompareItemDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 分组名 |
| `totalCost` | `double` | 总花费 |
| `totalImpressions` | `long` | 总展示 |
| `totalClicks` | `long` | 总点击 |
| `totalInstalls` | `long` | 总安装 |
| `totalInApp` | `double` | 总应用内操作 |
| `cpi` | `double` | CPI |
| `ctr` | `double` | CTR |
| `cvr` | `double` | CVR |

#### CrossUserResultDto（跨用户对比返回）

| 字段 | 类型 | 说明 |
|------|------|------|
| `users` | `List<CrossUserDto>` | 用户对比列表 |

#### CrossUserDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `userId` | `Long` | 用户 ID |
| `displayName` | `String` | 显示名称 |
| `username` | `String` | 用户名 |
| `totalCost` | `double` | 总花费 |
| `totalInstalls` | `long` | 总安装 |
| `totalInApp` | `double` | 总应用内操作 |
| `avgCpi` | `double` | 平均 CPI |
| `reportDays` | `long` | 报告天数 |

#### MultiAnalysisDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `points` | `List<ScatterPointDto>` | 散点数据 |
| `xAxis` | `String` | X 轴指标 |
| `yAxis` | `String` | Y 轴指标 |
| `groupBy` | `String` | 分组维度 |
| `xAvg` | `double` | X 均值 |
| `yAvg` | `double` | Y 均值 |
| `pearsonR` | `double` | Pearson 相关系数 |
| `insight` | `String` | 分析洞察文本 |

#### ScatterPointDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 点名称 |
| `x` | `double` | X 值 |
| `y` | `double` | Y 值 |
| `size` | `double` | 气泡大小（可选） |
| `totalCost` | `double` | 总花费 |

#### AdStatsDto（/stats 端点）

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalCost` | `double` | 总花费 |
| `totalImpressions` | `long` | 总展示 |
| `totalClicks` | `long` | 总点击 |
| `totalInstalls` | `long` | 总安装 |
| `totalInApp` | `double` | 总应用内操作 |
| `recordCount` | `long` | 记录数 |

#### AdDatesDto（/dates 端点）

| 字段 | 类型 | 说明 |
|------|------|------|
| `dates` | `Map<String, Object>` | 日期→计数映射 |

#### AnalyzeResultDto（/analyze 端点）

| 字段 | 类型 | 说明 |
|------|------|------|
| `question` | `String` | 用户问题 |
| `dataContext` | `Map<String, Object>` | 数据上下文 |
| `suggestion` | `String` | 提示文本 |

#### MultiAiChatResultDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `question` | `String` | 用户问题 |
| `dataContext` | `Map<String, Object>` | 数据上下文（含 history） |
| `suggestion` | `String` | 提示文本 |

### 3.13 FB 广告报告 DTO

#### FbAdStatsDto

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalCost` | `double` | 总花费 |
| `totalImpressions` | `long` | 总展示 |
| `totalClicks` | `long` | 总点击 |
| `totalRegistrations` | `long` | 总注册 |
| `totalPurchases` | `long` | 总购买 |
| `avgCpa` | `double` | 平均 CPA |
| `recordCount` | `long` | 记录数 |

### 3.14 辅助 DTO（内嵌用）

| DTO | 字段 | 用于 |
|-----|------|------|
| `BmBriefDto` | `id`, `name`, `bmId` | 内嵌在 FbAccountDto / FbProductDto |
| `LineBriefDto` | `id`, `lineName`, `link`, `pixelId` | 内嵌在 FbProductDto |
| `UserBriefDto` | `id`, `username`, `displayName`, `platform`, `role` | auth/names, fb/users |

### 3.15 操作结果 DTO（可选，简单的可用 Map）

以下端点返回简单的操作结果，可保留 `Map` 或使用专用 DTO：

| DTO | 字段 | 用于 |
|-----|------|------|
| `BatchOperationResultDto` | `created`/`deleted`/`skipped` (int), `skipped` (List) | 批量操作 |
| `MergeResultDto` | `mergedPackages`, `mergedProducts` (int) | merge 端点 |
| `ImportResultDto` | `imported`, `skipped` (int) | import 端点 |
| `SheetsSyncResultDto` | `updated`, `inserted` (int) | Google Sheets 同步 |
| `BanAndMigrateResultDto` | `migrated` (int), `targetBmId` (Long) | BM 封禁迁移 |

---

## 4. 需要新建的 Mapper XML

XML 文件放在对应 Mapper 同目录下（`src/main/resources/mapper/gg/`、`mapper/fb/`、`mapper/common/`）。

### 4.1 AccountsMapper.xml — `selectAccountDtos`

**路径**: `src/main/resources/mapper/gg/AccountsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.gg.AccountsMapper">

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
        <where>
            a.owner_id = #{ownerId}
            AND a.deleted_at IS NULL
            <if test="search != null and search != ''">
                AND (a.name LIKE CONCAT('%', #{search}, '%') OR a.account_id LIKE CONCAT('%', #{search}, '%'))
            </if>
            <if test="statusId != null">
                AND a.status_id = #{statusId}
            </if>
            <if test="mccId != null">
                AND a.mcc_id = #{mccId}
            </if>
            <if test="agentId != null">
                AND a.agent_id = #{agentId}
            </if>
        </where>
        ORDER BY a.created_at DESC
    </select>

    <select id="selectAccountDtoById" resultType="com.lmserver.dto.response.AccountDto">
        SELECT a.*,
               m.name AS mcc_name,
               m.mcc_id AS mcc_code,
               ag.name AS agent_name,
               s.name AS status_name
        FROM accounts a
        LEFT JOIN mcc m ON a.mcc_id = m.id
        LEFT JOIN agents ag ON a.agent_id = ag.id
        LEFT JOIN account_statuses s ON a.status_id = s.id
        WHERE a.id = #{id}
          AND a.deleted_at IS NULL
    </select>

</mapper>
```

支持参数: `ownerId`, `search`(name/accountId LIKE), `statusId`, `mccId`, `agentId`

### 4.2 ProductsMapper.xml — `selectProductDtos` + `selectProductDtoById`

**路径**: `src/main/resources/mapper/gg/ProductsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.gg.ProductsMapper">

    <select id="selectProductDtos" resultType="com.lmserver.dto.response.ProductDto">
        SELECT p.*,
               m.name AS mcc_name,
               m.mcc_id AS mcc_code,
               sp.name AS sales_person_name,
               (SELECT COUNT(*) FROM product_assets pa WHERE pa.product_id = p.id) AS asset_count,
               (SELECT COUNT(*) FROM accounts ac WHERE ac.mcc_id = p.mcc_id AND ac.deleted_at IS NULL) AS related_account_count
        FROM products p
        LEFT JOIN mcc m ON p.mcc_id = m.id
        LEFT JOIN sales_persons sp ON p.sales_person_id = sp.id
        <where>
            p.owner_id = #{ownerId}
            AND p.deleted_at IS NULL
            <if test="search != null and search != ''">
                AND (p.product_name LIKE CONCAT('%', #{search}, '%') OR p.customer LIKE CONCAT('%', #{search}, '%'))
            </if>
            <if test="region != null and region != ''">
                AND p.region = #{region}
            </if>
            <if test="status != null and status != ''">
                AND p.status = #{status}
            </if>
        </where>
        ORDER BY p.created_at DESC
    </select>

    <select id="selectProductDtoById" resultType="com.lmserver.dto.response.ProductDto">
        SELECT p.*,
               m.name AS mcc_name,
               m.mcc_id AS mcc_code,
               sp.name AS sales_person_name,
               (SELECT COUNT(*) FROM product_assets pa WHERE pa.product_id = p.id) AS asset_count,
               (SELECT COUNT(*) FROM accounts ac WHERE ac.mcc_id = p.mcc_id AND ac.deleted_at IS NULL) AS related_account_count
        FROM products p
        LEFT JOIN mcc m ON p.mcc_id = m.id
        LEFT JOIN sales_persons sp ON p.sales_person_id = sp.id
        WHERE p.id = #{id}
          AND p.deleted_at IS NULL
    </select>

</mapper>
```

### 4.3 PackagesMapper.xml — `selectPackagesByProductId`

**路径**: `src/main/resources/mapper/gg/PackagesMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.gg.PackagesMapper">

    <select id="selectPackagesByProductId" resultType="com.lmserver.dto.response.PackageDto">
        SELECT id, product_id, series_name, package_name, url, status, created_at
        FROM packages
        WHERE product_id = #{productId}
        ORDER BY created_at DESC
    </select>

</mapper>
```

### 4.4 MccMapper.xml — `selectMccDtos` + `selectMccDtoById`

**路径**: `src/main/resources/mapper/gg/MccMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.gg.MccMapper">

    <select id="selectMccDtos" resultType="com.lmserver.dto.response.MccDto">
        SELECT m.*,
               ml.name AS level_name,
               (SELECT COUNT(*) FROM accounts a WHERE a.mcc_id = m.id AND a.deleted_at IS NULL) AS direct_count
        FROM mcc m
        LEFT JOIN mcc_levels ml ON m.level_id = ml.id
        <where>
            m.owner_id = #{ownerId}
            <if test="search != null and search != ''">
                AND (m.name LIKE CONCAT('%', #{search}, '%') OR m.mcc_id LIKE CONCAT('%', #{search}, '%'))
            </if>
        </where>
        ORDER BY m.created_at DESC
    </select>

    <select id="selectMccDtoById" resultType="com.lmserver.dto.response.MccDto">
        SELECT m.*,
               ml.name AS level_name,
               (SELECT COUNT(*) FROM accounts a WHERE a.mcc_id = m.id AND a.deleted_at IS NULL) AS direct_count
        FROM mcc m
        LEFT JOIN mcc_levels ml ON m.level_id = ml.id
        WHERE m.id = #{id}
    </select>

</mapper>
```

### 4.5 FbAccountsMapper.xml — `selectFbAccountDtos` + `selectFbAccountDtoById`

**路径**: `src/main/resources/mapper/fb/FbAccountsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.fb.FbAccountsMapper">

    <select id="selectFbAccountDtos" resultType="com.lmserver.dto.response.FbAccountDto">
        SELECT a.*,
               s.name AS status_name
        FROM fb_accounts a
        LEFT JOIN account_statuses s ON a.status_id = s.id
        <where>
            a.owner_id = #{ownerId}
            AND a.deleted_at IS NULL
            <if test="search != null and search != ''">
                AND (a.name LIKE CONCAT('%', #{search}, '%') OR a.account_id LIKE CONCAT('%', #{search}, '%'))
            </if>
            <if test="statusId != null">
                AND a.status_id = #{statusId}
            </if>
        </where>
        ORDER BY a.created_at DESC
    </select>

    <select id="selectFbAccountDtoById" resultType="com.lmserver.dto.response.FbAccountDto">
        SELECT a.*,
               s.name AS status_name
        FROM fb_accounts a
        LEFT JOIN account_statuses s ON a.status_id = s.id
        WHERE a.id = #{id}
          AND a.deleted_at IS NULL
    </select>

</mapper>
```

### 4.6 FbBmsMapper.xml — `selectFbBmDtos`（unified 查询）

**路径**: `src/main/resources/mapper/fb/FbBmsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.fb.FbBmsMapper">

    <select id="selectFbBmDtos" resultType="com.lmserver.dto.response.FbBmDto">
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
    </select>

</mapper>
```

### 4.7 FbPixelsMapper.xml — `selectFbPixelDtos`

**路径**: `src/main/resources/mapper/fb/FbPixelsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.fb.FbPixelsMapper">

    <select id="selectFbPixelDtos" resultType="com.lmserver.dto.response.FbPixelDto">
        SELECT p.id, p.pixel_bm_id, p.pixel_name, p.pixel_id, p.created_at,
               pb.name AS bm_name,
               pb.bm_id AS bm_bm_id
        FROM fb_pixels p
        LEFT JOIN fb_pixel_bms pb ON p.pixel_bm_id = pb.id
        <where>
            1 = 1
            <if test="search != null and search != ''">
                AND (p.pixel_name LIKE CONCAT('%', #{search}, '%') OR p.pixel_id LIKE CONCAT('%', #{search}, '%'))
            </if>
        </where>
        ORDER BY p.created_at DESC
    </select>

</mapper>
```

### 4.8 FbProductsMapper.xml — `selectFbProductDtos` + `selectFbProductDtoById`

**路径**: `src/main/resources/mapper/fb/FbProductsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.fb.FbProductsMapper">

    <select id="selectFbProductDtos" resultType="com.lmserver.dto.response.FbProductDto">
        SELECT p.*,
               sp.name AS sales_person_name
        FROM fb_products p
        LEFT JOIN sales_persons sp ON p.sales_person_id = sp.id
        <where>
            p.owner_id = #{ownerId}
            <if test="search != null and search != ''">
                AND p.product_name LIKE CONCAT('%', #{search}, '%')
            </if>
            <if test="status != null and status != ''">
                AND p.status = #{status}
            </if>
        </where>
        ORDER BY p.created_at DESC
    </select>

    <select id="selectFbProductDtoById" resultType="com.lmserver.dto.response.FbProductDto">
        SELECT p.*,
               sp.name AS sales_person_name
        FROM fb_products p
        LEFT JOIN sales_persons sp ON p.sales_person_id = sp.id
        WHERE p.id = #{id}
    </select>

</mapper>
```

### 4.9 FbPixelBmsMapper.xml — `selectFbPixelBmDtos`

**路径**: `src/main/resources/mapper/fb/FbPixelBmsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.fb.FbPixelBmsMapper">

    <select id="selectFbPixelBmDtos" resultType="com.lmserver.dto.response.FbPixelBmDto">
        SELECT pb.*,
               (SELECT COUNT(*) FROM fb_pixels p WHERE p.pixel_bm_id = pb.id) AS pixel_count
        FROM fb_pixel_bms pb
        <where>
            pb.owner_id = #{ownerId}
            AND pb.deleted_at IS NULL
            <if test="search != null and search != ''">
                AND (pb.name LIKE CONCAT('%', #{search}, '%') OR pb.bm_id LIKE CONCAT('%', #{search}, '%'))
            </if>
            <if test="status != null and status != ''">
                AND pb.status = #{status}
            </if>
        </where>
        ORDER BY pb.created_at DESC
    </select>

</mapper>
```

### 4.10 UsersMapper.xml — `selectUserBriefs`

**路径**: `src/main/resources/mapper/common/UsersMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.common.UsersMapper">

    <select id="selectUserBriefs" resultType="com.lmserver.dto.response.UserBriefDto">
        SELECT id, username, display_name, platform, role
        FROM users
        <where>
            <if test="platform != null and platform != ''">
                platform = #{platform}
            </if>
            <if test="role != null and role != ''">
                AND role = #{role}
            </if>
        </where>
        ORDER BY id
    </select>

    <select id="selectUserBriefById" resultType="com.lmserver.dto.response.UserBriefDto">
        SELECT id, username, display_name, platform, role
        FROM users
        WHERE id = #{id}
    </select>

</mapper>
```

### 4.11 AdReportsMapper.xml — 分析查询辅助

**路径**: `src/main/resources/mapper/gg/AdReportsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.gg.AdReportsMapper">

    <!-- 汇总查询：按条件聚合 -->
    <select id="selectSummary" resultType="java.util.LinkedHashMap">
        SELECT COALESCE(SUM(cost), 0) AS total_cost,
               COALESCE(SUM(impressions), 0) AS total_impressions,
               COALESCE(SUM(clicks), 0) AS total_clicks,
               COALESCE(SUM(installs), 0) AS total_installs,
               COALESCE(SUM(in_app_actions), 0) AS total_in_app
        FROM ad_reports
        WHERE user_id = #{userId}
        <if test="productName != null and productName != ''">
            AND product_name = #{productName}
        </if>
        <if test="region != null and region != ''">
            AND region = #{region}
        </if>
        <if test="fromDate != null and fromDate != ''">
            AND report_date &gt;= #{fromDate}
        </if>
        <if test="toDate != null and toDate != ''">
            AND report_date &lt;= #{toDate}
        </if>
    </select>

</mapper>
```

> **说明**: 分析类查询仍使用 `JdbcTemplate` 更灵活，此 XML 作为备选/参考。

### 4.12 FbAdReportsMapper.xml

**路径**: `src/main/resources/mapper/fb/FbAdReportsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.fb.FbAdReportsMapper">

    <select id="selectStats" resultType="com.lmserver.dto.response.FbAdStatsDto">
        SELECT COALESCE(SUM(cost), 0) AS total_cost,
               COALESCE(SUM(impressions), 0) AS total_impressions,
               COALESCE(SUM(clicks), 0) AS total_clicks,
               COALESCE(SUM(registrations), 0) AS total_registrations,
               COALESCE(SUM(purchases), 0) AS total_purchases,
               COUNT(*) AS record_count
        FROM fb_ad_reports
        WHERE user_id = #{userId}
        <if test="productName != null and productName != ''">
            AND product_name = #{productName}
        </if>
        <if test="fromDate != null and fromDate != ''">
            AND report_date &gt;= #{fromDate}
        </if>
        <if test="toDate != null and toDate != ''">
            AND report_date &lt;= #{toDate}
        </if>
    </select>

</mapper>
```

### 4.13 SalesPersonsMapper.xml

**路径**: `src/main/resources/mapper/common/SalesPersonsMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.lmserver.mapper.common.SalesPersonsMapper">

    <select id="selectAllOrdered" resultType="com.lmserver.entity.common.SalesPersons">
        SELECT * FROM sales_persons ORDER BY name
    </select>

</mapper>
```

---

## 5. Mapper 接口需要新增的方法

### 5.1 AccountsMapper

```java
List<AccountDto> selectAccountDtos(
    @Param("ownerId") Long ownerId,
    @Param("search") String search,
    @Param("statusId") Long statusId,
    @Param("mccId") Long mccId,
    @Param("agentId") Long agentId);

AccountDto selectAccountDtoById(@Param("id") Long id);
```

### 5.2 ProductsMapper

```java
List<ProductDto> selectProductDtos(
    @Param("ownerId") Long ownerId,
    @Param("search") String search,
    @Param("region") String region,
    @Param("status") String status);

ProductDto selectProductDtoById(@Param("id") Long id);
```

### 5.3 PackagesMapper

```java
List<PackageDto> selectPackagesByProductId(@Param("productId") Long productId);
```

### 5.4 MccMapper

```java
List<MccDto> selectMccDtos(
    @Param("ownerId") Long ownerId,
    @Param("search") String search);

MccDto selectMccDtoById(@Param("id") Long id);
```

### 5.5 FbAccountsMapper

```java
List<FbAccountDto> selectFbAccountDtos(
    @Param("ownerId") Long ownerId,
    @Param("search") String search,
    @Param("statusId") Long statusId);

FbAccountDto selectFbAccountDtoById(@Param("id") Long id);
```

### 5.6 FbBmsMapper

```java
List<FbBmDto> selectFbBmDtos(@Param("ownerId") Long ownerId);
```

### 5.7 FbPixelsMapper

```java
List<FbPixelDto> selectFbPixelDtos(@Param("search") String search);
```

### 5.8 FbProductsMapper

```java
List<FbProductDto> selectFbProductDtos(
    @Param("ownerId") Long ownerId,
    @Param("search") String search,
    @Param("status") String status);

FbProductDto selectFbProductDtoById(@Param("id") Long id);
```

### 5.9 FbPixelBmsMapper

```java
List<FbPixelBmDto> selectFbPixelBmDtos(
    @Param("ownerId") Long ownerId,
    @Param("search") String search,
    @Param("status") String status);
```

### 5.10 UsersMapper

```java
List<UserBriefDto> selectUserBriefs(
    @Param("platform") String platform,
    @Param("role") String role);

UserBriefDto selectUserBriefById(@Param("id") Long id);
```

### 5.11 FbAdReportsMapper

```java
FbAdStatsDto selectStats(
    @Param("userId") Long userId,
    @Param("productName") String productName,
    @Param("fromDate") String fromDate,
    @Param("toDate") String toDate);
```

---

## 6. 需要改造的 Service/Controller

按返回类型分类：

### 6.1 PagedResponse\<Map\> → PagedResponse\<XxxDto\>

| 接口 | 改为 | Mapper 方法 | 涉及文件 |
|------|------|------------|----------|
| `AccountService.list()` | `PagedResponse<AccountDto>` | `AccountsMapper.selectAccountDtos` | AccountService, AccountServiceImpl, AccountController |
| `ProductService.list()` | `PagedResponse<ProductDto>` | `ProductsMapper.selectProductDtos` | ProductService, ProductServiceImpl, ProductController |
| `MccService.list()` | `PagedResponse<MccDto>` | `MccMapper.selectMccDtos` | MccService, MccServiceImpl, MccController |
| `FbService.listAccounts()` | `PagedResponse<FbAccountDto>` | `FbAccountsMapper.selectFbAccountDtos` | FbService, FbServiceImpl, FbAccountController |
| `FbService.listBms()` / `unified` | `PagedResponse<FbBmDto>` | `FbBmsMapper.selectFbBmDtos` | FbService, FbBmController |
| `FbPixelController.listBms()` | `PagedResponse<FbPixelBmDto>` | `FbPixelBmsMapper.selectFbPixelBmDtos` | FbPixelController |
| `FbPixelController.listPixels()` | `PagedResponse<FbPixelDto>` | `FbPixelsMapper.selectFbPixelDtos` | FbPixelController |

### 6.2 ApiResponse\<Map\> → ApiResponse\<XxxDto\>

| 接口 | 改为 | 涉及文件 |
|------|------|----------|
| `AccountController.detail()` | `ApiResponse<AccountDto>` | AccountController |
| `ProductController.detail()` | `ApiResponse<ProductDetailDto>` | ProductController |
| `MccController.detail()` | `ApiResponse<MccDto>` | MccController |
| `/ad-reports/dashboard` | `ApiResponse<DashboardDto>` | AdReportAnalysisController |
| `/ad-reports/trends` | `ApiResponse<TrendDto>` | AdReportAnalysisController |
| `/ad-reports/compare` | `ApiResponse<List<CompareItemDto>>` | AdReportAnalysisController |
| `/ad-reports/stats` | `ApiResponse<AdStatsDto>` | AdReportAnalysisController |
| `/ad-reports/dates` | `ApiResponse<AdDatesDto>` | AdReportAnalysisController |
| `/ad-reports/analyze` | `ApiResponse<AnalyzeResultDto>` | AdReportAnalysisController |
| `/ad-reports/cross-user` | `ApiResponse<CrossUserResultDto>` | AdReportAnalysisController |
| `/ad-reports/multi-analysis` | `ApiResponse<MultiAnalysisDto>` | AdReportAnalysisController |
| `/ad-reports/multi-ai-chat` | `ApiResponse<MultiAiChatResultDto>` | AdReportAnalysisController |
| `/fonts/list` | `ApiResponse<List<FontDto>>` | FontController |
| `/auth/names` | `ApiResponse<List<UserBriefDto>>` | AuthController, AuthService |
| `/fb/users` | `ApiResponse<List<UserBriefDto>>` | FbUserController |
| `/fb/products/list` | `PagedResponse<FbProductDto>` | FbProductController |
| `/fb/products/{id}/detail` | `ApiResponse<FbProductDetailDto>` | FbProductController |
| `/fb/reports/stats` | `ApiResponse<FbAdStatsDto>` | FbAdReportController |
| `/fb/bms/{bid}/ban-and-migrate` | `ApiResponse<BanAndMigrateResultDto>`（可选，简单 Map 也行） | FbBmController |
| `/fb/extract/parse` | `ApiResponse<FbExtractResultDto>`（可选） | FbExtractController |

### 6.3 ServiceImpl 改造步骤

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

### 6.4 ProductDto 集合字段填充

`ProductDto` 的 `packages`/`runnerIdList` 字段需要额外查询：

```java
// 批量获取所有产品的包列表和 runner 列表
List<Long> productIds = items.stream().map(ProductDto::getId).toList();
// 查询所有 packages
List<Packages> allPkgs = packagesMapper.selectList(
    new LambdaQueryWrapper<Packages>().in(Packages::getProductId, productIds));
Map<Long, List<PackageDto>> pkgMap = allPkgs.stream()
    .collect(Collectors.groupingBy(Packages::getProductId,
        Collectors.mapping(p -> { /* 转换 */ }, Collectors.toList())));
// 查询所有 runners
List<ProductRunners> allRunners = productRunnersMapper.selectList(
    new LambdaQueryWrapper<ProductRunners>().in(ProductRunners::getProductId, productIds));
Map<Long, List<Long>> runnerMap = allRunners.stream()
    .collect(Collectors.groupingBy(ProductRunners::getProductId,
        Collectors.mapping(ProductRunners::getUserId, Collectors.toList())));
// 回填到 DTO
for (ProductDto dto : items) {
    dto.setPackages(pkgMap.getOrDefault(dto.getId(), List.of()));
    dto.setRunnerIdList(runnerMap.getOrDefault(dto.getId(), List.of()));
}
```

---

## 7. 改造顺序

按依赖关系从底向上：

1. **新建 DTO 类** (14 个主 DTO + 12 个辅助/分析 DTO)
2. **新建 Mapper XML** (13 个 XML)
3. **Mapper 接口加方法** (11 个 Mapper 接口)
4. **Service 接口改签名** (5 个 Service 接口)
5. **ServiceImpl 改实现** (5 个 Impl)
6. **Controller 改返回类型** (对应 Controller)
7. **删除旧的手动 Map 构建代码**
8. **编译验证**

---

## 8. 注意事项

1. `@TableField(exist = false)` 必须加在 DTO 新增字段上，否则 MyBatis-Plus 的 `insert`/`update` 会报错
2. Jackson `SNAKE_CASE` 已全局配置，DTO 字段 `mccName` 自动输出为 `mcc_name`
3. `PagedResponse` 和 `ApiResponse` 已经对齐 Python 格式，不需要改动
4. 分析类 DTO（DashboardDto 等）不需要继承实体，是纯 POJO
5. `ProductDto` 的 `packages`/`runnerIdList` 字段需要额外查询填充（不能一条 SQL 搞定集合字段），在 ServiceImpl 中批量查询后 set
6. `FbAccountDto` 的 `bms` 字段也需要通过 `fb_account_bm` 中间表额外查询
7. `FbProductDto` 的 `bms`/`runnerIds`/`lines` 需要额外查询填充
8. `MccDto.totalAccountCount` 是子树递归计算，SQL 无法完成，需 Java 层递归（参见 MccServiceImpl 现有逻辑）
9. `FbBmController.unified()` 端点需要合并两种 BM（fb_bms + fb_pixel_bms），`FbBmsMapper.selectFbBmDtos` 的 UNION ALL 查询完成此合并
10. `AccountController.batchCreate()`、`MccController.batchDelete()` 等简单操作结果的端点，返回内容简单（`{"created": N, "skipped": [...]}`），可选改造为专用 DTO 或保留 Map
11. `SyncResult` DTO 内部使用 `List<Map<String, Object>>`，建议也改造为对应的 DTO 类型
12. `GoogleSheetsService` 和 `GoogleAdsService` 涉及外部 API 调用，其 Map 返回值保留原样更合适（对接外部系统）
13. AI Provider（`DoubaoProvider`/`VeoProvider`/`AtlasProvider`）中使用 Map 做 API 请求体是合理模式，不需要改

---

## 9. 补充：不需要改造的 Map 使用

以下场景使用 `Map<String, Object>` 是合理的，不纳入本次重构范围：

| 场景 | 原因 |
|------|------|
| Google Sheets API 交互 | 外部 API 对接 |
| Google Ads API 交互 | 外部 API 对接 |
| AI Provider API 调用 | 外部 API 对接 |
| `@RequestBody Map<String, Object> body` | 灵活接收前端动态参数 |
| Telegram Bot 通知载荷 | 外部 API 对接 |
| JWT token 解析 | 库函数返回类型 |
| `SyncResult` 中的 `List<Map>` | 需要后续单独改造 |
