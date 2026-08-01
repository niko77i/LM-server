package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.RechargeRecords;
import com.lmserver.mapper.gg.RechargeRecordsMapper;
import com.lmserver.service.RechargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeServiceImpl implements RechargeService {

    private final RechargeRecordsMapper mapper;

    @Override
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<RechargeRecords> list(Long userId, int page, int size, String accountId) {
        var qw = new LambdaQueryWrapper<RechargeRecords>().eq(RechargeRecords::getCreatedBy, userId);
        if (accountId != null && !accountId.isBlank()) qw.eq(RechargeRecords::getAccountId, accountId);
        qw.orderByDesc(RechargeRecords::getCreatedAt);
        var pg = mapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @Override
    /** 新增记录 — 返回创建后的完整对象 */
    public RechargeRecords create(Long userId, String accountId, String amount, String operator, String status, Long agentId) {
        RechargeRecords r = new RechargeRecords(); r.setAccountId(accountId); r.setAmount(amount);
        r.setCreatedBy(userId); r.setOperator(operator != null ? operator : "");
        r.setStatus(status != null ? status : ""); r.setAgentId(agentId); r.setSheetsSynced(0L);
        r.setCreatedAt(LocalDateTime.now()); mapper.insert(r); return r;
    }

    @Override
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public RechargeRecords update(Long id, String amount, String status, String operator) {
        RechargeRecords r = mapper.selectById(id); if (r == null) return null;
        if (amount != null) r.setAmount(amount);
        if (status != null) r.setStatus(status);
        if (operator != null) r.setOperator(operator);
        mapper.updateById(r); return r;
    }
    /** 删除记录 */
    @Override public void delete(Long id) { mapper.deleteById(id); }
}
