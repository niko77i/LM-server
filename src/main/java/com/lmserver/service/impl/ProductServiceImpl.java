package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Products;
import com.lmserver.mapper.gg.ProductsMapper;
import com.lmserver.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductsMapper productsMapper;

    @Override
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<Products> list(Long ownerId, int page, int size, String search, String region, String status) {
        var qw = new LambdaQueryWrapper<Products>().eq(Products::getOwnerId, ownerId);
        if (search != null && !search.isBlank()) qw.like(Products::getProductName, search);
        if (region != null && !region.isBlank()) qw.eq(Products::getRegion, region);
        if (status != null && !status.isBlank()) qw.eq(Products::getStatus, status);
        qw.orderByDesc(Products::getCreatedAt);
        var pg = productsMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }
    /** 按 ID 查询 — 返回单条记录 */
    @Override public Products getById(Long id) { return productsMapper.selectById(id); }

    @Override
    /** 新增记录 — 返回创建后的完整对象 */
    public Products create(Long ownerId, String name, String kpi, String region, String status,
                           String customer, Long spId, Long mccId, Double ratio) {
        Products p = new Products(); p.setProductName(name); p.setKpi(kpi); p.setRegion(region);
        p.setStatus(status); p.setCustomer(customer); p.setOwnerId(ownerId);
        p.setSalesPersonId(spId); p.setMccId(mccId); p.setAgencyRatio(ratio);
        p.setRunnerIds("[]"); p.setIsArchived(0L); p.setCreatedAt(LocalDateTime.now());
        productsMapper.insert(p); return p;
    }

    @Override
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
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
    /** 删除记录 */
    @Override public void delete(Long id) { productsMapper.deleteById(id); }
    /** 获取下拉选项 — 返回 id + name 的简略列表 */
    @Override public List<Products> options(Long ownerId) {
        return productsMapper.selectList(new LambdaQueryWrapper<Products>().eq(Products::getOwnerId, ownerId));
    }
}
