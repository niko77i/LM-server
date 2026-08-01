package com.lmserver.service.impl;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Mcc;
import com.lmserver.repository.gg.MccRepository;
import com.lmserver.service.MccService;
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
public class MccServiceImpl implements MccService {

    private final MccRepository mccRepository;
    private final EntityManager em;

    @Override
    public PagedResponse<Mcc> list(Long ownerId, int page, int size, String search, Long levelId) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Mcc.class);
        var root = cq.from(Mcc.class);
        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get("ownerId"), ownerId));

        if (search != null && !search.isBlank()) {
            var like = "%" + search + "%";
            predicates.add(cb.or(
                cb.like(root.get("name"), like),
                cb.like(root.get("mccId"), like)
            ));
        }
        if (levelId != null) {
            predicates.add(cb.equal(root.get("levelId"), levelId));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        // count
        var countQuery = cb.createQuery(Long.class);
        var countRoot = countQuery.from(Mcc.class);
        var countPreds = new ArrayList<Predicate>();
        countPreds.add(cb.equal(countRoot.get("ownerId"), ownerId));
        if (search != null && !search.isBlank()) {
            var like = "%" + search + "%";
            countPreds.add(cb.or(
                cb.like(countRoot.get("name"), like),
                cb.like(countRoot.get("mccId"), like)
            ));
        }
        if (levelId != null) {
            countPreds.add(cb.equal(countRoot.get("levelId"), levelId));
        }
        countQuery.select(cb.count(countRoot)).where(countPreds.toArray(new Predicate[0]));
        long total = em.createQuery(countQuery).getSingleResult();

        var typedQuery = em.createQuery(cq);
        typedQuery.setFirstResult((page - 1) * size);
        typedQuery.setMaxResults(size);
        List<Mcc> items = typedQuery.getResultList();

        return PagedResponse.of(items, total, page, size);
    }

    @Override
    public Mcc getById(Long id) {
        return mccRepository.findById(id).orElse(null);
    }

    @Override
    public Mcc create(Long ownerId, String name, String mccId, Long levelId, Long parentMccId) {
        Mcc m = new Mcc();
        m.setName(name);
        m.setMccId(mccId);
        m.setOwnerId(ownerId);
        m.setLevelId(levelId);
        m.setParentMccId(parentMccId);
        m.setSharedUserIds("[]");
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        return mccRepository.save(m);
    }

    @Override
    public Mcc update(Long id, String name, Long levelId, Long parentMccId) {
        Mcc m = mccRepository.findById(id).orElse(null);
        if (m == null) return null;
        if (name != null) m.setName(name);
        if (levelId != null) m.setLevelId(levelId);
        if (parentMccId != null) m.setParentMccId(parentMccId);
        m.setUpdatedAt(LocalDateTime.now());
        return mccRepository.save(m);
    }

    @Override
    public void delete(Long id) {
        mccRepository.deleteById(id);
    }

    @Override
    public List<Mcc> options(Long ownerId) {
        return mccRepository.findByOwnerIdOrderByNameAsc(ownerId);
    }
}
