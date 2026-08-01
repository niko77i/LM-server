# Phase 3: JPA Entity + Repository 层

> **日期**: 2026-08-01  
> **状态**: ✅ 完成

---

## Entity 层 — 40 个实体类

| 模块 | 数量 | 表 |
|------|------|-----|
| `entity/common` | 11 | Users, Config, Tags, AuditLog, Agents, AccountStatuses, MccLevels, SalesPersons, Regions, ImportHistory, Copywritings |
| `entity/gg` | 18 | Accounts, Mcc, Products, Packages, ProductRunners*, ProductAssets, RechargeRecords, AdReports, SheetsSyncLog, AccountMccHistory, ScrapeCache, Videos*, VideoHistory, VideoTasks, VideoConsumption, AudioReplaceHistory, DelistChecks, DelistNotifications |
| `entity/fb` | 11 | FbBms, FbAccounts, FbAccountBm, FbAccountBmHistory, FbProducts, FbProductRunners*, FbProductBms, FbPixelBms, FbPixels, FbLines, FbAdReports |

\* 复合主键，使用 @IdClass

### 技术要点

- JPA 注解：@Entity, @Table, @Id, @GeneratedValue, @Column
- 复合主键：ProductRunners, FbProductRunners, Videos（@IdClass）
- Java 保留字处理：`package` → `pkg`
- 类型映射：BIGINT→Long, VARCHAR→String, DATETIME→LocalDateTime, TINYINT→Boolean
- Lombok：@Data + @NoArgsConstructor + @AllArgsConstructor

## Repository 层 — 40 个接口

```java
@Repository
public interface XxxRepository extends JpaRepository<XxxEntity, Long> {}
```

复合主键 Repository 使用 IdClass 作为 ID 类型：

```java
public interface ProductRunnersRepository extends JpaRepository<ProductRunners, ProductRunnersId> {}
```

## 编译验证

```
mvn compile → BUILD SUCCESS (109 source files)
ddl-auto: validate → Hibernate 验证实体与 MySQL 表匹配 ✅
```
