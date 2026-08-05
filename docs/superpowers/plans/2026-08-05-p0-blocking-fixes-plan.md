# LM-Server P0 阻塞项修复计划

> **日期**: 2026-08-05  
> **基于**: `docs/superpowers/specs/2026-08-05-lmserver-gap-analysis-design.md`  
> **范围**: Phase A — 7 个 P0 阻塞项

---

## 任务列表

| # | 任务 | 涉及文件 | 预计 |
|---|------|---------|------|
| A1 | 重写 GG 做表列映射 + 公式列 + 格式化 | ZuobiaoRow, GoogleSheetsService, GoogleSheetsController | 2天 |
| A2 | 全局权限控制（owner_id 隔离） | 全部 Controller/Service | 1天 |
| A3 | Option 数据隔离 + 删除保护 | OptionController, OptionServiceImpl | 0.5天 |
| A4 | GG 产品软删除 + 审计日志 | ProductController, AuditController | 0.5天 |
| A5 | FB ban-and-migrate 重写 + unified UNION | FbBmController, FbServiceImpl | 1天 |
| A6 | FB 补全 5 个缺失接口 + 产品软删除 | FbAccountController, FbProductController | 0.5天 |
| A7 | FB 数据提取 sorted_mode + cost 修正 | FbExtractService | 0.5天 |

---

## A1: 重写 GG 做表

### 步骤
1. 重写 ZuobiaoRow — 对齐 Python 14 列映射
2. 重写 toSheetRow() — M/N 列写入公式
3. 重写 GoogleSheetsService.upsertZuobiao — 去重三元组 + 养户逻辑 + 格式化
4. 修改 GoogleSheetsController — 请求体字段名对齐 Python

## A2: 全局权限控制

### 步骤
1. 创建 @CheckOwner 注解 + AOP 切面
2. 所有 delete/update 操作加 owner_id WHERE 条件
3. sync-from-sheet 的 spreadsheet_id 从服务端读取

## A3: Option 数据隔离

### 步骤
1. agents/mcc_levels list/create/update/delete 加 owner_id 过滤
2. statuses/sales_persons/regions 加 platform 过滤
3. 删除前检查外键引用

## A4: GG 产品软删除

### 步骤
1. DELETE 改为设 is_archived=1 + deleted_at
2. 新增 restore + permanent 接口
3. 删除时写 audit_log

## A5: FB ban-migrate + unified

### 步骤
1. 重写 banAndMigrate — 目标BM创建 + 账户迁移 + history + 产品关联检查
2. 重写 unified — UNION fb_bms + fb_pixel_bms

## A6: FB 缺失接口

### 步骤
1. 补全 deleted/restore/permanent/bm-history 接口
2. 产品删除改为软删除(is_archived)

## A7: FB 数据提取

### 步骤
1. 补全 sorted_mode 解析
2. cost 取值改为 max
