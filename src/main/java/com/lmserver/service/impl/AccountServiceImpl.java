package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.dto.response.SyncResult;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.mapper.gg.AccountsMapper;
import com.lmserver.service.AccountService;
import com.lmserver.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountsMapper accountsMapper;

    @Override
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<Accounts> list(Long ownerId, int page, int size, String search, Long statusId, Long mccId, Long agentId) {
        var qw = new LambdaQueryWrapper<Accounts>().eq(Accounts::getOwnerId, ownerId).isNull(Accounts::getDeletedAt);
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(Accounts::getName, search).or().like(Accounts::getAccountId, search));
        if (statusId != null) qw.eq(Accounts::getStatusId, statusId);
        if (mccId != null) qw.eq(Accounts::getMccId, mccId);
        if (agentId != null) qw.eq(Accounts::getAgentId, agentId);
        qw.orderByDesc(Accounts::getCreatedAt);
        var pg = accountsMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }
    /** 按 ID 查询 — 返回单条记录 */
    @Override public Accounts getById(Long id) { return accountsMapper.selectById(id); }

    @Override
    /** 新增记录 — 返回创建后的完整对象 */
    public Accounts create(Long ownerId, String name, String accountId, Long mccId, Long agentId, Long statusId, String tz) {
        Accounts a = new Accounts(); a.setName(name); a.setAccountId(accountId); a.setOwnerId(ownerId);
        a.setMccId(mccId); a.setAgentId(agentId); a.setStatusId(statusId); a.setTimezone(tz != null ? tz : "");
        a.setAcquiredDate(LocalDate.now()); a.setCreatedAt(LocalDateTime.now()); a.setUpdatedAt(LocalDateTime.now());
        accountsMapper.insert(a); return a;
    }

    @Override
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public Accounts update(Long id, String name, Long mccId, Long agentId, Long statusId, String tz) {
        Accounts a = accountsMapper.selectById(id); if (a == null) return null;
        if (name != null) a.setName(name);
        if (mccId != null) a.setMccId(mccId);
        if (agentId != null) a.setAgentId(agentId);
        if (statusId != null) a.setStatusId(statusId);
        if (tz != null) a.setTimezone(tz);
        a.setUpdatedAt(LocalDateTime.now()); accountsMapper.updateById(a); return a;
    }
    /** 删除记录 */
    @Override public void delete(Long id) {
        Accounts a = accountsMapper.selectById(id);
        if (a != null) { a.setDeletedAt(LocalDateTime.now()); accountsMapper.updateById(a); }
    }
    /** 获取下拉选项 — 返回 id + name 的简略列表 */
    @Override public List<Accounts> options(Long ownerId) {
        return accountsMapper.selectList(new LambdaQueryWrapper<Accounts>().eq(Accounts::getOwnerId, ownerId));
    }

    /** v1.4 清账: 状态变为非存活时，检查并插入清账记录 */
    @Override public void tryClearAccount(Long accountId, Long operatorId, String newStatus) {
        Accounts a = accountsMapper.selectById(accountId);
        if (a == null) return;
        // 只有从"存活"变更为非存活状态时才清账
        if ("存活".equals(newStatus) || a.getStatusId() == null) return;
        String acctId = a.getAccountId();
        log.info("v1.4 清账检查: 账户{} 状态变更, accountId={}", accountId, acctId);
        var uncleared = rechargeService.findUnclearedByAccount(acctId);
        if (!uncleared.isEmpty()) {
            rechargeService.insertClearRecord(acctId, operatorId);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.lmserver.service.impl.RechargeServiceImpl rechargeService;

    @org.springframework.beans.factory.annotation.Autowired
    private GoogleSheetsService sheetsService;

    /** v1.5 Sheet双向同步 */
    @Override public SyncResult syncFromSheet(Long userId, String spreadsheetId, boolean dryRun) {
        List<Map<String, Object>> toCreate = new ArrayList<>();
        List<Map<String, Object>> toUpdate = new ArrayList<>();
        List<Map<String, Object>> unchanged = new ArrayList<>();
        int created = 0, updated = 0;

        try {
            // 1. 读取 Sheet 数据 (A-H列: 名称|账户ID|时区|代理|状态|备注|获取日期|是否解绑)
            var rows = sheetsService.read(spreadsheetId, "A:H");
            if (rows == null || rows.isEmpty()) return SyncResult.builder().dryRun(dryRun).build();

            // 2. 解析 Sheet 行
            for (var row : rows) {
                if (row.size() < 2 || row.get(0) == null || row.get(1) == null) continue;
                String name = row.get(0).toString().trim();
                String accountId = row.get(1).toString().trim();
                String unbind = row.size() > 7 && row.get(7) != null ? row.get(7).toString().trim() : "";

                // 跳过 H列="解绑" 的行
                if ("解绑".equals(unbind)) continue;
                if (name.isEmpty() || accountId.isEmpty()) continue;

                Map<String, Object> sheetRecord = new HashMap<>();
                sheetRecord.put("name", name); sheetRecord.put("account_id", accountId);

                // 3. 查找 DB 中是否有该账户
                var existing = accountsMapper.selectList(new LambdaQueryWrapper<Accounts>()
                        .eq(Accounts::getAccountId, accountId));
                if (existing.isEmpty()) {
                    toCreate.add(sheetRecord);
                } else {
                    sheetRecord.put("db_id", existing.get(0).getId());
                    toUpdate.add(sheetRecord);
                }
            }

            // 4. dry_run 只返回差异
            if (dryRun) {
                return SyncResult.builder().toCreate(toCreate).toUpdate(toUpdate)
                        .unchanged(unchanged).dryRun(true).build();
            }

            // 5. 执行创建+更新
            for (var r : toCreate) {
                create(userId, (String) r.get("name"), (String) r.get("account_id"), null, null, null, "");
                created++;
            }
            for (var r : toUpdate) {
                update(lng(r.get("db_id")), (String) r.get("name"), null, null, null, null);
                updated++;
            }

        } catch (Exception e) {
            log.error("v1.5 Sheet同步失败: {}", e.getMessage());
        }
        return SyncResult.builder().toCreate(toCreate).toUpdate(toUpdate).unchanged(unchanged)
                .created(created).updated(updated).dryRun(false).build();
    }

    private Long lng(Object v) { return v != null ? Long.valueOf(v.toString()) : null; }
}
