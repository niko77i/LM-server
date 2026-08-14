# 前后端字段对接修复实现计划

> **日期**: 2026-08-13
> **关联设计文档**: `../specs/2026-08-13-frontend-backend-field-alignment-design.md`
> **原则**: 统一 snake_case 全名，后端 SNAKE_CASE 全局策略 + 特殊字段显式标注，前端单一命名风格

---

## 0. 改动总览

| 类别 | 文件数 | 说明 |
|------|-------|------|
| 后端配置 | 1 | application.yml 缩进修复 |
| 后端 DTO | 6 | 删简写 @JsonProperty、新建 FbReportStatDto、修 isOwner |
| 后端 Controller | 1 | FbAdReportController.stats 重写为数组 |
| 后端 Mapper XML | 1 | FbBmsMapper.xml bm_type 值修正 |
| 前端 | ~12 | 简写→全名、camelCase→snake_case、accessToken |

---

## 1. 后端改动

### 1.1 application.yml（核心）

**文件**: `src/main/resources/application.yml`

把 `mail:`、`jackson:`、`servlet:` 从 `mybatis-plus:` 下移回 `spring:` 下。

当前（错误）：
```yaml
mybatis-plus:
  configuration: ...
  global-config: ...
  mail: ...       # ← 错误位置
  jackson: ...    # ← 错误位置
  servlet: ...    # ← 错误位置
```

修复后：
```yaml
spring:
  application: ...
  datasource: ...
  mail: ...       # ← 移回 spring
  jackson: ...    # ← 移回 spring（SNAKE_CASE 生效）
  servlet: ...    # ← 移回 spring

mybatis-plus:
  mapper-locations: ...
  configuration: ...
  global-config: ...
```

**验证**: `python -c "import yaml; print(list(yaml.safe_load(open('src/main/resources/application.yml')).get('spring',{}).keys()))"` 应包含 `jackson`/`mail`/`servlet`。

### 1.2 AccountDto（删简写 @JsonProperty）

**文件**: `dto/response/AccountDto.java`

- 删除 `@JsonProperty("agent")`（agentName 字段），SNAKE_CASE 自动输出 `agent_name`
- 删除 `@JsonProperty("status")`（statusName 字段），SNAKE_CASE 自动输出 `status_name`

### 1.3 MccDto（删简写 @JsonProperty + 修 isOwner）

**文件**: `dto/response/MccDto.java`

- 删除 `@JsonProperty("level")`（levelName），输出 `level_name`
- 保留 `@JsonProperty("total_accounts")`（totalAccountCount），GG-Server 权威
- `isOwner` 加 `@JsonProperty("is_owner")`（避免 Lombok `isOwner()` getter 被 Jackson 误识别为 `owner`）

### 1.4 ProductDto（保持不变）

**文件**: `dto/response/ProductDto.java`

- 保留 `@JsonProperty("sales_person")`（salesPersonName，GG-Server 权威）
- 保留 `@JsonProperty("runner_ids")`（runnerIdList）
- 保留 `@JsonIgnoreProperties({"runnerIds", "salesPerson"})`

### 1.5 新建 FbReportStatDto

**文件**: `dto/response/FbReportStatDto.java`（新建）

`/fb/reports/stats` 的数组元素，字段（靠 SNAKE_CASE 自动转 snake_case）：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FbReportStatDto {
    private String productName;      // → product_name
    private String lineName;         // → line_name
    private LocalDateTime reportDate; // → report_date
    private Double totalCost;        // → total_cost
    private Long totalImpressions;   // → total_impressions
    private Long totalClicks;        // → total_clicks
    private Long totalRegistrations; // → total_registrations
    private Long totalPurchases;     // → total_purchases
    private Long accountCount;       // → account_count
}
```

### 1.6 重写 FbAdReportController.stats

**文件**: `controller/fb/FbAdReportController.java`

`/fb/reports/stats` 从返回单个 `FbAdStatsDto` 改为返回 `ApiResponse<List<FbReportStatDto>>`（GROUP BY 分组数组），对齐 GG-Server：

```sql
SELECT product_name, line_name, report_date,
       SUM(cost) as total_cost, SUM(impressions) as total_impressions,
       SUM(clicks) as total_clicks, SUM(registrations) as total_registrations,
       SUM(purchases) as total_purchases, COUNT(DISTINCT account_id) as account_count
