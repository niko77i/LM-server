package com.lmserver.service.impl;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Products;
import com.lmserver.repository.gg.ProductsRepository;
import com.lmserver.service.ProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductsRepository productsRepository;
    private final EntityManager em;

    @Override
    public PagedResponse<Products> list(Long ownerId, int page, int size, String search, String region, String status) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Products.class);
        var root = cq.from(Products.class);
        var preds = new ArrayList<Predicate>();
        preds.add(cb.equal(root.get("ownerId"), ownerId));
        if (search != null && !search.isBlank()) {
            preds.add(cb.like(root.get("productName"), "%" + search + "%"));
        }
        if (region != null && !region.isBlank()) preds.add(cb.equal(root.get("region"), region));
        if (status != null && !status.isBlank()) preds.add(cb.equal(root.get("status"), status));
        cq.where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        var cqCount = cb.createQuery(Long.class);
        var rootCount = cqCount.from(Products.class);
        var predsCount = new ArrayList<Predicate>();
        predsCount.add(cb.equal(rootCount.get("ownerId"), ownerId));
        if (search != null && !search.isBlank()) predsCount.add(cb.like(rootCount.get("productName"), "%" + search + "%"));
        if (region != null && !region.isBlank()) predsCount.add(cb.equal(rootCount.get("region"), region));
        if (status != null && !status.isBlank()) predsCount.add(cb.equal(rootCount.get("status"), status));
        cqCount.select(cb.count(rootCount)).where(predsCount.toArray(new Predicate[0]));
        long total = em.createQuery(cqCount).getSingleResult();

        var q = em.createQuery(cq);
        q.setFirstResult((page - 1) * size);
        q.setMaxResults(size);
        return PagedResponse.of(q.getResultList(), total, page, size);
    }

    @Override public Products getById(Long id) { return productsRepository.findById(id).orElse(null); }

    @Override
    public Products create(Long ownerId, String productName, String kpi, String region, String status,
                           String customer, Long salesPersonId, Long mccId, Double agencyRatio) {
        Products p = new Products();
        p.setProductName(productName); p.setKpi(kpi); p.setRegion(region);
        p.setStatus(status); p.setCustomer(customer); p.setOwnerId(ownerId);
        p.setSalesPersonId(salesPersonId); p.setMccId(mccId); p.setAgencyRatio(agencyRatio);
        p.setRunnerIds("[]"); p.setIsArchived(0L); p.setCreatedAt(LocalDateTime.now());
        return productsRepository.save(p);
    }

    @Override
    public Products update(Long id, String productName, String kpi, String region, String status,
                           String customer, Long salesPersonId, Long mccId, Double agencyRatio) {
        Products p = productsRepository.findById(id).orElse(null);
        if (p == null) return null;
        if (productName != null) p.setProductName(productName);
        if (kpi != null) p.setKpi(kpi);
        if (region != null) p.setRegion(region);
        if (status != null) p.setStatus(status);
        if (customer != null) p.setCustomer(customer);
        if (salesPersonId != null) p.setSalesPersonId(salesPersonId);
        if (mccId != null) p.setMccId(mccId);
        if (agencyRatio != null) p.setAgencyRatio(agencyRatio);
        return productsRepository.save(p);
    }

    @Override public void delete(Long id) { productsRepository.deleteById(id); }

    @Override public List<Products> options(Long ownerId) { return productsRepository.findByOwnerIdOrderByProductNameAsc(ownerId); }
}
