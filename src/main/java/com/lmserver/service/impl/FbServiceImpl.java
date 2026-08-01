package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.*;
import com.lmserver.mapper.fb.*;
import com.lmserver.service.FbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Service interface */
@Slf4j
@Service
@RequiredArgsConstructor
public class FbServiceImpl implements FbService {

    private final FbBmsMapper bmMapper;
    private final FbAccountsMapper accMapper;
    private final FbProductsMapper prodMapper;

    // BM
    @Override
    public PagedResponse<FbBms> listBms(Long ownerId, int page, int size, String search, String status) {
        var qw = new LambdaQueryWrapper<FbBms>().eq(FbBms::getOwnerId, ownerId).isNull(FbBms::getDeletedAt);
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(FbBms::getName, search).or().like(FbBms::getBmId, search));
        if (status != null && !status.isBlank()) qw.eq(FbBms::getStatus, status);
        qw.orderByDesc(FbBms::getCreatedAt);
        var pg = bmMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }
    @Override public FbBms getBmById(Long id) { return bmMapper.selectById(id); }
    @Override
    public FbBms createBm(Long ownerId, String name, String bmId, String note) {
        FbBms b = new FbBms(); b.setName(name); b.setBmId(bmId); b.setNote(note);
        b.setOwnerId(ownerId); b.setStatus("normal");
        b.setCreatedAt(LocalDateTime.now()); b.setUpdatedAt(LocalDateTime.now());
        bmMapper.insert(b); return b;
    }
    @Override
    public FbBms updateBm(Long id, String name, String note) {
        FbBms b = bmMapper.selectById(id); if (b == null) return null;
        if (name != null) b.setName(name);
        if (note != null) b.setNote(note);
        b.setUpdatedAt(LocalDateTime.now()); bmMapper.updateById(b); return b;
    }
    @Override public void deleteBm(Long id) {
        FbBms b = bmMapper.selectById(id);
        if (b != null) { b.setDeletedAt(LocalDateTime.now()); bmMapper.updateById(b); }
    }
    @Override public List<FbBms> bmOptions(Long ownerId) {
        return bmMapper.selectList(new LambdaQueryWrapper<FbBms>().eq(FbBms::getOwnerId, ownerId));
    }

    // Account
    @Override
    public PagedResponse<FbAccounts> listAccounts(Long ownerId, int page, int size, String search, Long statusId) {
        var qw = new LambdaQueryWrapper<FbAccounts>().eq(FbAccounts::getOwnerId, ownerId).isNull(FbAccounts::getDeletedAt);
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(FbAccounts::getName, search).or().like(FbAccounts::getAccountId, search));
        if (statusId != null) qw.eq(FbAccounts::getStatusId, statusId);
        qw.orderByDesc(FbAccounts::getCreatedAt);
        var pg = accMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }
    @Override public FbAccounts getAccountById(Long id) { return accMapper.selectById(id); }
    @Override
    public FbAccounts createAccount(Long ownerId, String name, String accountId, Long statusId, String tz) {
        FbAccounts a = new FbAccounts(); a.setName(name); a.setAccountId(accountId);
        a.setOwnerId(ownerId); a.setStatusId(statusId); a.setTimezone(tz != null ? tz : "");
        a.setAcquiredDate(LocalDate.now()); a.setCreatedAt(LocalDateTime.now()); a.setUpdatedAt(LocalDateTime.now());
        accMapper.insert(a); return a;
    }
    @Override
    public FbAccounts updateAccount(Long id, String name, Long statusId, String tz) {
        FbAccounts a = accMapper.selectById(id); if (a == null) return null;
        if (name != null) a.setName(name);
        if (statusId != null) a.setStatusId(statusId);
        if (tz != null) a.setTimezone(tz);
        a.setUpdatedAt(LocalDateTime.now()); accMapper.updateById(a); return a;
    }
    @Override public void deleteAccount(Long id) {
        FbAccounts a = accMapper.selectById(id);
        if (a != null) { a.setDeletedAt(LocalDateTime.now()); accMapper.updateById(a); }
    }

    // Product
    @Override
    public PagedResponse<FbProducts> listProducts(Long ownerId, int page, int size, String search, String region) {
        var qw = new LambdaQueryWrapper<FbProducts>().eq(FbProducts::getOwnerId, ownerId);
        if (search != null && !search.isBlank()) qw.like(FbProducts::getProductName, search);
        if (region != null && !region.isBlank()) qw.eq(FbProducts::getRegion, region);
        qw.orderByDesc(FbProducts::getCreatedAt);
        var pg = prodMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }
    @Override public FbProducts getProductById(Long id) { return prodMapper.selectById(id); }
    @Override
    public FbProducts createProduct(Long ownerId, String name, String kpi, String region, Long spId, Double ratio) {
        FbProducts p = new FbProducts(); p.setProductName(name); p.setKpi(kpi); p.setRegion(region);
        p.setOwnerId(ownerId); p.setSalesPersonId(spId); p.setAgencyRatio(ratio);
        p.setStatus("active"); p.setIsArchived(0L);
        p.setCreatedAt(LocalDateTime.now()); p.setUpdatedAt(LocalDateTime.now());
        prodMapper.insert(p); return p;
    }
    @Override
    public FbProducts updateProduct(Long id, String name, String kpi, String region, Long spId, Double ratio) {
        FbProducts p = prodMapper.selectById(id); if (p == null) return null;
        if (name != null) p.setProductName(name);
        if (kpi != null) p.setKpi(kpi);
        if (region != null) p.setRegion(region);
        if (spId != null) p.setSalesPersonId(spId);
        if (ratio != null) p.setAgencyRatio(ratio);
        p.setUpdatedAt(LocalDateTime.now()); prodMapper.updateById(p); return p;
    }
    @Override public void deleteProduct(Long id) { prodMapper.deleteById(id); }
    @Override public List<FbProducts> productOptions(Long ownerId) {
        return prodMapper.selectList(new LambdaQueryWrapper<FbProducts>().eq(FbProducts::getOwnerId, ownerId));
    }
}
