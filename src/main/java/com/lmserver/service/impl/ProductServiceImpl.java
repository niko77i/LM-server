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
    public PagedResponse<Products> list(Long ownerId, int page, int size, String search, String region, String status) {
        var qw = new LambdaQueryWrapper<Products>().eq(Products::getOwnerId, ownerId);
        if (search != null && !search.isBlank()) qw.like(Products::getProductName, search);
        if (region != null && !region.isBlank()) qw.eq(Products::getRegion, region);
        if (status != null && !status.isBlank()) qw.eq(Products::getStatus, status);
        qw.orderByDesc(Products::getCreatedAt);
        var pg = productsMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
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
        return productsMapper.selectList(new LambdaQueryWrapper<Products>().eq(Products::getOwnerId, ownerId));
    }
}
