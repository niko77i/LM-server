package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.mapper.gg.AccountsMapper;
import com.lmserver.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountsMapper accountsMapper;

    @Override
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

    @Override public Accounts getById(Long id) { return accountsMapper.selectById(id); }

    @Override
    public Accounts create(Long ownerId, String name, String accountId, Long mccId, Long agentId, Long statusId, String tz) {
        Accounts a = new Accounts(); a.setName(name); a.setAccountId(accountId); a.setOwnerId(ownerId);
        a.setMccId(mccId); a.setAgentId(agentId); a.setStatusId(statusId); a.setTimezone(tz != null ? tz : "");
        a.setAcquiredDate(LocalDate.now()); a.setCreatedAt(LocalDateTime.now()); a.setUpdatedAt(LocalDateTime.now());
        accountsMapper.insert(a); return a;
    }

    @Override
    public Accounts update(Long id, String name, Long mccId, Long agentId, Long statusId, String tz) {
        Accounts a = accountsMapper.selectById(id); if (a == null) return null;
        if (name != null) a.setName(name);
        if (mccId != null) a.setMccId(mccId);
        if (agentId != null) a.setAgentId(agentId);
        if (statusId != null) a.setStatusId(statusId);
        if (tz != null) a.setTimezone(tz);
        a.setUpdatedAt(LocalDateTime.now()); accountsMapper.updateById(a); return a;
    }

    @Override public void delete(Long id) {
        Accounts a = accountsMapper.selectById(id);
        if (a != null) { a.setDeletedAt(LocalDateTime.now()); accountsMapper.updateById(a); }
    }

    @Override public List<Accounts> options(Long ownerId) {
        return accountsMapper.selectList(new LambdaQueryWrapper<Accounts>().eq(Accounts::getOwnerId, ownerId));
    }
}
