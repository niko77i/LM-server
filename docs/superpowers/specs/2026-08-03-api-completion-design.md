# Phase 6: API 补完设计文档

> **日期**: 2026-08-03  
> **目标**: 按迁移设计文档补完剩余 ~80 API  
> **依赖**: Phase 1-5 基础设施已完成

---

## 待补完模块分析

### 第一批: 简单 CRUD（直接对接 Mapper，工作量小）

| 模块 | 接口 | 数量 | 说明 |
|------|------|------|------|
| Font | 字体文件下载预览 | 4 | 静态资源服务 + 上传/删除增强 |
| Packages | 产品包 CRUD | 4 | list/create/update/delete |
| ProductRunners | 在跑人员管理 | 3 | 关联表追加/移除 |
| AccountMccHistory | MCC变更历史查询 | 1 | 按账户ID查询 |
| RechargeRecords | 按账户查充值 | 1 | 已有list,需支持accountId筛选 |
| ProductAssets | 产品素材关联 | 2 | 关联/解绑 |
| SheetsSyncLog | 同步日志查询 | 1 | 按用户查询 |
| ImportHistory | 导入历史 | 1 | 查询列表 |

### 第二批: 增强功能（需额外逻辑）

| 模块 | 接口 | 数量 | 说明 |
|------|------|------|------|
| Account | 恢复软删除/lookup/MCC历史 | 4 | 已实现软删,需恢复 |
| AdReport | 导出CSV/去重分析 | 4 | 流式导出+统计 |
| YouTube | 频道管理/导出 | 4 | 频道筛选+CSV |
| FbReport | 统计/导出/Sheet同步重试 | 6 | 聚合查询 |
| FbBm | 统一列表含Pixel BM | 1 | 联表查询 |
| Admin | 数据导入触发/更多管理 | 3 | 管理员专用 |

### 第三批: 复杂集成（依赖外部API）

| 模块 | 接口 | 数量 | 说明 |
|------|------|------|------|
| Account | Sheet同步 | 3 | 依赖 Google Sheets API |
| AdReport | AI对话 | 2 | 依赖 AI API |
| Video | 音乐管理/视频下载 | 4 | 文件管理 |

---

## 实现策略

- 第一批 17 个接口: Controller 直接注入 Mapper,简单 CRUD,无 Service 层
- 第二批 22 个接口: 需要 Service 方法,含查询逻辑
- 第三批 9 个接口: 占位实现,标记 TODO 和所需依赖

## 响应格式

所有接口沿用 `ApiResponse<T>` / `PagedResponse<T>`，与前端完全兼容。

## 优先级

1. 第一批 (17) — 1-2小时
2. 第二批 (22) — 2-3小时
3. 第三批 (9) — 占位即可

确认后开始实现第一批？
