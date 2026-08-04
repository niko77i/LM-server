# LM-Server API 文档

> 自动生成于 2026-08-03 | 基础路径: http://localhost:8080

## 接口总览

| 模块 | 接口数 |
|------|--------|
| AccountController | 16 |
| AccountMccHistoryController | 1 |
| AdReportController | 10 |
| AdminController | 3 |
| AdminDataController | 2 |
| AdminTriggerController | 3 |
| AuditController | 2 |
| AuthController | 12 |
| ConfigController | 4 |
| CopywritingController | 5 |
| DataController | 4 |
| DelistController | 2 |
| FbAccountBmController | 3 |
| FbAccountController | 5 |
| FbAdReportController | 6 |
| FbBmController | 8 |
| FbExtractController | 3 |
| FbLinesController | 4 |
| FbPixelController | 7 |
| FbProductController | 6 |
| FontController | 6 |
| GoogleAdsController | 3 |
| GoogleSheetsController | 5 |
| HealthController | 1 |
| ImportHistoryController | 1 |
| MccController | 6 |
| OptionController | 4 |
| PackageController | 4 |
| ProductAssetController | 3 |
| ProductController | 10 |
| ProductRunnerController | 3 |
| RechargeController | 5 |
| ScrapeController | 4 |
| SettingsController | 2 |
| SheetsSyncLogController | 2 |
| UtilityController | 4 |
| VideoController | 13 |
| YoutubeController | 11 |

**总计: 193 个接口**

---
## AccountController

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/accounts/list` | - |
| Get | `/api/accounts/{id}` | - |
| Get | `/api/accounts/deleted` | - |
| Get | `/api/accounts/options` | - |
| Get | `/api/accounts/lookup` | - |
| Get | `/api/accounts/recharge-records` | - |
| Get | `/api/accounts/mcc-history` | - |
| Post | `/api/accounts/create` | - |
| Post | `/api/accounts/{id}/restore` | - |
| Post | `/api/accounts/batch-delete` | - |
| Post | `/api/accounts/batch-update` | - |
| Post | `/api/accounts/sync-from-sheet` | - |
| Post | `/api/accounts/batch-lookup` | - |
| Put | `/api/accounts/{id}` | - |
| Delete | `/api/accounts/{id}` | - |
| Delete | `/api/accounts/{id}/permanent` | - |

---
## AccountMccHistoryController
> 账户 MCC 变更历史 — /api/account-mcc-history/*。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/account-mcc-history/list` | - |

