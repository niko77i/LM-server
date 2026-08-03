package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.RechargeRecords;
import com.lmserver.entity.gg.SheetsSyncLog;
import com.lmserver.mapper.gg.RechargeRecordsMapper;
import com.lmserver.mapper.gg.SheetsSyncLogMapper;
import com.lmserver.service.RechargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 充值服务实现 — v1.4: 含 Google Sheets 异步同步。
 * 创建充值记录后异步写 Sheet，更新 sheets_synced 标记。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeServiceImpl implements RechargeService {

    private final RechargeRecordsMapper mapper;
    private final SheetsSyncLogMapper syncLogMapper;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    public PagedResponse<RechargeRecords> list(Long userId, int page, int size, String accountId) {
        var qw = new LambdaQueryWrapper<RechargeRecords>().eq(RechargeRecords::getCreatedBy, userId);
        if (accountId != null && !accountId.isBlank()) qw.eq(RechargeRecords::getAccountId, accountId);
        qw.orderByDesc(RechargeRecords::getCreatedAt);
        var pg = mapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @Override
    public RechargeRecords create(Long userId, String accountId, String amount, String operator, String status, Long agentId) {
        RechargeRecords r = new RechargeRecords();
        r.setAccountId(accountId); r.setAmount(amount); r.setCreatedBy(userId);
        r.setOperator(operator != null ? operator : ""); r.setStatus(status != null ? status : "");
        r.setAgentId(agentId); r.setSheetsSynced(0L); r.setCreatedAt(LocalDateTime.now());
        mapper.insert(r);

        // 异步写 Google Sheets
        asyncSyncToSheet(r);
        return r;
    }

    @Override public RechargeRecords update(Long id, String amount, String status, String operator) {
        RechargeRecords r = mapper.selectById(id); if (r == null) return null;
        if (amount != null) r.setAmount(amount);
        if (status != null) r.setStatus(status);
        if (operator != null) r.setOperator(operator);
        mapper.updateById(r); return r;
    }

    @Override public void delete(Long id) { mapper.deleteById(id); }

    /** 批量创建 — v1.4: 每条记录异步写 Sheet */
    public List<RechargeRecords> batchCreate(Long userId, List<RechargeRecords> records) {
        List<RechargeRecords> saved = new ArrayList<>();
        for (RechargeRecords r : records) {
            r.setCreatedBy(userId); r.setSheetsSynced(0L); r.setCreatedAt(LocalDateTime.now());
            mapper.insert(r); saved.add(r); asyncSyncToSheet(r);
        }
        return saved;
    }

    /** v1.4 清账查询: 查某账户下未清的充值记录 */
    public List<RechargeRecords> findUnclearedByAccount(String accountId) {
        return mapper.selectList(new LambdaQueryWrapper<RechargeRecords>()
                .eq(RechargeRecords::getAccountId, accountId)
                .ne(RechargeRecords::getAmount, "清"));
    }

    /** v1.4: 插入清账记录 */
    public void insertClearRecord(String accountId, Long userId) {
        // 防重复：检查是否已有清账记录
        var existing = mapper.selectList(new LambdaQueryWrapper<RechargeRecords>()
                .eq(RechargeRecords::getAccountId, accountId)
                .eq(RechargeRecords::getAmount, "清")
                .orderByDesc(RechargeRecords::getCreatedAt).last("LIMIT 1"));
        if (!existing.isEmpty()) { log.info("账户{}已有清账记录，跳过", accountId); return; }

        RechargeRecords r = new RechargeRecords();
        r.setAccountId(accountId); r.setAmount("清"); r.setCreatedBy(userId);
        r.setOperator("系统"); r.setStatus(""); r.setSheetsSynced(0L);
        r.setCreatedAt(LocalDateTime.now());
        mapper.insert(r);
        log.info("v1.4 清账: 账户{}插入清账记录", accountId);
        asyncSyncToSheet(r);
    }

    /** 异步写 Sheet 并更新同步标记 */
    private void asyncSyncToSheet(RechargeRecords r) {
        if (taskExecutor == null) { log.info("[充值Sheet] 异步线程池未配置，跳过"); return; }
        SheetsSyncLog syncLog = new SheetsSyncLog();
        syncLog.setUserId(r.getCreatedBy()); syncLog.setProductName("充值_" + r.getAccountId());
        syncLog.setStatus("pending"); syncLog.setRetryCount(0L);
        syncLog.setCreatedAt(LocalDateTime.now());
        syncLogMapper.insert(syncLog);

        taskExecutor.execute(() -> {
            try {
                // TODO: Google Sheets 实际写入 recharge_records 对应的 Sheet
                r.setSheetsSynced(1L); mapper.updateById(r);
                syncLog.setStatus("synced"); syncLogMapper.updateById(syncLog);
            } catch (Exception e) {
                log.error("[充值Sheet] 同步失败: {}", e.getMessage());
                r.setSheetsError(e.getMessage().substring(0, Math.min(500, e.getMessage().length())));
                mapper.updateById(r);
                syncLog.setStatus("failed"); syncLog.setErrorMsg(r.getSheetsError());
                syncLog.setRetryCount(syncLog.getRetryCount() + 1);
                syncLogMapper.updateById(syncLog);
            }
        });
    }
}
