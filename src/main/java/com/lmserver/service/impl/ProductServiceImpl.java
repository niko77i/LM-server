package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.*;
import com.lmserver.mapper.gg.*;
import com.lmserver.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductsMapper productsMapper;
    @Autowired private PackagesMapper packagesMapper;
    @Autowired private com.lmserver.mapper.gg.MccMapper mccMapper;
    @Autowired private com.lmserver.mapper.common.SalesPersonsMapper salesPersonsMapper;
    @Autowired private ProductRunnersMapper productRunnersMapper;
    @Autowired private ProductAssetsMapper productAssetsMapper;
    @Autowired private com.lmserver.mapper.gg.AccountsMapper accountsMapper;

    @Override
    public PagedResponse<Map<String, Object>> list(Long ownerId, int page, int size, String search, String region, String status) {
        var qw = new LambdaQueryWrapper<Products>().eq(Products::getOwnerId, ownerId).isNull(Products::getDeletedAt);
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(Products::getProductName, search).or().like(Products::getKpi, search));
        if (region != null && !region.isBlank()) qw.eq(Products::getRegion, region);
        if (status != null && !status.isBlank()) qw.eq(Products::getStatus, status);
        qw.orderByDesc(Products::getCreatedAt);
        var pg = productsMapper.selectPage(new Page<>(page, size), qw);

        // Batch-load related data
        var mccMap = new HashMap<Long, com.lmserver.entity.gg.Mcc>();
        var spMap = new HashMap<Long, com.lmserver.entity.common.SalesPersons>();
        for (Products p : pg.getRecords()) {
            if (p.getMccId() != null && !mccMap.containsKey(p.getMccId()))
                mccMap.put(p.getMccId(), mccMapper.selectById(p.getMccId()));
            if (p.getSalesPersonId() != null && !spMap.containsKey(p.getSalesPersonId()))
                spMap.put(p.getSalesPersonId(), salesPersonsMapper.selectById(p.getSalesPersonId()));
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (Products p : pg.getRecords()) {
            var mcc = mccMap.get(p.getMccId());
            var sp = spMap.get(p.getSalesPersonId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", p.getId());
            row.put("product_name", p.getProductName());
            row.put("kpi", p.getKpi());
            row.put("region", p.getRegion());
            row.put("status", p.getStatus());
            row.put("customer", p.getCustomer());
            row.put("owner_id", p.getOwnerId());
            row.put("agency_ratio", p.getAgencyRatio());
            row.put("is_archived", p.getIsArchived());
            row.put("created_at", p.getCreatedAt());
            // JOIN names
            row.put("sales_person", sp != null ? sp.getName() : null);
            row.put("sales_person_id", p.getSalesPersonId());
            row.put("mcc_name", mcc != null ? mcc.getName() : null);
            row.put("mcc_code", mcc != null ? mcc.getMccId() : null);
            row.put("mcc_id", p.getMccId());
            // Runners
            List<Long> runnerIds = new ArrayList<>();
            for (var pr : productRunnersMapper.selectList(
                    new LambdaQueryWrapper<ProductRunners>().eq(ProductRunners::getProductId, p.getId())))
                runnerIds.add(pr.getUserId());
            row.put("runner_ids", runnerIds);
            // Packages
            row.put("packages", packagesMapper.selectList(
                    new LambdaQueryWrapper<Packages>().eq(Packages::getProductId, p.getId())));
            // Counts
            row.put("asset_count", productAssetsMapper.selectCount(
                    new LambdaQueryWrapper<ProductAssets>().eq(ProductAssets::getProductId, p.getId())));
            // Related accounts via MCC
            long acctCount = 0;
            if (p.getMccId() != null)
                acctCount = accountsMapper.selectCount(
                        new LambdaQueryWrapper<Accounts>().eq(Accounts::getMccId, p.getMccId()).isNull(Accounts::getDeletedAt));
            row.put("related_account_count", acctCount);
            items.add(row);
        }

        return PagedResponse.of(items, pg.getTotal(), page, size);
    }

    @Override public Products getById(Long id) { return productsMapper.selectById(id); }

    @Override
    public Products create(Long ownerId, String name, String kpi, String region, String status,
                           String customer, Long spId, Long mccId, Double ratio) {
        Products p = new Products(); p.setProductName(name); p.setKpi(kpi); p.setRegion(region);
        p.setStatus(status); p.setCustomer(customer); p.setOwnerId(ownerId);
        p.setSalesPersonId(spId); p.setMccId(mccId); p.setAgencyRatio(ratio);
        p.setRunnerIds("[]"); p.setIsArchived(0L); p.setCreatedAt(LocalDateTime.now());
        productsMapper.insert(p); return p;
    }

    @Override
    public Products update(Long id, String name, String kpi, String region, String status,
                           String customer, Long spId, Long mccId, Double ratio) {
        Products p = productsMapper.selectById(id); if (p == null) return null;
        if (name != null) p.setProductName(name);
        if (kpi != null) p.setKpi(kpi);
        if (region != null) p.setRegion(region);
        if (status != null) p.setStatus(status);
        if (customer != null) p.setCustomer(customer);
        if (spId != null) p.setSalesPersonId(spId);
        if (mccId != null) p.setMccId(mccId);
        if (ratio != null) p.setAgencyRatio(ratio);
        productsMapper.updateById(p); return p;
    }

    @Override public void delete(Long id) { productsMapper.deleteById(id); }
    @Override public List<Products> options(Long ownerId) {
        return productsMapper.selectList(new LambdaQueryWrapper<Products>()
                .eq(Products::getOwnerId, ownerId).isNull(Products::getDeletedAt));
    }
}