---
## AdReportController
> GG 广告报告控制器 — /api/ad-reports/*，GG广告投放数据的CRUD

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/ad-reports/list` | - |
| Get | `/api/ad-reports/export` | - |
| Get | `/api/ad-reports/stats` | - |
| Get | `/api/ad-reports/products` | - |
| Post | `/api/ad-reports/create` | - |
| Post | `/api/ad-reports/batch-delete` | - |
| Post | `/api/ad-reports/analysis` | - |
| Post | `/api/ad-reports/dedup-check` | - |
| Put | `/api/ad-reports/{id}` | - |
| Delete | `/api/ad-reports/{id}` | - |

---
## AdminController
> 管理员控制器 — /api/admin/*，用户列表/编辑/禁用，@PreAuthorize控制权限

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/admin/users` | - |
| Put | `/api/admin/users/{id}` | - |
| Delete | `/api/admin/users/{id}` | - |

---
## AdminDataController
> 管理员数据管理 — /api/admin/data/*。仅 ADMIN/DEVELOPER 角色。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/admin/data/stats` | - |
| Post | `/api/admin/data/import` | - |

---
## AdminTriggerController

| 方法 | 路径 | 说明 |
|------|------|------|
| Post | `/api/admin/trigger-cleanup` | 手动触发周清理 |
| Post | `/api/admin/trigger-delist` | 手动触发掉包检测 |
| Post | `/api/admin/trigger-test-notify` | - |

---
## AuditController
> 审计日志控制器 — /api/audit-log/*，操作审计记录的查询

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/audit-log/list` | - |
| Get | `/api/audit-log/{id}` | - |

---
## AuthController
> 认证控制器 — /api/auth/*。12 个接口完整实现。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/auth/me` | - |
| Get | `/api/auth/names` | 获取所有用户名列表（下拉选择用） |
| Get | `/api/auth/names/{id}` | - |
| Post | `/api/auth/login` | - |
| Post | `/api/auth/register` | - |
| Post | `/api/auth/refresh` | - |
| Post | `/api/auth/logout` | - |
| Put | `/api/auth/password` | 修改密码 |
| Put | `/api/auth/profile` | 修改显示名称 |
| Put | `/api/auth/custom-name` | 修改自定义名称 |
| Put | `/api/auth/email` | 修改邮箱 |
| Put | `/api/auth/telegram-username` | 修改 Telegram 用户名 |

---
## ConfigController
> 系统配置控制器 — /api/config/*，键值对配置的查询和修改

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/config/list` | - |
| Get | `/api/config/key/{key}` | - |
| Put | `/api/config/key/{key}` | - |
| Delete | `/api/config/key/{key}` | - |

---
## CopywritingController
> 文案管理控制器 — /api/copywriting/*，营销文案的CRUD+批量删除

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/copywriting/list` | - |
| Post | `/api/copywriting/create` | - |
| Post | `/api/copywriting/batch-delete` | 删除记录 |
| Put | `/api/copywriting/{id}` | - |
| Delete | `/api/copywriting/{id}` | - |

---
## DataController
> 数据导入导出控制器 — /api/data/*，用户级数据备份与恢复。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/data/export` | / |
| Get | `/api/data/export/download` | / |
| Get | `/api/data/history` | / |
| Post | `/api/data/import` | - |

---
## DelistController
> 掉包检测控制器 — /api/delist/*，查询Google Play应用下架检测结果

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/delist/checks` | - |
| Get | `/api/delist/product/{productId}` | - |

---
## FbAccountBmController
> FB 账户-BM 关联管理 — /api/fb/account-bm/*。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/fb/account-bm/list` | - |
| Post | `/api/fb/account-bm/associate` | - |
| Delete | `/api/fb/account-bm/{id}` | - |

---
## FbAccountController
> FB 账户管理控制器 — /api/fb/accounts/*，FB广告账户的CRUD+软删除

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/fb/accounts/list` | - |
| Get | `/api/fb/accounts/{id}` | - |
| Post | `/api/fb/accounts/create` | - |
| Put | `/api/fb/accounts/{id}` | - |
| Delete | `/api/fb/accounts/{id}` | - |

---
## FbAdReportController
> FB 广告报告控制器 — /api/fb/reports/*，FB广告投放数据的导入查询

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/fb/reports/list` | - |
| Get | `/api/fb/reports/stats` | - |
| Get | `/api/fb/reports/export` | - |
| Post | `/api/fb/reports/create` | - |
| Post | `/api/fb/reports/sync-retry/{logId}` | - |
| Delete | `/api/fb/reports/{id}` | 新增记录 — 返回创建后的完整对象 |

---
## FbBmController
> FB BM 管理控制器 — /api/fb/bms/*，BM的CRUD+软删除+下拉选项

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/fb/bms/list` | - |
| Get | `/api/fb/bms/{id}` | - |
| Get | `/api/fb/bms/options` | 删除记录 |
| Get | `/api/fb/bms/unified` | - |
| Post | `/api/fb/bms/create` | - |
| Post | `/api/fb/bms/{bid}/ban-and-migrate` | - |
| Put | `/api/fb/bms/{id}` | - |
| Delete | `/api/fb/bms/{id}` | - |

---
## FbExtractController
> 检查重复 — 查询已存在的记录，避免重复导入。

| 方法 | 路径 | 说明 |
|------|------|------|
| Post | `/api/fb/extract/parse` | / |
| Post | `/api/fb/extract/check-duplicates` | / |
| Post | `/api/fb/extract/save` | / |

---
## FbLinesController
> FB 广告线（落地页）控制器 — /api/fb/lines/*。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/fb/lines/list` | - |
| Post | `/api/fb/lines/create` | - |
| Put | `/api/fb/lines/{id}` | - |
| Delete | `/api/fb/lines/{id}` | - |

---
## FbPixelController
> FB Pixel 管理控制器 — /api/fb/pixels/* 和 /api/fb/pixel-bms/*。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/fb/pixel-bms/list` | - |
| Get | `/api/fb/pixels/list` | - |
| Post | `/api/fb/pixel-bms/create` | BM 列表查询 — 支持名称/ID搜索和状态筛选 |
| Post | `/api/fb/pixels/create` | - |
| Put | `/api/fb/pixel-bms/{id}` | - |
| Delete | `/api/fb/pixel-bms/{id}` | - |
| Delete | `/api/fb/pixels/{id}` | - |

---
## FbProductController
> FB 产品管理控制器 — /api/fb/products/*，FB产品的CRUD+下拉选项

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/fb/products/list` | - |
| Get | `/api/fb/products/{id}` | - |
| Get | `/api/fb/products/options` | 删除记录 |
| Post | `/api/fb/products/create` | - |
| Put | `/api/fb/products/{id}` | - |
| Delete | `/api/fb/products/{id}` | - |

---
## FontController

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/fonts/list` | - |
| Get | `/api/fonts/preview/{name}` | - |
| Get | `/api/fonts/download/{name}` | - |
| Post | `/api/fonts/upload` | - |
| Post | `/api/fonts/batch-upload` | - |
| Delete | `/api/fonts/{name}` | - |

---
## GoogleAdsController
> Google Ads 集成控制器 — /api/google-ads/*，凭证从数据库 config 表动态获取。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/google-ads/accounts` | 列出经理账户下的子账户列表 |
| Post | `/api/google-ads/report` | 拉取指定账户的广告系列报告 |
| Post | `/api/google-ads/sync` | - |

---
## GoogleSheetsController

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/google-sheets/read` | 读取 Sheet 指定范围 |
| Post | `/api/google-sheets/write` | 写入 Sheet |
| Post | `/api/google-sheets/zuobiao/upsert` | GG 做表 upsert — 14列去重/扩容/批量更新 |
| Post | `/api/google-sheets/fb-reports/upsert` | FB 做表 upsert — 12列去重/扩容/批量更新 |
| Post | `/api/google-sheets/sync-trigger` | - |

---
## HealthController
> 健康检查控制器 — GET /api/health，返回服务运行状态

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/health` | - |

---
## ImportHistoryController
> 导入历史查询 — /api/import-history/*。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/import-history/list` | - |

---
## MccController
> MCC 管理控制器 — /api/mcc/*，GG平台MCC的CRUD+下拉选项

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/mcc/list` | - |
| Get | `/api/mcc/{id}` | - |
| Get | `/api/mcc/options` | - |
| Post | `/api/mcc/create` | - |
| Put | `/api/mcc/{id}` | - |
| Delete | `/api/mcc/{id}` | - |

---
## OptionController
> 选项管理控制器 — /api/{type}/*，通过路径变量统一分发5个选项表的CRUD

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/{type}/list` | - |
| Post | `/api/{type}/create` | - |
| Put | `/api/{type}/{id}` | - |
| Delete | `/api/{type}/{id}` | - |

---
## PackageController
> 产品包管理控制器 — /api/packages/*。管理产品下的素材包/系列。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/packages/list` | - |
| Post | `/api/packages/create` | - |
| Put | `/api/packages/{id}` | - |
| Delete | `/api/packages/{id}` | - |

---
## ProductAssetController
> 产品素材关联管理 — /api/product-assets/*。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/product-assets/list` | - |
| Post | `/api/product-assets/bind` | - |
| Delete | `/api/product-assets/{id}` | - |

---
## ProductController
> 产品管理控制器 — /api/products/*，GG平台产品的完整CRUD+下拉选项

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/products/list` | - |
| Get | `/api/products/{id}/detail` | - |
| Get | `/api/products/options` | - |
| Get | `/api/products/deleted` | - |
| Post | `/api/products/create` | - |
| Post | `/api/products/batch-update` | - |
| Post | `/api/products/{id}/archive` | - |
| Post | `/api/products/{id}/unarchive` | - |
| Put | `/api/products/{id}` | - |
| Delete | `/api/products/{id}` | - |

---
## ProductRunnerController
> 产品在跑人员管理 — /api/product-runners/*。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/product-runners/list` | - |
| Post | `/api/product-runners/add` | - |
| Delete | `/api/product-runners/remove` | - |

---
## RechargeController
> 充值管理控制器 — /api/recharge/*，GG平台充值记录的CRUD

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/recharge/list` | - |
| Post | `/api/recharge/create` | - |
| Post | `/api/recharge/batch-create` | - |
| Put | `/api/recharge/{id}` | - |
| Delete | `/api/recharge/{id}` | - |

---
## ScrapeController
> 图片抓取控制器 — /api/scrape/*，Google Play截图抓取缓存管理

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/scrape/cache` | / |
| Get | `/api/scrape/cache/{packageName}` | - |
| Post | `/api/scrape/trigger` | - |
| Delete | `/api/scrape/cache/{packageName}` | - |

---
## SettingsController
> 系统设置控制器 — /api/settings/*，批量配置保存+标签管理

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/settings/tags` | - |
| Put | `/api/settings/tags/{key}` | 获取标签列表 — 返回所有标签键值对 |

---
## SheetsSyncLogController
> Sheets 同步日志查询 — /api/sheets-sync-log/*。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/sheets-sync-log/list` | - |
| Post | `/api/sheets-sync-log/retry/{id}` | - |

---
## UtilityController

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/browse` | / |
| Get | `/api/browse/file` | - |
| Get | `/api/browse/info` | - |
| Post | `/api/translate` | / |

---
## VideoController
> 视频处理控制器 — /api/video/* 和 /api/audio-replace/*，AI生成/FFmpeg合成/音频替换。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/video/providers` | 可用的 AI Provider 列表 |
| Get | `/api/video/progress/{taskId}` | 查询 AI 任务进度 |
| Get | `/api/video/history` | 获取视频生成历史配置 |
| Get | `/api/audio-replace/history` | 音频替换历史 |
| Get | `/api/video/tasks` | 视频任务列表 |
| Get | `/api/music/list` | 音乐列表 |
| Get | `/api/video/download/{taskId}` | - |
| Post | `/api/video/generate` | 提交 AI 视频生成任务 |
| Post | `/api/video/history` | 保存视频生成历史配置 |
| Post | `/api/video/compose` | 提交 FFmpeg 视频合成 |
| Post | `/api/audio-replace` | 提交音频替换任务 |
| Post | `/api/music/upload` | 上传音乐 |
| Delete | `/api/music/{name}` | 删除音乐 |

---
## YoutubeController
> YouTube 视频管理控制器 — /api/youtube/*，视频CRUD/批量导入/消耗追踪/标签配置。

| 方法 | 路径 | 说明 |
|------|------|------|
| Get | `/api/youtube/list` | - |
| Get | `/api/youtube/{id}` | - |
| Get | `/api/youtube/consumption/list` | - |
| Get | `/api/youtube/tags` | - |
| Get | `/api/youtube/export` | - |
| Post | `/api/youtube/import` | - |
| Post | `/api/youtube/consumption` | - |
| Put | `/api/youtube/{id}` | - |
| Put | `/api/youtube/tags/{key}` | - |
| Delete | `/api/youtube/consumption/{id}` | - |
| Delete | `/api/youtube/{id}` | - |
