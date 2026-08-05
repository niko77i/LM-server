package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.dto.response.SyncResult;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.entity.gg.RechargeRecords;
import com.lmserver.entity.gg.AccountMccHistory;
import com.lmserver.mapper.gg.AccountMccHistoryMapper;
import com.lmserver.mapper.gg.AccountsMapper;
import com.lmserver.mapper.gg.RechargeRecordsMapper;
import com.lmserver.service.AccountService;
import com.lmserver.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RechargeRecordsMapper rechargeRecordsMapper;
    @Autowired
    private AccountMccHistoryMapper mccHistoryMapper;
    @Autowired
    private RechargeServiceImpl rechargeService;
    @Autowired
    private GoogleSheetsService sheetsService;

    // ═══════ 查询 ═══════

    @Override
    public PagedResponse<Map<String, Object>> list(Long ownerId, int page, int size, String search, Long statusId, Long mccId, Long agentId) {
        var qw = new LambdaQueryWrapper<Accounts>().eq(Accounts::getOwnerId, ownerId).isNull(Accounts::getDeletedAt);
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(Accounts::getName, search).or().like(Accounts::getAccountId, search));
        if (statusId != null) qw.eq(Accounts::getStatusId, statusId);
        if (mccId != null) qw.eq(Accounts::getMccId, mccId);
        if (agentId != null) qw.eq(Accounts::getAgentId, agentId);
        qw.orderByDesc(Accounts::getCreatedAt);
        var pg = accountsMapper.selectPage(new Page<>(page, size), qw);

        // Batch-load related data
        var allMcc = new HashMap<Long, com.lmserver.entity.gg.Mcc>();
        var allAgents = new HashMap<Long, com.lmserver.entity.common.Agents>();
        var allStatuses = new HashMap<Long, com.lmserver.entity.common.AccountStatuses>();
        for (Accounts a : pg.getRecords()) {
            if (a.getMccId() != null && !allMcc.containsKey(a.getMccId()))
                allMcc.put(a.getMccId(), mccMapper.selectById(a.getMccId()));
            if (a.getAgentId() != null && !allAgents.containsKey(a.getAgentId()))
                allAgents.put(a.getAgentId(), agentsMapper.selectById(a.getAgentId()));
            if (a.getStatusId() != null && !allStatuses.containsKey(a.getStatusId()))
                allStatuses.put(a.getStatusId(), statusesMapper.selectById(a.getStatusId()));
        }

        // Build enriched rows
        List<Map<String, Object>> items = new ArrayList<>();
        for (Accounts a : pg.getRecords()) {
            var mcc = allMcc.get(a.getMccId());
            var agent = allAgents.get(a.getAgentId());
            var st = allStatuses.get(a.getStatusId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", a.getId()); row.put("name", a.getName()); row.put("account_id", a.getAccountId());
            row.put("mcc_id", a.getMccId()); row.put("agent_id", a.getAgentId()); row.put("status_id", a.getStatusId());
            row.put("timezone", a.getTimezone()); row.put("owner_id", a.getOwnerId());
            row.put("acquired_date", a.getAcquiredDate()); row.put("death_date", a.getDeathDate());
            row.put("status_changed_date", a.getStatusChangedDate()); row.put("created_at", a.getCreatedAt());
            row.put("deleted_at", a.getDeletedAt());
            // JOIN names
            row.put("mcc_name", mcc != null ? mcc.getName() : null);
            row.put("mcc_code", mcc != null ? mcc.getMccId() : null);
            row.put("agent", agent != null ? agent.getName() : null);
            row.put("status", st != null ? st.getName() : null);
            items.add(row);
        }

        // Build extra metadata for frontend filters
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (Accounts a : accountsMapper.selectList(
                new LambdaQueryWrapper<Accounts>().eq(Accounts::getOwnerId, ownerId).isNull(Accounts::getDeletedAt))) {
            var st = allStatuses.get(a.getStatusId());
            String name = st != null ? st.getName() : "未知";
            statusCounts.merge(name, 1L, Long::sum);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", pg.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("status_counts", statusCounts);
        result.put("mcc_options", mccMapper.selectList(
                new LambdaQueryWrapper<com.lmserver.entity.gg.Mcc>().eq(com.lmserver.entity.gg.Mcc::getOwnerId, ownerId))
                .stream().map(m -> { Map<String, Object> opt = new LinkedHashMap<>(); opt.put("id", m.getId()); opt.put("name", m.getName()); opt.put("mcc_id", m.getMccId()); return opt; }).toList());
        result.put("timezone_options", accountsMapper.selectList(
                new LambdaQueryWrapper<Accounts>().select(Accounts::getTimezone).eq(Accounts::getOwnerId, ownerId).isNull(Accounts::getDeletedAt))
                .stream().map(Accounts::getTimezone).filter(tz -> tz != null && !tz.isBlank()).distinct().sorted().toList());
        return PagedResponse.of(items, pg.getTotal(), page, size);
    }

    @Override
    public Accounts getById(Long id) {
        return accountsMapper.selectById(id);
    }

    @Override
    public List<Accounts> options(Long ownerId) {
        return accountsMapper.selectList(new LambdaQueryWrapper<Accounts>()
                .eq(Accounts::getOwnerId, ownerId).isNull(Accounts::getDeletedAt));
    }

    // ═══════ 创建 ═══════

    @Override
    public Accounts create(Long ownerId, String name, String accountId, Long mccId, Long agentId, Long statusId, String tz) {
        Accounts a = new Accounts();
        a.setName(name);
        a.setAccountId(accountId);
        a.setOwnerId(ownerId);
        a.setMccId(mccId);
        a.setAgentId(agentId);
        a.setStatusId(statusId);
        a.setTimezone(tz != null ? tz : "");
        a.setAcquiredDate(LocalDate.now());
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        accountsMapper.insert(a);

        // 记录 MCC 变更历史
        if (mccId != null) {
            AccountMccHistory h = new AccountMccHistory();
            h.setAccountId(a.getId());
            h.setNewMccId(mccId);
            h.setChangeType("create");
            h.setCreatedAt(LocalDateTime.now());
            mccHistoryMapper.insert(h);
        }
        return a;
    }

    // ═══════ 更新（含 owner_id 校验）═══════

    @Override
    public Accounts update(Long id, Long userId, String name, Long mccId, Long agentId, Long statusId, String tz) {
        Accounts a = accountsMapper.selectById(id);
        if (a == null) return null;
        if (!a.getOwnerId().equals(userId)) {
            log.warn("权限拒绝: 用户{} 尝试更新账户{} (owner={})", userId, id, a.getOwnerId());
            return null;
        }

        Long oldMccId = a.getMccId();
        if (name != null) a.setName(name);
        if (mccId != null) a.setMccId(mccId);
        if (agentId != null) a.setAgentId(agentId);
        if (tz != null) a.setTimezone(tz);

        // 状态变更追踪
        if (statusId != null && !statusId.equals(a.getStatusId())) {
            a.setStatusChangedDate(LocalDateTime.now());
            a.setStatusId(statusId);
            // 触发清账检查
            tryClearAccount(id, userId, null);
        }

        a.setUpdatedAt(LocalDateTime.now());
        accountsMapper.updateById(a);

        // MCC 变更历史
        if (mccId != null && !mccId.equals(oldMccId)) {
            AccountMccHistory h = new AccountMccHistory();
            h.setAccountId(id);
            h.setOldMccId(oldMccId);
            h.setNewMccId(mccId);
            h.setChangedBy(userId);
            h.setChangeType("manual");
            h.setCreatedAt(LocalDateTime.now());
            mccHistoryMapper.insert(h);
        }
        return a;
    }

    // ═══════ 软删除（含 owner_id 校验）═══════

    @Override
    public void delete(Long id, Long userId) {
        Accounts a = accountsMapper.selectById(id);
        if (a == null) return;
        if (!a.getOwnerId().equals(userId)) {
            log.warn("权限拒绝: 用户{} 尝试删除账户{} (owner={})", userId, id, a.getOwnerId());
            return;
        }
        a.setDeletedAt(LocalDateTime.now());
        accountsMapper.updateById(a);
        syncSheetUnbind(a.getAccountId(), "解绑");
    }

    @Override
    public void restore(Long id, Long userId) {
        Accounts a = accountsMapper.selectById(id);
        if (a == null) return;
        if (!a.getOwnerId().equals(userId)) {
            log.warn("权限拒绝: 用户{} 尝试恢复账户{} (owner={})", userId, id, a.getOwnerId());
            return;
        }
        a.setDeletedAt(null);
        accountsMapper.updateById(a);
        syncSheetUnbind(a.getAccountId(), "");
    }

    @Override
    public void permanentDelete(Long id, Long userId) {
        Accounts a = accountsMapper.selectById(id);
        if (a == null) return;
        if (!a.getOwnerId().equals(userId)) {
            log.warn("权限拒绝: 用户{} 尝试永久删除账户{} (owner={})", userId, id, a.getOwnerId());
            return;
        }
        if (a.getDeletedAt() == null) {
            log.warn("永久删除拒绝: 账户{} 未先软删除", id);
            return;
        }
        // 清理关联数据
        rechargeRecordsMapper.delete(new LambdaQueryWrapper<RechargeRecords>()
                .eq(RechargeRecords::getAccountId, a.getAccountId()));
        mccHistoryMapper.delete(new LambdaQueryWrapper<AccountMccHistory>()
                .eq(AccountMccHistory::getAccountId, id));
        accountsMapper.deleteById(id);
    }

    // ═══════ 批量操作（含 owner_id 校验）═══════

    @Override
    public int batchDelete(List<Long> ids, Long userId) {
        int count = 0;
        for (Long id : ids) {
            Accounts a = accountsMapper.selectById(id);
            if (a != null && a.getOwnerId().equals(userId) && a.getDeletedAt() == null) {
                a.setDeletedAt(LocalDateTime.now());
                accountsMapper.updateById(a);
                count++;
            }
        }
        return count;
    }

    @Override
    public int batchUpdate(List<Long> ids, Long userId, String name, Long mccId, Long agentId, Long statusId, String timezone) {
        int count = 0;
        for (Long id : ids) {
            Accounts a = accountsMapper.selectById(id);
            if (a == null || !a.getOwnerId().equals(userId)) continue;
            if (name != null) a.setName(name);
            if (mccId != null) a.setMccId(mccId);
            if (agentId != null) a.setAgentId(agentId);
            if (statusId != null) {
                if (!statusId.equals(a.getStatusId())) a.setStatusChangedDate(LocalDateTime.now());
                a.setStatusId(statusId);
            }
            if (timezone != null) a.setTimezone(timezone);
            a.setUpdatedAt(LocalDateTime.now());
            accountsMapper.updateById(a);
            count++;
        }
        return count;
    }

    // ═══════ 清账 ═══════

    @Override
    public void tryClearAccount(Long accountId, Long operatorId, String newStatus) {
        Accounts a = accountsMapper.selectById(accountId);
        if (a == null) return;
        if ("存活".equals(newStatus)) return;
        String acctId = a.getAccountId();
        var uncleared = rechargeService.findUnclearedByAccount(acctId);
        if (!uncleared.isEmpty()) {
            rechargeService.insertClearRecord(acctId, operatorId);
            log.info("清账完成: 账户{} accountId={}", accountId, acctId);
        }
    }

    // ═══════ Sheet 双向同步 — A=运营|B=账户ID|C=代理|D=国家|E=时区|F=备注|G=封户|H=解绑 ═══════

    @Override
    public SyncResult syncFromSheet(Long userId, String spreadsheetId, boolean dryRun) {
        List<Map<String, Object>> toCreate = new ArrayList<>();
        List<Map<String, Object>> toUpdate = new ArrayList<>();
        List<Map<String, Object>> unchanged = new ArrayList<>();
        int created = 0, updated = 0;

        try {
            var rows = sheetsService.read(spreadsheetId, "A:H");
            if (rows == null || rows.isEmpty()) return SyncResult.builder().dryRun(dryRun).build();

            var allAccts = accountsMapper.selectList(
                    new LambdaQueryWrapper<Accounts>().eq(Accounts::getOwnerId, userId).isNull(Accounts::getDeletedAt));
            Map<String, Accounts> existingMap = new HashMap<>();
            for (Accounts a : allAccts) {
                if (a.getAccountId() != null) existingMap.put(a.getAccountId().trim(), a);
            }

            for (var row : rows) {
                if (row.size() < 2 || row.get(1) == null) continue;
                String accountId = row.get(1).toString().trim();
                String name = row.get(0) != null ? row.get(0).toString().trim() : "";
                String agentName = row.size() > 2 && row.get(2) != null ? row.get(2).toString().trim() : "";
                String timezone = row.size() > 4 && row.get(4) != null ? row.get(4).toString().trim() : "";
                String blocked = row.size() > 6 && row.get(6) != null ? row.get(6).toString().trim() : "";
                String unbind = row.size() > 7 && row.get(7) != null ? row.get(7).toString().trim() : "";
                if ("解绑".equals(unbind)) continue;
                if (accountId.isEmpty()) continue;

                Map<String, Object> rec = new HashMap<>();
                rec.put("name", name); rec.put("account_id", accountId);
                rec.put("agent_name", agentName); rec.put("timezone", timezone); rec.put("blocked", blocked);

                if (existingMap.containsKey(accountId)) {
                    rec.put("db_id", existingMap.get(accountId).getId());
                    toUpdate.add(rec);
                } else {
                    toCreate.add(rec);
                }
            }

            if (dryRun) {
                return SyncResult.builder().toCreate(toCreate).toUpdate(toUpdate)
                        .unchanged(unchanged).dryRun(true).build();
            }

            for (var r : toCreate) {
                String an = (String) r.get("agent_name");
                Long agentId = an != null && !an.isBlank() ? resolveAgentId(an) : null;
                Long statusId = "是".equals(r.get("blocked")) || "可用".equals(r.get("blocked"))
                        ? resolveStatusId("存活") : null;
                create(userId, (String) r.get("name"), (String) r.get("account_id"), null, agentId, statusId, (String) r.get("timezone"));
                created++;
            }
            for (var r : toUpdate) {
                Accounts a = existingMap.get(r.get("account_id"));
                if (a != null && !a.getName().equals(r.get("name"))) {
                    a.setName((String) r.get("name"));
                    a.setUpdatedAt(java.time.LocalDateTime.now());
                    accountsMapper.updateById(a);
                    updated++;
                }
            }

            if (created > 0 || updated > 0) writebackSheet(spreadsheetId, new ArrayList<>(existingMap.values()));

        } catch (Exception e) {
            log.error("Sheet同步失败: {}", e.getMessage(), e);
        }
        return SyncResult.builder().toCreate(toCreate).toUpdate(toUpdate).unchanged(unchanged)
                .created(created).updated(updated).dryRun(false).build();
    }

    private void writebackSheet(String spreadsheetId, List<Accounts> accounts) {
        try {
            var rows = sheetsService.read(spreadsheetId, "A:H");
            if (rows == null) return;
            boolean changed = false;
            for (var row : rows) {
                if (row.size() < 2 || row.get(1) == null) continue;
                String accountId = row.get(1).toString().trim();
                for (Accounts a : accounts) {
                    if (accountId.equals(a.getAccountId())) {
                        while (row.size() < 8) row.add("");
                        row.set(7, a.getDeletedAt() != null ? "解绑" : "");
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) sheetsService.write(spreadsheetId, "A1", rows);
        } catch (Exception e) { log.warn("Sheet回写失败: {}", e.getMessage()); }
    }

    private Long resolveAgentId(String name) {
        if (name == null || name.isBlank()) return null;
        var existing = agentsMapper.selectList(
                new LambdaQueryWrapper<com.lmserver.entity.common.Agents>().eq(com.lmserver.entity.common.Agents::getName, name));
        if (!existing.isEmpty()) return existing.get(0).getId();
        com.lmserver.entity.common.Agents a = new com.lmserver.entity.common.Agents();
        a.setName(name); agentsMapper.insert(a);
        return a.getId();
    }

    private Long resolveStatusId(String name) {
        if (name == null || name.isBlank()) return null;
        var existing = statusesMapper.selectList(
                new LambdaQueryWrapper<com.lmserver.entity.common.AccountStatuses>().eq(com.lmserver.entity.common.AccountStatuses::getName, name));
        if (!existing.isEmpty()) return existing.get(0).getId();
        com.lmserver.entity.common.AccountStatuses s = new com.lmserver.entity.common.AccountStatuses();
        s.setName(name); s.setPlatform("gg"); statusesMapper.insert(s);
        return s.getId();
    }

    @Autowired private com.lmserver.mapper.common.AgentsMapper agentsMapper;
    @Autowired private com.lmserver.mapper.common.AccountStatusesMapper statusesMapper;
    @Autowired private com.lmserver.mapper.gg.MccMapper mccMapper;

    private Long lng(Object v) { return v != null ? Long.valueOf(v.toString()) : null; }

    /** 后台同步 Sheet H 列（"解绑" 或清空），异步执行不阻塞主流程 */
    private void syncSheetUnbind(String accountId, String unbindValue) {
        try {
            // 读取用户的 Google Sheets 配置，找到"我的看板"，更新 H 列
            // 当前简化实现：记录日志，完整实现需异步调用 sheetsService
            log.info("Sheet同步: accountId={} H列设为'{}'", accountId, unbindValue);
        } catch (Exception e) {
            log.warn("Sheet同步失败: accountId={}, error={}", accountId, e.getMessage());
        }
    }
}
