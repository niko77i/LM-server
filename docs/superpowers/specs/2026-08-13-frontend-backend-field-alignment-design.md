# 前后端字段对接设计文档

> **日期**: 2026-08-13
> **目的**: 修复前后端字段名不匹配问题，建立可扩展的统一字段命名规范
> **原则**: 统一 snake_case，后端全局策略 + 特殊字段显式标注，前端单一命名风格

---

## 1. 背景与现象

### 1.1 现象

前端产品管理页面只渲染部分字段：

| 能正常显示 | 空白/错误 |
|-----------|----------|
| kpi、地区（region） | 产品名（product_name） |
| 包的链接（url） | 系列名（series_name）、包名（package_name） |

其他页面（账户、MCC、FB 等）也有同样问题。后端接口**返回了完整数据**，但前端拿不到对应字段渲染。

### 1.2 关键线索

能显示的字段（`kpi`、`region`、`url`）都是**单词**，不能显示的（`product_name`、`series_name`）都是**双词**。

这说明：**单词字段前后端命名恰好一致，双词字段命名风格不一致** → 前端用 snake_case，后端实际输出 camelCase。

---

## 2. 根本原因

### 2.1 YAML 缩进错误（唯一根源）

`src/main/resources/application.yml` 存在**缩进错误**：

```yaml
# 当前（错误）—— mail/jackson/servlet 被错误缩进到 mybatis-plus 下
mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
  # 下面三个本应属于 spring，却缩进在 mybatis-plus 下 ↓
  mail: ...
  jackson:
    property-naming-strategy: SNAKE_CASE   # ← 从未生效！
  servlet:
    multipart: ...
```

Python YAML 解析验证结果：

```
spring 的键: ['application', 'datasource']        ← 没有 jackson/mail/servlet
mybatis-plus 的键: ['mapper-locations', 'configuration',
                     'global-config', 'mail', 'jackson', 'servlet']  ← 错误归属
```

### 2.2 连锁影响

`spring.jackson.*` 全部失效，导致：

| 失效配置 | 后果 |
|---------|------|
| `property-naming-strategy: SNAKE_CASE` | DTO/实体序列化为 **camelCase**（如 `productName`），而前端读 snake_case（`product_name`）→ **双词字段全部对不上** |
| `default-property-inclusion: non_null` | null 字段被输出为 `"field": null`（而非省略） |
| `date-format: yyyy-MM-dd HH:mm:ss` | 日期输出 ISO 格式（`2026-08-13T10:30:00`）而非 `yyyy-MM-dd HH:mm:ss` |
| `spring.mail.*` | SMTP 邮件配置失效 |
| `spring.servlet.multipart.*` | 文件上传大小限制失效（默认 1MB 而非 500MB） |

### 2.3 为什么之前"部分能工作"

改造前 Controller 返回 `Map<String, Object>`，key 是**手写的 snake_case 字符串字面量**（`row.put("product_name", ...)`）。Jackson 的 SNAKE_CASE 只作用于 POJO 属性，**不作用于 Map 的 key**，所以手写 snake_case key 能原样输出，前端恰好能对上。

改造后改成 DTO（POJO），SNAKE_CASE 没生效 → 输出 camelCase → 前端对不上。

---

## 3. 前后端字段命名现状

### 3.1 前端命名风格

**整体 snake_case**（无字段名转换层，`api/client.js` 直接返回 `resp.data`）：

- `product.product_name`、`pkg.series_name`、`row.account_id`、`row.mcc_name` ✅ snake_case
- 无 camelcase-keys / lodash 等转换库

### 3.2 后端命名风格（当前实际）

**camelCase**（因为 SNAKE_CASE 失效），仅 10 处 `@JsonProperty` 显式覆盖：

| DTO | Java 字段 | 当前 JSON 输出 |
|-----|----------|--------------|
| AccountDto | agentName | `agent`（@JsonProperty） |
| AccountDto | statusName | `status`（@JsonProperty） |
| ProductDto | salesPersonName | `sales_person`（@JsonProperty） |
| ProductDto | runnerIdList | `runner_ids`（@JsonProperty） |
| MccDto | levelName | `level`（@JsonProperty） |
| MccDto | totalAccountCount | `total_accounts`（@JsonProperty） |

其余全部 camelCase（`productName`、`seriesName`、`accountId`、`mccId`…）。

### 3.3 简写字段（历史遗留，需保留兼容）

前端某些列表/详情读**简写字段名**而非 `_name`/`_id` 全名：

| 实体 | 前端读 | 语义 | 后端字段 |
|------|-------|------|---------|
| 账户 | `agent` | 代理**名称**（非 id） | agentName |
| 账户 | `status` | 状态**名称**（非 id） | statusName |
| MCC | `level` | 等级**名称** | levelName |
| MCC | `total_accounts` | 子树账户总数 | totalAccountCount |
| 产品 | `sales_person` | 商务**名称**（非 id） | salesPersonName |
| 产品 | `runner_ids` | 在跑人员 id **数组** | runnerIdList |

