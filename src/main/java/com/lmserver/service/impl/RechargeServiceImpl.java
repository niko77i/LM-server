package com.lmserver.service.impl;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.RechargeRecords;
import com.lmserver.repository.gg.RechargeRecordsRepository;
import com.lmserver.service.RechargeService;
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
public class RechargeServiceImpl implements RechargeService {

    private final RechargeRecordsRepository rechargeRecordsRepository;
    private final EntityManager em;

    @Override
    public PagedResponse<RechargeRecords> list(Long userId, int page, int size, String accountId) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(RechargeRecords.class);
        var root = cq.from(RechargeRecords.class);
        var preds = new ArrayList<Predicate>();
        preds.add(cb.equal(root.get("createdBy"), userId));
        if (accountId != null && !accountId.isBlank()) preds.add(cb.equal(root.get("accountId"), accountId));
        cq.where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        var cqCount = cb.createQuery(Long.class);
        var rc = cqCount.from(RechargeRecords.class);
        var pc = new ArrayList<Predicate>();
        pc.add(cb.equal(rc.get("createdBy"), userId));
        if (accountId != null && !accountId.isBlank()) pc.add(cb.equal(rc.get("accountId"), accountId));
        cqCount.select(cb.count(rc)).where(pc.toArray(new Predicate[0]));
        long total = em.createQuery(cqCount).getSingleResult();

        var q = em.createQuery(cq);
        q.setFirstResult((page - 1) * size);
        q.setMaxResults(size);
        return PagedResponse.of(q.getResultList(), total, page, size);
    }

    @Override
    public RechargeRecords create(Long userId, String accountId, String amount, String operator, String status, Long agentId) {
        RechargeRecords r = new RechargeRecords();
        r.setAccountId(accountId); r.setAmount(amount); r.setCreatedBy(userId);
        r.setOperator(operator != null ? operator : ""); r.setStatus(status != null ? status : "");
        r.setAgentId(agentId); r.setSheetsSynced(0L); r.setCreatedAt(LocalDateTime.now());
        return rechargeRecordsRepository.save(r);
    }

    @Override
    public RechargeRecords update(Long id, String amount, String status, String operator) {
        RechargeRecords r = rechargeRecordsRepository.findById(id).orElse(null);
        if (r == null) return null;
        if (amount != null) r.setAmount(amount);
        if (status != null) r.setStatus(status);
        if (operator != null) r.setOperator(operator);
        return rechargeRecordsRepository.save(r);
    }

    @Override public void delete(Long id) { rechargeRecordsRepository.deleteById(id); }
}
