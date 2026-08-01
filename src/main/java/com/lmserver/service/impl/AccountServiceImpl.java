package com.lmserver.service.impl;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.repository.gg.AccountsRepository;
import com.lmserver.service.AccountService;
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
public class AccountServiceImpl implements AccountService {

    private final AccountsRepository accountsRepository;
    private final EntityManager em;

    @Override
    public PagedResponse<Accounts> list(Long ownerId, int page, int size, String search, Long statusId, Long mccId, Long agentId) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Accounts.class);
        var root = cq.from(Accounts.class);
        var preds = new ArrayList<Predicate>();
        preds.add(cb.equal(root.get("ownerId"), ownerId));
        preds.add(cb.isNull(root.get("deletedAt"))); // 排除软删除
        if (search != null && !search.isBlank()) {
            var like = "%" + search + "%";
            preds.add(cb.or(cb.like(root.get("name"), like), cb.like(root.get("accountId"), like)));
        }
        if (statusId != null) preds.add(cb.equal(root.get("statusId"), statusId));
        if (mccId != null) preds.add(cb.equal(root.get("mccId"), mccId));
        if (agentId != null) preds.add(cb.equal(root.get("agentId"), agentId));
        cq.where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        // count
        var cqCount = cb.createQuery(Long.class);
        var rc = cqCount.from(Accounts.class);
        var pc = new ArrayList<Predicate>();
        pc.add(cb.equal(rc.get("ownerId"), ownerId));
        pc.add(cb.isNull(rc.get("deletedAt")));
        if (search != null && !search.isBlank()) {
            var like = "%" + search + "%";
            pc.add(cb.or(cb.like(rc.get("name"), like), cb.like(rc.get("accountId"), like)));
        }
        if (statusId != null) pc.add(cb.equal(rc.get("statusId"), statusId));
        if (mccId != null) pc.add(cb.equal(rc.get("mccId"), mccId));
        if (agentId != null) pc.add(cb.equal(rc.get("agentId"), agentId));
        cqCount.select(cb.count(rc)).where(pc.toArray(new Predicate[0]));
        long total = em.createQuery(cqCount).getSingleResult();

        var q = em.createQuery(cq);
        q.setFirstResult((page - 1) * size);
        q.setMaxResults(size);
        return PagedResponse.of(q.getResultList(), total, page, size);
    }

    @Override public Accounts getById(Long id) { return accountsRepository.findById(id).orElse(null); }

    @Override
    public Accounts create(Long ownerId, String name, String accountId, Long mccId, Long agentId, Long statusId, String timezone) {
        Accounts a = new Accounts();
        a.setName(name); a.setAccountId(accountId); a.setOwnerId(ownerId);
        a.setMccId(mccId); a.setAgentId(agentId); a.setStatusId(statusId);
        a.setTimezone(timezone != null ? timezone : "");
        a.setAcquiredDate(LocalDate.now());
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        return accountsRepository.save(a);
    }

    @Override
    public Accounts update(Long id, String name, Long mccId, Long agentId, Long statusId, String timezone) {
        Accounts a = accountsRepository.findById(id).orElse(null);
        if (a == null) return null;
        if (name != null) a.setName(name);
        if (mccId != null) a.setMccId(mccId);
        if (agentId != null) a.setAgentId(agentId);
        if (statusId != null) a.setStatusId(statusId);
        if (timezone != null) a.setTimezone(timezone);
        a.setUpdatedAt(LocalDateTime.now());
        return accountsRepository.save(a);
    }

    @Override public void delete(Long id) {
        // 软删除
        Accounts a = accountsRepository.findById(id).orElse(null);
        if (a != null) { a.setDeletedAt(LocalDateTime.now()); accountsRepository.save(a); }
    }

    @Override public List<Accounts> options(Long ownerId) { return accountsRepository.findByOwnerIdOrderByNameAsc(ownerId); }
}