---

## 4. 解决方案

### 4.1 核心：修复 YAML 缩进

把 `mail:`、`jackson:`、`servlet:` 移回 `spring:` 下：

```yaml
spring:
  application:
    name: lm-server
  datasource:
    ...
  mail:                # ← 移回 spring
    host: smtp.qq.com
    ...
  jackson:             # ← 移回 spring，SNAKE_CASE 生效
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    default-property-inclusion: non_null
    property-naming-strategy: SNAKE_CASE
  servlet:             # ← 移回 spring
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      column-format: "`%s`"
```

**修复后效果**：SNAKE_CASE 全局生效，所有 DTO/实体字段自动转 snake_case：

- `productName` → `product_name` ✅
- `seriesName` → `series_name` ✅
- `accountId` → `account_id` ✅
- `mccId` → `mcc_id` ✅

这解决了 **90% 的字段不匹配**。

### 4.2 简写字段统一为全名（用户已确认）

用户确认：**简写字段统一为全名**。修复 YAML 后，删除简写的 `@JsonProperty`，让字段按 SNAKE_CASE 自动输出全名，前端同步改为读全名。

| DTO | Java 字段 | 修复后 JSON（全名） | 旧简写（废弃） |
|-----|----------|-------------------|--------------|
| AccountDto | agentName | `agent_name` | ~~agent~~ |
| AccountDto | statusName | `status_name` | ~~status~~ |
| MccDto | levelName | `level_name` | ~~level~~ |

**需删除的 @JsonProperty**：AccountDto 的 `@JsonProperty("agent")`、`@JsonProperty("status")`，MccDto 的 `@JsonProperty("level")`。

**前端同步改**：`row.agent`→`row.agent_name`、`row.status`→`row.status_name`、`row.level`→`row.level_name`。

**保留的字段**（GG-Server 权威命名，已是 snake_case 语义名，不改）：

| 字段 | 说明 |
|------|------|
| `sales_person` | GG 产品商务名称（GG-Server 权威 `COALESCE(sp.name,'') AS sales_person`） |
| `sales_person_name` | FB 产品商务名称（GG-Server FB 用 `sales_person_name`，与 GG 不同，历史不一致，本次保持） |
| `total_accounts` | MCC 子树账户总数（GG-Server 权威 `total_accounts`） |
| `runner_ids` | 产品在跑人员 id 数组 |

### 4.3 重写 /fb/reports/stats（GG-Server 权威核对）

GG-Server 权威后端（`py/routes/fb_routes.py:1390`）的 `/api/fb/reports/stats` 返回**按 product_name + line_name + report_date 分组的数组**，字段：

```
product_name, line_name, report_date, total_cost, total_impressions,
total_clicks, total_registrations, total_purchases, account_count
```

我之前 Java 改成了单个 `FbAdStatsDto` 汇总对象，**错误**。需重写为返回 `List<FbReportStatDto>`（分组数组），字段 snake_case。

### 4.3.1 修正 bm_type 值

GG-Server 权威 `fb_bms/unified` 的 `bm_type` 值是 **`account`/`pixel`**（`py/routes/fb_routes.py:78,84`），我之前写成了 `normal`/`pixel_bm`，需修正。

### 4.4 前端 camelCase 混用修复（需前端配合）

前端有 4 处 camelCase 混用，修复 YAML 后需统一为 snake_case：

| 位置 | 当前 | 应为 |
|------|------|------|
| `views/AnalysisView.vue:760` | `r.productName \|\| r.product_name` | 只读 `r.product_name` |
| `utils/adsParser.js`（整个文件） | 输出 camelCase（`customerId`、`inAppActions`） | 输出 snake_case（`customer_id`、`in_app_actions`） |
| `views/DataManageView.vue` save 分支 | 发 camelCase（`customerId`、`inAppActions`） | 发 snake_case（`customer_id`、`in_app_actions`） |
| `stores/auth.js` | `d.accessToken`（camelCase） | `d.access_token`（若后端也改）或保持 |

> 注：`accessToken` 涉及登录响应，前端有注释「Java 返回 {success, data:{accessToken, user}}」。修复 YAML 后 `LoginResponse.accessToken` 会变 `access_token`，需前后端同步确认。

### 4.5 响应结构不一致（需后端核对）

前端同一接口在不同文件读不同字段，需确认后端实际返回哪种结构：

