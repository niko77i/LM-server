# LM-Server 接口差异修复设计文档

> **文档版本**: v1.1  
> **日期**: 2026-08-05（v1.1 补充 FB 模块审查结果）  
> **目的**: Python GG-Server 与 Java LM-Server 全量接口差异分析及修复方案  
> **审查范围**: 全部 238 个 Python 路由 — GG Accounts / Products / YouTube / Video / Audio / AdReports / Scrape / Copywriting / Fonts / Audit / Delist / Config / MCC / Auth / GoogleSheets / Option / Admin / **FB (BM/账户/产品/Pixel/提取/报告)**  

---

## 目录

1. [总体评估](#1-总体评估)
2. [阻塞项 — 必须修复](#2-阻塞项)
3. [高优先级 — 核心业务差异](#3-高优先级)
4. [中优先级 — 功能缺失](#4-中优先级)
5. [低优先级 — 改进项](#5-低优先级)
6. [修复实施计划](#6-修复实施计划)

---

## 1. 总体评估

### 1.1 各模块匹配度

| 模块 | 匹配度 | 状态 |
|------|--------|------|
| Auth 认证 | ⚠️ 80% | 接口基本对齐，响应格式不兼容 |
| GG Accounts | ❌ 30% | 0 个一致，10 个严重差异，3 个完全缺失 |
| GG Products | ❌ 35% | 路由结构不兼容，6 个接口缺失 |
| GG MCC | ⚠️ 50% | 缺树形逻辑/循环检测/关联检测/批量操作 |
| GG Recharge | ⚠️ 65% | 缺 Sheets 同步/重试机制 |
| GG AdReports | ❌ 30% | 缺仪表盘/趋势/对比/多维分析/AI 对话 |
| GG YouTube | ❌ 50% | 缺批量操作/多维筛选/统计汇总/关联清理 |
| GG Video/AI/Audio | ❌ 55% | AI 仅单图/无扫描目录/无历史删除 |
| GG Scrape | ❌ 15% | Java 只有缓存记录，无实际爬取 |
| GG Copywriting | ⚠️ 70% | 缺批量编辑/scope 概念/细粒度权限 |
| GG Fonts | ❌ 0% | Java 完全缺失 |
| GG Audit | ❌ 0% | Java 完全缺失 |
| GG Delist | ⚠️ 40% | 仅有查询，无检测引擎/通知/dismiss |
| Config/Settings | ❌ 25% | 缺 Google Sheets 全流程/翻译/AI 按用户隔离 |
| Option 选项 | ⚠️ 60% | 缺 owner_id 数据隔离/删除保护 |
| Admin 管理 | ⚠️ 55% | 缺创建用户/重置密码/Telegram/导出 |
| Google Sheets 做表 | ❌ 10% | **列映射完全不同，是最严重的问题** |
| Data 导入导出 | ⚠️ 60% | 基本对齐但缺部分功能 |
| Utility 工具 | ❌ 10% | 几乎全部缺失 |
| FB 平台 | ❌ 30% | 2 个阻塞项(ban-migrate空壳/unified无UNION)，4 个接口缺失 |
| **总体** | **~30%** | **Java 版与 Python 版差异巨大，需大量重写** |

### 1.2 根本原因

Java LM-Server 的实现在**没有参考 Python 源码**的情况下完成，大部分接口是 AI 按"常识"生成的通用 CRUD，并未按照 Python GG-Server 的实际业务逻辑来实现。导致：

1. **列映射、去重逻辑、业务规则完全不同**
2. **路由结构不兼容**（嵌套 vs 扁平）
3. **响应格式不兼容**（`{success, data}` vs `ApiResponse<T>`）
4. **权限控制基本缺失**（owner_id 隔离、角色检查）
5. **大量核心功能完全缺失**（爬虫引擎、掉包检测、分析仪表盘等）

---

## 2. 阻塞项 — 必须修复（P0）

这些差异导致数据损坏、安全漏洞或系统不可用，必须在任何上线前修复。

### 🔴 P0-1: Google Sheets GG 做表列映射完全不同

**影响**: 两张后端写入的表格格式不同，数据无法互通。同一个 Google Sheets 文件被 Python 和 Java 交替写入会产生混乱。

| 列 | Python (正确) | Java (错误) |
|----|-------------|-----------|
| A | 日期 | 日期 ✅ |
| B | 运营 | 运营 ✅ |
| C | **账户名称** | 客户名称 ❌ |
| D | **客户ID** | 商务 ❌ |
| E | **账号消耗**(数值) | 投放国家(文本) ❌ |
| F | 报给客户(空) | 渠道号 ❌ |
| G | 客户名称(产品名/养户) | 系列名 ❌ |
| H | 商务(止戈) | 包名 ❌ |
| I | 投放国家 | 账户ID ❌ |
| J | 广告系列 | 素材图 ❌ |
| K | 平台实际(空) | 落地页 ❌ |
| L | 代投比例(%) | 账号消耗(数值) ❌ |
| M | `=F*L` 利润公式 | **缺失** ❌ |
| N | `=F-K+M` 实际消耗公式 | **缺失** ❌ |

**去重索引差异**:
- Python: `(日期, 客户ID, 广告系列)` 三元组
- Java: `(日期, 账户ID)` 二元组 — 不够精确

**修复方案**:
1. 重写 `ZuobiaoRow.java` — 列映射对齐 Python 的 A-N 14 列
2. 重写 `toSheetRow()` 方法
3. 重写 `GoogleSheetsService.upsertZuobiao()` — 去重索引改为三元组
4. 新增 M/N 公式列写入逻辑
5. 新增 D 列 TEXT 格式、E 列 NUMBER 千分位格式化
6. 修复养户行逻辑 — G="养户"/H="止戈"/L="0%"
7. Controller 请求体字段名对齐 Python

### 🔴 P0-2: 权限控制缺失

**影响**: 任意登录用户可操作他人数据。数据安全漏洞。

涉及接口（至少以下）:
- `DELETE /api/accounts/{id}` — 不校验 owner_id
- `DELETE /api/accounts/{id}/permanent` — 不校验 owner_id + NPE 风险
- `POST /api/accounts/{id}/restore` — 不校验 owner_id
- `POST /api/accounts/batch-delete` — 不校验 owner_id
- `POST /api/accounts/batch-update` — 不校验 owner_id
- `POST /api/accounts/sync-from-sheet` — spreadsheet_id 来自客户端输入 + 查询无 owner 隔离
- `PUT/DELETE /api/ad-reports/{id}` — 不校验 owner
- `PUT/DELETE /api/copywriting/{id}` — 不校验 owner

**修复方案**:
1. 所有写操作必须在 SQL WHERE 条件中加入 `owner_id = currentUserId`
2. `sync-from-sheet` 的 spreadsheet_id 从服务端 config/tags 读取，不接受客户端输入
3. 统一抽取 `@CheckOwner` 注解或 AOP 切面

### 🔴 P0-3: 选项数据缺少 owner_id 隔离

**影响**: 所有用户的 agents/mcc-levels 混在一起，用户 A 可以看到/误删用户 B 的数据。

**修复方案**:
1. `agents` 和 `mcc_levels` 的 list/create/update/delete 全部加 `owner_id` 过滤
2. `statuses`/`sales_persons`/`regions` 按 platform 隔离（GG/FB 各一套）
3. 删除前检查外键引用（agents 被 accounts 引用时拒绝删除等）

### 🔴 P0-4: FB ban-and-migrate 是空壳 + unified 无 UNION

**ban-and-migrate**: Java 仅设置 `status="banned"`，以下全部缺失：
- 目标 BM 不存在时自动创建
- 账户关联迁移（INSERT OR IGNORE 到目标 BM → DELETE 原关联）
- 迁移历史记录写入 `fb_account_bm_history`
- 产品关联检查及警告返回

**unified**: Java 直接调用 `listBms`（只查 `fb_bms`），完全缺失 UNION `fb_pixel_bms` 的核心逻辑。像素 BM 不会出现在统一列表中。

**修复方案**: 重写 `FbBmController.banAndMigrate()` 和 `unified()`，按 Python 源码对齐。

### 🔴 P0-5: FB 产品/账户删除语义不一致 + 缺失恢复接口

| 缺失接口 | 影响 |
|---------|------|
| `GET /api/fb/accounts/deleted` | 无法查看已删除账户 |
| `POST /api/fb/accounts/{aid}/restore` | 无法恢复软删除账户 |
| `DELETE /api/fb/accounts/{aid}/permanent` | 无法物理删除 |
| `GET /api/fb/accounts/{aid}/bm-history` | 无法查看 BM 迁移历史 |
| `POST /api/fb/products/{pid}/restore` | 无法恢复归档产品 |

FB 产品删除: Python `is_archived=1`（归档可恢复），Java `deleteById`（物理删除不可恢复）

**修复方案**: 补全 5 个缺失接口，产品删除改为软删除。

### 🔴 P0-6: FB 数据提取 sorted_mode 缺失 + cost 取值错误

**sorted_mode**: Python 支持解析展示次数/点击/注册/购物/cost_per_purchase，Java 全部硬编码为 0

**cost 取值**: Python 取 `max(dollar_amounts)`，Java 取 `distinctCosts.get(0)` — 多金额时值不准确

**修复方案**: 重写 `FbExtractService.parse()`，补充 sorted_mode 解析 + 修正 cost 取值策略。

### 🔴 P0-7 (原 P0-4): 产品删除语义不一致（软删除 vs 硬删除）

**影响**: Python 版删除可恢复（软删除），Java 版物理删除不可恢复。用户迁移后删除的数据永久丢失。

**修复方案**:
1. `DELETE /api/products/{id}` 改为软删除（设 `is_archived=1, deleted_at=now`）
2. 新增 `POST /api/products/{id}/restore` 恢复接口
3. 物理删除单独抽到 `DELETE /api/products/{id}/permanent`
4. 删除时写入 audit_log（产品+包快照）

---

## 3. 高优先级 — 核心业务差异（P1）

这些差异导致核心业务流程不完整或行为不同。

### 🟡 P1-1: 路由结构兼容性

Python 嵌套结构 vs Java 扁平结构，前端 API 调用路径需全面改造：

| 资源 | Python 路由 | Java 现有路由 | 修复方向 |
|------|------------|-------------|---------|
| 包管理 | `/api/products/{pid}/packages` | `/api/packages/*` | **保持 Java 现有或对齐 Python？** |
| 在跑人员 | `/api/products/{pid}/runners` | `/api/product-runners/*` | 同上 |
| 素材关联 | `/api/products/{pid}/assets` | `/api/product-assets/*` | 同上 |
| 掉包检测 | `/api/products/{pid}/check-delist` | `/api/delist/*` | 同上 |
| 充值记录 | `/api/accounts/{aid}/recharge-records` | `/api/recharge/list?accountId=` | 同上 |
| MCC历史 | `/api/accounts/{aid}/mcc-history` | `/api/accounts/mcc-history?accountId=` | 同上 |

**修复方案**: **对齐 Python** — 路由结构应最大限度兼容原前端。建议在 Java 中同时注册新旧两套路由，通过 `@RequestMapping` 支持多个路径，让前端逐步过渡。

### 🟡 P1-2: 账户管理核心业务缺失

| 缺失功能 | 影响 |
|---------|------|
| `POST /api/accounts/batch-create` | 无法批量创建账户 |
| `PUT /api/accounts/{aid}/reassign` | 账户归属权无法转移 |
| 清账逻辑（状态变更自动写充值记录+Sheet） | 状态从"存活"切到非存活时不触发清账 |
| MCC 变更历史自动记录 | account_mcc_history 表不会自动写入 |
| status_changed_date 自动追踪 | 状态变更时间不记录 |
| 状态变更时 Sheet 同步（备注列+H列解绑） | 我的看板不同步 |
| agent/status 文本名自动创建 | 仅接受 ID 不接受名称 |
| `DELETE /api/accounts/{aid}/mcc-history/{hid}` | 无法删除单条 MCC 历史 |

**修复方案**: 按 Python 源码逐接口补充逻辑。

### 🟡 P1-3: 产品管理核心功能缺失

| 缺失功能 | 影响 |
|---------|------|
| `GET /api/products/runner-products` | 消耗录入下拉框无数据 |
| `POST /api/products/merge` | 无法合并产品 |
| `POST /api/products/import-text` | 无法从文本批量导入 Google Play 产品 |
| 同名产品创建时自动追加包 | 创建同名产品会重复而非追加 |
| 创建产品时自动将创建者加入 runner | 创建者不在 runner 列表 |
| MCC 自动分配权限链 | 新增 runner 时无 MCC 权限 |
| 产品改名时同步 videos/ad_reports 冗余字段 | 历史数据不同步 |

**修复方案**: 按 Python 源码逐接口补充。

### 🟡 P1-4: Google Sheets 同步缺失

| 缺失功能 | 影响 |
|---------|------|
| 做表数据同步写入 ad_reports 表 | Google Sheets 和数据库不同步 |
| 充值记录后台异步写 Sheet | 充值后不自动写 Sheet |
| 充值 Sheet 重试机制（30s 等待 + retry-sheets） | 同步失败无重试 |
| Sheets 同步状态查询 + 错误记录 | 无法排查同步失败原因 |
| 状态变更时看板 Sheet 同步（备注+H列） | 看板数据不同步 |

**修复方案**: 在 `GoogleSheetsService` 中补全异步写入逻辑，充值记录写入后触发 `@Async` 写 Sheet。

### 🟡 P1-5: 广告报告分析系统缺失

| 缺失功能 | 影响 |
|---------|------|
| `GET /api/ad-reports/dashboard` (完整版) | 无 KPI 环比/异常检测/素材关联数 |
| `GET /api/ad-reports/trends` | 无趋势分析 |
| `GET /api/ad-reports/compare` | 无产品/campaign 对比 |
| `GET /api/ad-reports/cross-user` | 无跨用户 CPI 对比 |
| `GET/POST /api/ad-reports/multi-analysis` | 无多维散点图分析/Pearson 相关/规则引擎 |
| `POST /api/ad-reports/multi-ai-chat` | 无多轮 AI 对话 |
| `GET /api/ad-reports/dates` | 无日期标记 |
| `POST /api/ad-reports/save` 聚合+upsert+自动MCC关联 | Java 版是简单的 insert |

**修复方案**: 这些是最高价值的业务功能，需要按 Python 源码逐个实现。`multi-analysis` 和 `multi-ai-chat` 是最复杂的两个。

### 🟡 P1-6: YouTube 视频管理功能缺失

| 缺失功能 |
|---------|
| 视频导入缺少爬取解析引擎（Python 从 URL 爬取视频信息，Java 直接拿前端字段 insert） |
| 缺少 scope/public-private 筛选、日期范围、uploader 筛选、channel_name 模糊搜索 |
| 缺少维度统计汇总（by region/frame_type/effectiveness 等计数） |
| 缺少批量编辑、批量删除 |
| 缺少消耗记录的编辑功能 |
| 缺少消耗日期统计端点 |
| 标签体系完全不同（Python 结构化分类 vs Java 简单 KV） |
| 缺少频道补全（backfill-channels） |
| 缺少资产关联端点（asset-products、product-assets） |

**修复方案**: 
1. 视频导入加入爬取逻辑（调用 yt-dlp 或 YouTube API）
2. 列表查询补充所有筛选参数和统计汇总
3. 标签体系对齐 Python 的五维分类模型
4. 补充批量操作

### 🟡 P1-7: FB 报告 Sheet 同步体系不完整

Python 有完整的闭环：保存时异步写 Sheets → 记录同步日志(成功/失败) → 前端查同步状态 → 失败重试(实际调 Sheets API)。

Java 缺失：
- `GET /api/fb/reports/last-sync` — 查询最近同步状态
- `GET /api/fb/reports/sync-status` — 按产品查同步失败记录
- `POST /api/fb/reports/retry-sync` — Java 版只改日志状态为 pending，**无实际 Sheets 写入**

**修复方案**: 补全 3 个接口，retry-sync 补充实际调用 Google Sheets API 写入。

### 🟡 P1-8: FB 产品创建/更新缺少关联数据

创建和更新接口不支持传入 `bm_ids`/`runner_ids`/`lines` 关联数组，前端需要多次分开调用：

| 缺失参数 | 影响 |
|---------|------|
| `bm_ids` | 需额外调用 `/api/fb/product-bms/*` |
| `runner_ids` | 需额外调用产品 runner 管理 |
| `lines` | 需额外调用 `/api/fb/lines/create` |

**修复方案**: `FbProductController.create/update` 支持一次性传入关联数组。

### 🟡 P1-9: FB 账户创建/更新缺少 bm_ids 和 acquired_date

- 创建/更新不支持 `bm_ids` 一次性关联 BM
- 创建不支持 `acquired_date`（Java 硬编码为当天）

---

## 4. 中优先级 — 功能缺失（P2）

### 🟡 P2-1: 完全缺失的模块

| 模块 | 计划 |
|------|------|
| **Fonts 字体管理** (7个接口) | 需要从零实现：字体导入/上传/预览/文件服务/使用标记 |
| **Audit 审计日志** (2个接口) | 需要从零实现：日志列表 + 从快照恢复产品/包 |
| **Delist 掉包检测引擎** | 需要实现 Google Play 爬取 + Telegram 通知 + dismiss 机制 |

### 🟡 P2-2: 抓取管理（Scrape）

Java 版仅有缓存记录 CRUD，缺少实际的 Google Play 爬取引擎：
- 截图爬取 + 下载
- Logo 提取
- 图片处理（PNG 转换、Google Ads 规格放大）
- ZIP 打包下载
- 图片上传

### 🟡 P2-3: AI 视频生成

Java 版仅支持单张图片 → 单视频生成，Python 版支持多图并行 + FFmpeg 合成一体化流水线。

### 🟡 P2-4: MCC 管理

缺少:
- 树形祖先补全逻辑
- 循环引用检测
- 子MCC/账户/产品关联检测
- 批量删除
- Link 机制
- MCC 变更历史

---

## 5. 低优先级 — 改进项（P3）

### 🔵 P3-1: 响应格式兼容

Python 的 `ok()` 函数对 dict 做展平：`{success: true, accounts: [...], total: N}`。Java 的 `ApiResponse<T>` 嵌套：`{success: true, data: {...}}`。分页列表 Java 用 `items`，Python 用具体字段名（`accounts`, `products` 等）。

**方案**: 保持 Java 当前格式不变。前端已经在对接 Java 版时适配了新格式（`api/client.js` 的 axios 拦截器处理 `ApiResponse`），字段名差异由前端 store 层适配。

### 🔵 P3-2: Auth 细节差异

- Java `GET /api/auth/me` 缺少 custom_name、email、telegram_username 返回字段
- Java `GET /api/auth/names` 无角色过滤（应过滤 hidden，非 developer 不显示 developer）
- Python 有 `GET /api/auth/custom-name` 和 `GET /api/auth/email` 的 GET 端点，Java 仅有 PUT

### 🔵 P3-3: 其他缺失接口

| 接口 | 功能 |
|------|------|
| `POST /api/video/scan-dir` | 扫描图片目录 |
| `POST /api/video/history/delete` | 删除视频生成历史 |
| `POST /api/video/next-filename` | 文件名冲突检测 |
| `POST /api/browse-file` / `POST /api/browse-folder` / `POST /api/browse-save` | 文件浏览/保存 |
| `POST /api/translate` | 翻译接口 |
| `POST /api/google-ads/accounts` / `POST /api/google-ads/report` | Google Ads API 直接调用 |

---

## 6. 修复实施计划

### Phase A: 阻塞项修复（预计 5-7 天）

| 任务 | 优先级 | 涉及文件 |
|------|--------|---------|
| A1. 重写 GG 做表列映射 + 公式列 + 格式化 | P0-1 | `ZuobiaoRow.java`, `GoogleSheetsService.java`, `GoogleSheetsController.java` |
| A2. 全局权限控制（owner_id 隔离） | P0-2 | 全部 Controller/Service |
| A3. Option 数据隔离 + 删除保护 | P0-3 | `OptionController.java`, `OptionServiceImpl.java` |
| A4. 产品软删除 + 审计日志 + 恢复 | P0-4 | `ProductController.java`, 新增 AuditLog 写入逻辑 |

### Phase B: 核心业务补充（预计 10-14 天）

| 任务 | 优先级 | 涉及文件 |
|------|--------|---------|
| B1. 账户管理完整逻辑（清账/MCC历史/状态追踪/批量创建/归属转移） | P1-2 | `AccountController.java`, `AccountServiceImpl.java` |
| B2. 产品管理完整逻辑（合并/导入/runner共享/MCC权限链） | P1-3 | `ProductController.java`, `ProductServiceImpl.java` |
| B3. Google Sheets 全流程（DB同步/异步写入/重试/状态查询/看板同步） | P1-4 | `GoogleSheetsService.java`, 新增 `SheetsSyncService.java` |
| B4. 广告报告分析系统（仪表盘/趋势/对比/多维分析/AI对话） | P1-5 | `AdReportController.java`, 新增 `AdReportAnalysisService.java` |
| B5. YouTube 视频完整功能（爬取导入/多维筛选/统计/批量/标签体系） | P1-6 | `YoutubeController.java` |
| B6. 路由兼容（双注册新旧路径） | P1-1 | 各 Controller |

### Phase C: 缺失模块补充（预计 5-7 天）

| 任务 | 优先级 | 涉及文件 |
|------|--------|---------|
| C1. 字体管理（7个接口从零实现） | P2-1 | 新增 `FontController.java`, `FontService.java` |
| C2. 审计日志 + 恢复 | P2-1 | 新增 `AuditController.java`, 审计写入逻辑 |
| C3. 掉包检测引擎（爬虫+通知+dismiss） | P2-1 | 重写 `DelistController.java`, 新增爬虫模块 |
| C4. 抓取爬取引擎 | P2-2 | 重写 `ScrapeController.java` |

### Phase D: FB 模块修复（预计 3-5 天）

| 任务 | 优先级 | 涉及文件 |
|------|--------|------|
| D1. ban-and-migrate 重写（账户迁移+history+产品关联检查） | P0-4 | `FbBmController.java`, `FbServiceImpl.java` |
| D2. unified 补充 UNION 逻辑 | P0-4 | `FbBmController.java`, `FbServiceImpl.java` |
| D3. 补全 deleted/restore/permanent/bm-history 5个接口 | P0-5 | `FbAccountController.java`, `FbProductController.java` |
| D4. FB 产品软删除 + 创建/更新支持关联数据 | P0-5, P1-8, P1-9 | `FbProductController.java` |
| D5. sorted_mode + cost 取值修正 | P0-6 | `FbExtractService.java` |
| D6. 报告 Sheet 同步闭环（last-sync/sync-status/retry-sync） | P1-7 | `FbAdReportController.java`, `GoogleSheetsService.java` |

### Phase E: 改进项（预计 2-3 天）

| 任务 | 优先级 |
|------|--------|
| E1. Auth 接口补充字段 | P3 |
| E2. 视频/AI/音频缺失接口补充 | P3 |
| E3. 工具类接口补充（browse/translate/google-ads） | P3 |
| E4. MCC 管理增强（树形/循环检测/关联检测） | P2 |

---

## 附录 A: FB 平台审查结果摘要

> 已完成，详见 P0-4/P0-5/P0-6/P1-7/P1-8/P1-9 及 Phase D 计划。FB 模块同样存在大量严重差异，总体匹配度约 30%。

### FB 各子模块匹配度

| 子模块 | Python接口数 | Java接口数 | ✅一致 | ⚠️差异 | 🚫缺失/不完整 | 🔴阻塞 |
|--------|-------------|-----------|--------|--------|--------------|--------|
| BM管理 | 7 | 8 | 2 | 2 | 3 | 2 |
| 账户管理 | 8 | 9 | 2 | 3 | 3 | 0 |
| 产品管理 | 8 | 4 | 0 | 2 | 2 | 1 |
| 线名管理 | 3 | 3 | 2 | 1 | 0 | 0 |
| 像素BM | 7 | 4 | 2 | 2 | 3 | 0 |
| 像素 | 3 | 3 | 1 | 1 | 1 | 0 |
| 数据提取 | 3 | 3 | 0 | 3 | 0 | 1 |
| 报告 | 9 | 6 | 2 | 2 | 3 | 1 |
| 用户查询 | 1 | 1 | 0 | 1 | 0 | 0 |

## 附录 B: 总差异统计

| 类别 | 数量 |
|------|------|
| 🔴 阻塞项 (P0) | 7 |
| 🟡 高优先级 (P1) | 9 |
| 🟡 中优先级 (P2) | 4 |
| 🔵 低优先级 (P3) | 3 |
| **Java 完全缺失的 Python 接口** | **~45 个** |
| **Java 有但为空壳/完全不同实现的** | **~30 个** |
