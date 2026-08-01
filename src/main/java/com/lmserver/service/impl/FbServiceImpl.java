package com.lmserver.service.impl;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.*;
import com.lmserver.repository.fb.*;
import com.lmserver.service.FbService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FbServiceImpl implements FbService {

    private final FbBmsRepository fbBmsRepository;
    private final FbAccountsRepository fbAccountsRepository;
    private final FbProductsRepository fbProductsRepository;
    private final EntityManager em;

    // ==================== BM ====================
    @Override
    public PagedResponse<FbBms> listBms(Long ownerId, int page, int size, String search, String status) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(FbBms.class); var root = cq.from(FbBms.class);
        var preds = new ArrayList<Predicate>();
        preds.add(cb.equal(root.get("ownerId"), ownerId));
        preds.add(cb.isNull(root.get("deletedAt")));
        if (search != null && !search.isBlank()) {
            var like = "%" + search + "%";
            preds.add(cb.or(cb.like(root.get("name"), like), cb.like(root.get("bmId"), like)));
        }
        if (status != null && !status.isBlank()) preds.add(cb.equal(root.get("status"), status));
        cq.where(preds.toArray(new Predicate[0])); cq.orderBy(cb.desc(root.get("createdAt")));

        var cq2 = cb.createQuery(Long.class); var r2 = cq2.from(FbBms.class);
        var p2 = new ArrayList<Predicate>();
        p2.add(cb.equal(r2.get("ownerId"), ownerId)); p2.add(cb.isNull(r2.get("deletedAt")));
        cq2.select(cb.count(r2)).where(p2.toArray(new Predicate[0]));
        long total = em.createQuery(cq2).getSingleResult();
        var q = em.createQuery(cq);
        q.setFirstResult((page - 1) * size); q.setMaxResults(size);
        return PagedResponse.of(q.getResultList(), total, page, size);
    }

    @Override public FbBms getBmById(Long id) { return fbBmsRepository.findById(id).orElse(null); }

    @Override
    public FbBms createBm(Long ownerId, String name, String bmId, String note) {
        FbBms b = new FbBms(); b.setName(name); b.setBmId(bmId); b.setNote(note);
        b.setOwnerId(ownerId); b.setStatus("normal"); b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        return fbBmsRepository.save(b);
    }

    @Override
    public FbBms updateBm(Long id, String name, String note) {
        FbBms b = fbBmsRepository.findById(id).orElse(null);
        if (b == null) return null;
        if (name != null) b.setName(name);
        if (note != null) b.setNote(note);
        b.setUpdatedAt(LocalDateTime.now());
        return fbBmsRepository.save(b);
    }

    @Override public void deleteBm(Long id) {
        FbBms b = fbBmsRepository.findById(id).orElse(null);
        if (b != null) { b.setDeletedAt(LocalDateTime.now()); fbBmsRepository.save(b); }
    }

    @Override public List<FbBms> bmOptions(Long ownerId) { return fbBmsRepository.findByOwnerIdOrderByNameAsc(ownerId); }

    // ==================== Account ====================
    @Override
    public PagedResponse<FbAccounts> listAccounts(Long ownerId, int page, int size, String search, Long statusId) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(FbAccounts.class); var root = cq.from(FbAccounts.class);
        var preds = new ArrayList<Predicate>();
        preds.add(cb.equal(root.get("ownerId"), ownerId));
        preds.add(cb.isNull(root.get("deletedAt")));
        if (search != null && !search.isBlank()) {
            var like = "%" + search + "%";
            preds.add(cb.or(cb.like(root.get("name"), like), cb.like(root.get("accountId"), like)));
        }
        if (statusId != null) preds.add(cb.equal(root.get("statusId"), statusId));
        cq.where(preds.toArray(new Predicate[0])); cq.orderBy(cb.desc(root.get("createdAt")));

        var cq2 = cb.createQuery(Long.class); var r2 = cq2.from(FbAccounts.class);
        var p2 = new ArrayList<Predicate>();
        p2.add(cb.equal(r2.get("ownerId"), ownerId)); p2.add(cb.isNull(r2.get("deletedAt")));
        cq2.select(cb.count(r2)).where(p2.toArray(new Predicate[0]));
        long total = em.createQuery(cq2).getSingleResult();
        var q = em.createQuery(cq);
        q.setFirstResult((page - 1) * size); q.setMaxResults(size);
        return PagedResponse.of(q.getResultList(), total, page, size);
    }

    @Override public FbAccounts getAccountById(Long id) { return fbAccountsRepository.findById(id).orElse(null); }

    @Override
    public FbAccounts createAccount(Long ownerId, String name, String accountId, Long statusId, String timezone) {
        FbAccounts a = new FbAccounts(); a.setName(name); a.setAccountId(accountId);
        a.setOwnerId(ownerId); a.setStatusId(statusId);
        a.setTimezone(timezone != null ? timezone : "");
        a.setAcquiredDate(LocalDate.now()); a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        return fbAccountsRepository.save(a);
    }

    @Override
    public FbAccounts updateAccount(Long id, String name, Long statusId, String timezone) {
        FbAccounts a = fbAccountsRepository.findById(id).orElse(null);
        if (a == null) return null;
        if (name != null) a.setName(name);
        if (statusId != null) a.setStatusId(statusId);
        if (timezone != null) a.setTimezone(timezone);
        a.setUpdatedAt(LocalDateTime.now());
        return fbAccountsRepository.save(a);
    }

    @Override public void deleteAccount(Long id) {
        FbAccounts a = fbAccountsRepository.findById(id).orElse(null);
        if (a != null) { a.setDeletedAt(LocalDateTime.now()); fbAccountsRepository.save(a); }
    }

    // ==================== Product ====================
    @Override
    public PagedResponse<FbProducts> listProducts(Long ownerId, int page, int size, String search, String region) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(FbProducts.class); var root = cq.from(FbProducts.class);
        var preds = new ArrayList<Predicate>();
        preds.add(cb.equal(root.get("ownerId"), ownerId));
        if (search != null && !search.isBlank()) preds.add(cb.like(root.get("productName"), "%" + search + "%"));
        if (region != null && !region.isBlank()) preds.add(cb.equal(root.get("region"), region));
        cq.where(preds.toArray(new Predicate[0])); cq.orderBy(cb.desc(root.get("createdAt")));

        var cq2 = cb.createQuery(Long.class); var r2 = cq2.from(FbProducts.class);
        var p2 = new ArrayList<Predicate>();
        p2.add(cb.equal(r2.get("ownerId"), ownerId));
        cq2.select(cb.count(r2)).where(p2.toArray(new Predicate[0]));
        long total = em.createQuery(cq2).getSingleResult();
        var q = em.createQuery(cq);
        q.setFirstResult((page - 1) * size); q.setMaxResults(size);
        return PagedResponse.of(q.getResultList(), total, page, size);
    }

    @Override public FbProducts getProductById(Long id) { return fbProductsRepository.findById(id).orElse(null); }

    @Override
    public FbProducts createProduct(Long ownerId, String name, String kpi, String region, Long salesPersonId, Double ratio) {
        FbProducts p = new FbProducts(); p.setProductName(name); p.setKpi(kpi); p.setRegion(region);
        p.setOwnerId(ownerId); p.setSalesPersonId(salesPersonId); p.setAgencyRatio(ratio);
        p.setStatus("active"); p.setIsArchived(0L); p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return fbProductsRepository.save(p);
    }

    @Override
    public FbProducts updateProduct(Long id, String name, String kpi, String region, Long salesPersonId, Double ratio) {
        FbProducts p = fbProductsRepository.findById(id).orElse(null);
        if (p == null) return null;
        if (name != null) p.setProductName(name);
        if (kpi != null) p.setKpi(kpi);
        if (region != null) p.setRegion(region);
        if (salesPersonId != null) p.setSalesPersonId(salesPersonId);
        if (ratio != null) p.setAgencyRatio(ratio);
        p.setUpdatedAt(LocalDateTime.now());
        return fbProductsRepository.save(p);
    }

    @Override public void deleteProduct(Long id) { fbProductsRepository.deleteById(id); }

    @Override public List<FbProducts> productOptions(Long ownerId) { return fbProductsRepository.findByOwnerIdOrderByProductNameAsc(ownerId); }
}