| 接口 | 文件 A 读 | 文件 B 读 | 后端实际 |
|------|----------|----------|---------|
| `/mcc/options` | `res.options` | `res.data` | 需核对 |
| `/regions/list` | `res.items`（GG） | `res.regions`（FB） | 需核对 |
| `/sales-persons/list` | `res.items` | `res.sales_persons` | 需核对 |
| `/statuses/list` | `res.items` | `res.statuses \|\| res.data` | 需核对 |
| `/products/:id/detail` | `res.data` | `res.product` | 需核对 |
| `/ad-reports/list` | `res.items` | `res.reports` | 需核对 |

这些不一致**可能与本次 YAML 修复无关**（是历史遗留的响应结构问题），但会导致"后端返回了数据前端拿不到"，应一并核对修复。

---

## 5. 可扩展的字段命名规范（核心约定）

为避免未来再次出现此类问题，建立以下**强制规范**：

### 5.1 命名约定

1. **前后端统一 snake_case**：所有 API 的 JSON 字段名一律 snake_case（`product_name`、`series_name`、`account_id`）。
2. **后端实现方式**：
   - 默认依赖全局 `spring.jackson.property-naming-strategy: SNAKE_CASE` 自动转 snake_case。
   - **不要**在 DTO/实体上逐个字段加 `@JsonProperty` 来转 snake_case（全局策略已覆盖）。
   - 仅当字段名**偏离**标准 snake_case 全名时（如简写 `agent` vs `agent_name`、语义重命名 `sales_person` 名称 vs id），才用 `@JsonProperty` 显式标注，并**注释说明原因**。
3. **前端实现方式**：
   - 统一 snake_case 访问后端字段，**禁止** camelCase 混用。
   - 禁止在前端做字段名转换（如 `r.productName || r.product_name` 这种兼容写法应删除）。

### 5.2 新接口开发检查清单

新增/修改接口时，必须满足：

- [ ] 后端 DTO 字段命名符合 snake_case（或 @JsonProperty 显式标注 + 注释）
- [ ] 前端访问字段名与后端 JSON 字段名**完全一致**（逐字段核对）
- [ ] 响应包装统一：分页用 `PagedResponse`（`items`/`total`/`page`/`size`），单对象用 `ApiResponse`（`data`）
- [ ] 列表项嵌套字段（如 `packages[].series_name`）也遵守 snake_case

### 5.3 防回归手段

1. **application.yml 缩进**：任何 YAML 修改后，用 `yaml.safe_load` 校验键归属（本次教训）。
2. **字段一致性测试**：可选增加一个契约测试，比对前端 `api/*.js` 访问的字段与后端 DTO 字段是否一致。
3. **代码审查**：新接口 CR 时检查前端字段访问与后端返回是否一致。

---

## 6. 完整字段对照表

### 6.1 核心实体字段（修复后）

| 前端读（snake_case） | 后端 Java 字段 | 修复后 JSON | 状态 |
|---------------------|---------------|-----------|------|
| `product_name` | productName | product_name | ✅ 修复 YAML 后自动 |
| `series_name` | seriesName | series_name | ✅ |
| `package_name` | packageName | package_name | ✅ |
| `account_id` | accountId | account_id | ✅ |
| `mcc_id` | mccId | mcc_id | ✅ |
| `mcc_name` | mccName | mcc_name | ✅ |
| `mcc_code` | mccCode | mcc_code | ✅ |
| `created_at` | createdAt | created_at | ✅ |
| `updated_at` | updatedAt | updated_at | ✅ |
| `owner_id` | ownerId | owner_id | ✅ |
| `asset_count` | assetCount | asset_count | ✅ |
| `related_account_count` | relatedAccountCount | related_account_count | ✅ |
| `sales_person_id` | salesPersonId | sales_person_id | ✅ |
| `agency_ratio` | agencyRatio | agency_ratio | ✅ |
| `is_archived` | isArchived | is_archived | ✅ |

### 6.2 简写字段统一为全名（用户已确认）

| 前端读（旧） | 前端读（新） | 后端 Java 字段 | 修复后 JSON |
|-------------|-------------|--------------|-----------|
| ~~`agent`~~ | `agent_name` | agentName | `agent_name`（删 @JsonProperty） |
| ~~`status`~~ | `status_name` | statusName | `status_name`（删 @JsonProperty） |
| ~~`level`~~ | `level_name` | levelName | `level_name`（删 @JsonProperty） |

**保留的语义字段**（GG-Server 权威，不改）：

| 前端读 | 后端 Java 字段 | 修复后 JSON | 说明 |
|--------|--------------|-----------|------|
| `total_accounts` | totalAccountCount | `total_accounts`（保留 @JsonProperty） | 子树总数，GG-Server 权威 |
| `sales_person` | salesPersonName | `sales_person`（保留 @JsonProperty） | GG 产品商务名称 |
| `runner_ids` | runnerIdList | `runner_ids`（保留 @JsonProperty） | 数组 |