FROM fb_ad_reports WHERE user_id=? [AND product_name=?] [AND line_name=?] [AND report_date>=?] [AND report_date<=?]
GROUP BY product_name, line_name, report_date ORDER BY report_date DESC
```

用 `JdbcTemplate` 实现（与 AdReportAnalysisController 一致）。

### 1.7 删除 FbAdStatsDto（不再使用）

**文件**: `dto/response/FbAdStatsDto.java`（删除）

（被 FbReportStatDto 替代）

### 1.8 修正 bm_type 值

**文件**: `resources/mapper/fb/FbBmsMapper.xml`

- `selectFbBmDtos`（unified UNION）：`'normal' AS bm_type` → `'account' AS bm_type`，`'pixel_bm' AS bm_type` → `'pixel' AS bm_type`
- `selectFbBmDtosNormal`：`'normal' AS bm_type` → `'account' AS bm_type`

**文件**: `controller/fb/FbBmController.java`

- `unified()` 方法的 bmType 过滤值：`"pixel_bm"` → `"pixel"`，`"normal"` → `"account"`

---

## 2. 前端改动

### 2.1 简写字段 → 全名（agent/status/level）

**agent → agent_name**（7 个文件）：
- `components/AccountBatchImportModal.vue`
- `components/AccountDeletedModal.vue`
- `components/AccountDetailModal.vue`
- `components/AccountModal.vue`
- `components/RechargeBatchModal.vue`
- `components/RechargeModal.vue`
- `views/AdsAccountPanel.vue`

**status → status_name**（账户状态名称，注意：表单提交的 `status_id` 不变，仅响应展示字段改）：
- 上述账户相关文件里读 `row.status`/`account.status`（状态名称）的地方

**level → level_name**（1 个文件）：
- `views/MccPanel.vue`

> 注意：需区分「响应展示字段」（`agent`→`agent_name`）和「表单提交字段」（`agent_id`/`status_id` 不变，这些是 id，本来就叫 `_id`）。前端 agent 报告的简写只涉及展示名称字段。

### 2.2 accessToken → access_token

- `stores/auth.js`：`d.accessToken` → `d.access_token`（登录/刷新响应）
- 同步检查 `LoginResponse` 的 `refreshToken` → `refresh_token`、内嵌 user 的 `displayName`→`display_name`、`telegramUsername`→`telegram_username` 等前端读取处

### 2.3 前端 camelCase 混用 → snake_case（4 处）

1. `views/AnalysisView.vue:760`：`r.productName || r.product_name` → 只读 `r.product_name`
2. `utils/adsParser.js`：输出 camelCase（`customerId`/`inAppActions` 等）→ 改为 snake_case（`customer_id`/`in_app_actions`）
3. `views/DataManageView.vue`（save 分支）：发 camelCase → snake_case
4. `stores/auth.js`：`d.accessToken` → `d.access_token`（与 2.2 合并）

### 2.4 bm_type 值

- `views/fb/FbBmPanel.vue` 等读 `bm_type` 的地方：确认判断值是 `account`/`pixel`（前端 agent 报告已显示前端用 `account`/`pixel`，后端修正后即对齐，前端可能无需改）

---

## 3. 执行顺序

1. **后端 1.1**：修复 application.yml（先做，影响最大）
2. **后端 1.2~1.8**：DTO + Controller + Mapper XML
3. **编译**：`mvn compile`
4. **后端启动 + curl 验证**：确认几个核心接口返回 snake_case 全名
5. **前端 2.1~2.3**：字段同步改
6. **前端构建验证**：`npm run build`（输出到后端 static）
7. **联调回归**：产品/账户/MCC/FB 各页面

---

## 4. 验证方式

### 4.1 YAML 生效验证

```bash
cd D:/server/cc/LM-Server
python -c "import yaml; d=yaml.safe_load(open('src/main/resources/application.yml',encoding='utf-8')); print('spring keys:', list(d.get('spring',{}).keys()))"
# 期望包含 jackson/mail/servlet
```

### 4.2 接口字段验证（后端启动后）

```bash
# 账户列表（期望 agent_name/status_name，而非 agent/status）
curl -H "Authorization: Bearer <token>" "http://localhost:8080/api/accounts/list?page=1&size=2"

# 产品列表（期望 product_name/series_name，而非 productName/seriesName）
curl -H "Authorization: Bearer <token>" "http://localhost:8080/api/products/list?page=1&size=2"

# fb reports stats（期望数组 + account_count）
curl -H "Authorization: Bearer <token>" "http://localhost:8080/api/fb/reports/stats"
```

### 4.3 前端回归

- 产品管理页：产品名、系列名、包名、商务名正常显示
- 账户页：代理名、状态名正常显示
- MCC 页：等级名、账户数正常显示
- FB 页：BM 类型、统计正常显示

---

## 5. 风险提示

1. 修复 YAML 会同时恢复 `spring.mail`（SMTP）和 `spring.servlet.multipart`（上传 500MB），属修复隐性 bug，需确认无副作用。
2. 日期格式从 ISO 变 `yyyy-MM-dd HH:mm:ss`，前端日期展示需确认。
3. `non_null` 生效后，null 字段不再输出，前端 `=== null` 判断需确认。
4. 前端改动涉及 ~12 个文件，需仔细区分「展示字段」和「提交字段」。
