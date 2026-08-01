# Phase 2: 前端拷贝 + MySQL 数据库 + 数据迁移

> **日期**: 2026-08-01  
> **状态**: ✅ 完成

---

## 1. 前端拷贝

- 从 `GG-Server/frontend/` 完整拷贝至 `LM-Server/frontend/`
- `vite.config.js` 代理改为 `http://127.0.0.1:8080`
- 原 GG-Server 前端不受影响（仍指向 Flask 5001）

## 2. MySQL 数据库

- **MySQL 8.0.40** (D:\work\mysql\mysql-8.0.40-winx64)
- 数据库: `lmserver`，字符集 `utf8mb4`
- 表结构从 SQLite 导出并适配 MySQL 语法
- 数据从 GG-Server `temp/app.db` 导出并清洗导入

### 迁移难点

| 问题 | 处理方式 |
|------|---------|
| SQLite AUTOINCREMENT | → MySQL AUTO_INCREMENT |
| TEXT 默认值 | → VARCHAR 或去掉默认值 |
| 日期默认 `datetime('now')` | → `CURRENT_TIMESTAMP` + 列类型改为 DATETIME |
| 空字符串日期 `''` | → NULL |
| 不完整日期 `06-09 16:19` | → NULL |
| 复合主键 | → 不加 AUTO_INCREMENT，用 PRIMARY KEY(col1, col2) |
| TEXT 列太长 | → 按实际数据长度选 VARCHAR 或 TEXT |

## 3. 数据概览

| 表 | 行数 |
|------|-----|
| videos | 1961 |
| ad_reports | 1052 |
| packages | 997 |
| accounts | 300 |
| products | 97 |
| recharge_records | 101 |
| mcc | 56 |
| users | 12 |
| ... | ... |
| **总计** | **6213 行** |

## 4. 启动方式

```bash
# 确保 MySQL 已启动
# 后端
mvn spring-boot:run
# 前端
cd frontend && npm run dev
```
