package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.FbAccountDto;
import com.lmserver.dto.response.FbBmDto;
import com.lmserver.dto.response.FbProductDto;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class FbServiceImpl implements FbService {

    private final FbBmsMapper bmMapper;
    private final FbAccountsMapper accMapper;
    private final FbProductsMapper prodMapper;

    // BM
    @Override
    /** BM 列表查询 — 支持名称/ID搜索和状态筛选 */
    public PagedResponse<FbBmDto> listBms(Long ownerId, int page, int size, String search, String status) {
        Page<FbBmDto> pg = new Page<>(page, size);
        List<FbBmDto> items = bmMapper.selectFbBmDtosNormal(pg, ownerId,
                search != null && !search.isBlank() ? search : null,
                status != null && !status.isBlank() ? status : null);
        return PagedResponse.of(items, pg.getTotal(), page, size);
    }
    /** 按 ID 查询 BM */
    @Override public FbBms getBmById(Long id) { return bmMapper.selectById(id); }
    @Override
    /** 创建 BM — 新建商务管理平台记录 */
    public FbBms createBm(Long ownerId, String name, String bmId, String note) {
        FbBms b = new FbBms(); b.setName(name); b.setBmId(bmId); b.setNote(note);
        b.setOwnerId(ownerId); b.setStatus("normal");
        b.setCreatedAt(LocalDateTime.now()); b.setUpdatedAt(LocalDateTime.now());
        bmMapper.insert(b); return b;
    }
    @Override
    /** 更新 BM — 可改名和备注 */
    public FbBms updateBm(Long id, String name, String note) {
        FbBms b = bmMapper.selectById(id); if (b == null) return null;
        if (name != null) b.setName(name);
        if (note != null) b.setNote(note);
        b.setUpdatedAt(LocalDateTime.now()); bmMapper.updateById(b); return b;
    }
    /** 删除 BM — 软删除，记录删除时间 */
    @Override public void deleteBm(Long id) {
        FbBms b = bmMapper.selectById(id);
        if (b != null) { b.setDeletedAt(LocalDateTime.now()); bmMapper.updateById(b); }
    }
    /** BM 下拉选项 — 返回当前用户可见的 BM 列表 */
    @Override public List<FbBms> bmOptions(Long ownerId) {
        return bmMapper.selectList(new LambdaQueryWrapper<FbBms>().eq(FbBms::getOwnerId, ownerId));
    }

    // Account
    @Override
    /** 账户列表查询 — 多条件筛选（名称/账号ID/状态） */
    public PagedResponse<FbAccountDto> listAccounts(Long ownerId, int page, int size, String search, Long statusId, Long bmId) {
        Page<FbAccountDto> pg = new Page<>(page, size);
        List<FbAccountDto> items = accMapper.selectFbAccountDtos(pg, ownerId,
                search != null && !search.isBlank() ? search : null, statusId, bmId);
        return PagedResponse.of(items, pg.getTotal(), page, size);
    }
    /** 按 ID 查询账户 DTO（含状态名称） */
    @Override public FbAccountDto getAccountDtoById(Long id) { return accMapper.selectFbAccountDtoById(id); }
    /** 按 ID 查询账户 */
    @Override public FbAccounts getAccountById(Long id) { return accMapper.selectById(id); }
    @Override
    /** 创建账户 — 新建广告账户记录 */
    public FbAccounts createAccount(Long ownerId, String name, String accountId, Long statusId, String tz) {
        FbAccounts a = new FbAccounts(); a.setName(name); a.setAccountId(accountId);
        a.setOwnerId(ownerId); a.setStatusId(statusId); a.setTimezone(tz != null ? tz : "");
        a.setAcquiredDate(LocalDate.now()); a.setCreatedAt(LocalDateTime.now()); a.setUpdatedAt(LocalDateTime.now());
        accMapper.insert(a); return a;
    }
    @Override
    /** 更新账户 — 可改名称/状态/时区 */
    public FbAccounts updateAccount(Long id, String name, Long statusId, String tz) {
        FbAccounts a = accMapper.selectById(id); if (a == null) return null;
        if (name != null) a.setName(name);
        if (statusId != null) a.setStatusId(statusId);
        if (tz != null) a.setTimezone(tz);
        a.setUpdatedAt(LocalDateTime.now()); accMapper.updateById(a); return a;
    }
    /** 删除账户 — 软删除 */
    @Override public void deleteAccount(Long id) {
        FbAccounts a = accMapper.selectById(id);
        if (a != null) { a.setDeletedAt(LocalDateTime.now()); accMapper.updateById(a); }
    }

    // Product
    @Override
    /** 产品列表查询 — 支持名称搜索和地区筛选 */
    public PagedResponse<FbProductDto> listProducts(Long ownerId, int page, int size, String search, String region) {
        Page<FbProductDto> pg = new Page<>(page, size);
        List<FbProductDto> items = prodMapper.selectFbProductDtos(pg, ownerId,
                search != null && !search.isBlank() ? search : null,
                region != null && !region.isBlank() ? region : null);
        return PagedResponse.of(items, pg.getTotal(), page, size);
    }
    /** 按 ID 查询产品 */
    @Override public FbProducts getProductById(Long id) { return prodMapper.selectById(id); }
    @Override
    /** 创建产品 — 新建产品记录 */
    public FbProducts createProduct(Long ownerId, String name, String kpi, String region, Long spId, Double ratio) {
        FbProducts p = new FbProducts(); p.setProductName(name); p.setKpi(kpi); p.setRegion(region);
        p.setOwnerId(ownerId); p.setSalesPersonId(spId); p.setAgencyRatio(ratio);
        p.setStatus("active"); p.setIsArchived(0L);
        p.setCreatedAt(LocalDateTime.now()); p.setUpdatedAt(LocalDateTime.now());
        prodMapper.insert(p); return p;
    }
    @Override
    /** 更新产品 — 可改名称/KPI/地区/状态/商务/MCC/代理比例 */
    public FbProducts updateProduct(Long id, String name, String kpi, String region, Long spId, Double ratio) {
        FbProducts p = prodMapper.selectById(id); if (p == null) return null;
        if (name != null) p.setProductName(name);
        if (kpi != null) p.setKpi(kpi);
        if (region != null) p.setRegion(region);
        if (spId != null) p.setSalesPersonId(spId);
        if (ratio != null) p.setAgencyRatio(ratio);
        p.setUpdatedAt(LocalDateTime.now()); prodMapper.updateById(p); return p;
    }
    /** 删除产品 */
    @Override public void deleteProduct(Long id) { prodMapper.deleteById(id); }
    /** 产品下拉选项 — 返回当前用户可见的产品列表 */
    @Override public List<FbProducts> productOptions(Long ownerId) {
        return prodMapper.selectList(new LambdaQueryWrapper<FbProducts>().eq(FbProducts::getOwnerId, ownerId));
    }
}