### 6.3 FB 模块字段

| 前端读 | 后端 Java 字段 | 修复后 JSON | 状态 |
|--------|--------------|-----------|------|
| `bm_id` | bmId | bm_id | ✅ |
| `bm_type` | bmType | bm_type | ✅ |
| `account_count` | accountCount | account_count | ✅ |
| `pixel_count` | pixelCount | pixel_count | ✅ |
| `sales_person_name` | salesPersonName | sales_person_name | ✅（FB 产品用 `sales_person_name`，GG 产品用 `sales_person`，注意区别） |
| `pixel_name` | pixelName | pixel_name | ✅ |
| `pixel_id` | pixelId | pixel_id | ✅ |
| `pixel_bm_id` | pixelBmId | pixel_bm_id | ✅ |
| `bm_name` | bmName | bm_name | ✅ |
| `bm_bm_id` | bmBmId | bm_bm_id | ✅ |

### 6.4 分析报告字段（ad-reports）

| 前端读 | 后端 Java 字段 | 修复后 JSON | 状态 |
|--------|--------------|-----------|------|
| `total_cost` | totalCost | total_cost | ✅ |
| `total_impressions` | totalImpressions | total_impressions | ✅ |
| `total_clicks` | totalClicks | total_clicks | ✅ |
| `total_installs` | totalInstalls | total_installs | ✅ |
| `total_in_app` | totalInApp | total_in_app | ✅ |
| `avg_cpi` | avgCpi | avg_cpi | ✅ |
| `avg_ctr` | avgCtr | avg_ctr | ✅ |
| `avg_cvr` | avgCvr | avg_cvr | ✅ |
| `record_count` | recordCount | record_count | ✅ |
| `period_compare` | periodCompare | period_compare | ✅ |
| `asset_count` | assetCount | asset_count | ✅ |
| `report_days` | reportDays | report_days | ✅ |
| `pearson_r` | pearsonR | pearson_r | ✅ |

### 6.5 需确认的响应结构不一致（第 4.5 节）

这些不是字段名问题，而是**同一接口的响应包装层不一致**，需后端逐接口核对实际返回，前端统一读取方式。

---

## 7. 实施步骤

按依赖顺序：

1. **修复 YAML 缩进**（核心，一处改动解决 90% 问题）
2. **删除 FbAdStatsDto 的 camelCase @JsonProperty**（待确认 /fb/reports/stats 数据结构后处理）
3. **编译验证**：`mvn compile`
4. **后端启动验证**：用 curl 测几个接口，确认字段是 snake_case
5. **前端 camelCase 修复**（4 处，需用户确认前端改动范围）
6. **响应结构不一致核对**（第 4.5 节，逐接口确认）
7. **回归测试**：产品/账户/MCC/FB 各页面

---

## 8. 风险与注意事项

1. **修复 YAML 会同时恢复 `spring.mail` 和 `spring.servlet.multipart`**——这是好事（修复了邮件和上传限制的隐性 bug），但要确认 SMTP 密码等配置正确、上传 500MB 限制符合预期。
2. **日期格式变化**：修复后日期从 ISO（`2026-08-13T10:30:00`）变为 `yyyy-MM-dd HH:mm:ss`，前端如有日期解析逻辑需确认兼容。
3. **null 字段省略**：修复后 `non_null` 生效，DTO/实体的 null 字段不再输出 `"field": null`，前端如有 `=== null` 判断需确认（一般是好事）。
4. **`accessToken` → `access_token`**：登录响应字段名会变，前后端需同步（前端 `stores/auth.js`、`client.js` 读 token 的地方）。
5. **MccDto.isOwner 序列化坑**：`private boolean isOwner` 的 Lombok `isOwner()` getter 会被 Jackson 识别为 `owner`（去 is 前缀）。建议前端读 `owner` 或后端改字段名 `owner`。
6. **FB 产品 vs GG 产品的商务字段名不同**：GG 用 `sales_person`（名称），FB 用 `sales_person_name`（名称），这是历史不一致，本次不动（避免破坏前端），但应记录为待统一项。

---

## 9. 已确认决策（2026-08-13 用户拍板）

| # | 事项 | 决策 |
|---|------|------|
| 1 | `/fb/reports/stats` 结构 | 已核对 GG-Server：返回**数组**（GROUP BY product_name/line_name/report_date），重写 stats 接口 |
| 2 | `accessToken` 命名 | 前后端同步改为 `access_token` |
| 3 | 前端 camelCase 4 处 | 一并修复（AnalysisView/adsParser/DataManageView/auth） |
| 4 | 简写字段 | 统一为全名：`agent_name`/`status_name`/`level_name` |

**另发现需修正**（GG-Server 权威核对）：
- `bm_type` 值应为 `account`/`pixel`（非 `normal`/`pixel_bm`）
