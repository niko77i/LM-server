package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PackageDto;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.dto.response.ProductDto;
import com.lmserver.entity.gg.*;
import com.lmserver.mapper.gg.*;
import com.lmserver.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public PagedResponse<ProductDto> list(Long ownerId, int page, int size, String search, String region, String status) {
        Page<ProductDto> pg = new Page<>(page, size);
        List<ProductDto> items = productsMapper.selectProductDtos(pg, ownerId,
                search != null && !search.isBlank() ? search : null,
                region != null && !region.isBlank() ? region : null,
                status != null && !status.isBlank() ? status : null);

        // 批量填充 packages 和 runnerIdList（无法一条 SQL 完成集合字段）
        if (!items.isEmpty()) {
            List<Long> productIds = items.stream().map(ProductDto::getId).toList();

            // 批量查包列表
            List<PackageDto> allPkgs = packagesMapper.selectPackagesByProductIds(productIds);
            Map<Long, List<PackageDto>> pkgMap = allPkgs.stream()
                    .collect(Collectors.groupingBy(PackageDto::getProductId));

            // 批量查 runners
            List<ProductRunners> allRunners = productRunnersMapper.selectList(
                    new LambdaQueryWrapper<ProductRunners>().in(ProductRunners::getProductId, productIds));
            Map<Long, List<Long>> runnerMap = allRunners.stream()
                    .collect(Collectors.groupingBy(ProductRunners::getProductId,
                            Collectors.mapping(ProductRunners::getUserId, Collectors.toList())));

            // 回填
            for (ProductDto dto : items) {
                dto.setPackages(pkgMap.getOrDefault(dto.getId(), List.of()));
                dto.setRunnerIdList(runnerMap.getOrDefault(dto.getId(), List.of()));
            }
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
