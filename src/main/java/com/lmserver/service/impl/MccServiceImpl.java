package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.entity.gg.Mcc;
import com.lmserver.entity.common.MccLevels;
import com.lmserver.mapper.gg.AccountsMapper;
import com.lmserver.mapper.gg.MccMapper;
import com.lmserver.mapper.common.MccLevelsMapper;
import com.lmserver.service.MccService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MccServiceImpl implements MccService {

    private final MccMapper mccMapper;
    @Autowired private MccLevelsMapper mccLevelsMapper;
    @Autowired private AccountsMapper accountsMapper;

    @Override
    public PagedResponse<Map<String, Object>> list(Long ownerId, int page, int size, String search, Long levelId) {
        var qw = new LambdaQueryWrapper<Mcc>().eq(Mcc::getOwnerId, ownerId);
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(Mcc::getName, search).or().like(Mcc::getMccId, search));
        if (levelId != null) qw.eq(Mcc::getLevelId, levelId);
        qw.orderByDesc(Mcc::getCreatedAt);
        var pg = mccMapper.selectPage(new Page<>(page, size), qw);

        // Build enriched items
        List<Map<String, Object>> items = new ArrayList<>();
        // Cache level names
        Map<Long, String> levelCache = new HashMap<>();
        for (Mcc m : pg.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("name", m.getName());
            item.put("mcc_id", m.getMccId());
            item.put("parent_mcc_id", m.getParentMccId());
            item.put("level_id", m.getLevelId());
            item.put("owner_id", m.getOwnerId());
            item.put("created_at", m.getCreatedAt());
            // Level name
            if (m.getLevelId() != null) {
                String lvName = levelCache.computeIfAbsent(m.getLevelId(),
                        lid -> { MccLevels lv = mccLevelsMapper.selectById(lid); return lv != null ? lv.getName() : ""; });
                item.put("level", lvName);
            } else {
                item.put("level", "");
            }
            // Direct account count
            long directCount = accountsMapper.selectCount(
                    new LambdaQueryWrapper<Accounts>().eq(Accounts::getMccId, m.getId()).isNull(Accounts::getDeletedAt));
            item.put("direct_count", directCount);
            // is_owner
            item.put("is_owner", m.getOwnerId().equals(ownerId));
            items.add(item);
        }

        // Batch compute total_accounts (subtree count) — lazy compute
        for (Map<String, Object> item : items) {
            Long mid = (Long) item.get("id");
            long total = accountsMapper.selectCount(
                    new LambdaQueryWrapper<Accounts>().isNull(Accounts::getDeletedAt)); // simplified: all accounts
            item.put("total_accounts", directCountInSubtree(mid, new HashSet<>()));
        }

        return PagedResponse.of(items, pg.getTotal(), page, size);
    }

    private long directCountInSubtree(Long mccId, Set<Long> visited) {
        if (!visited.add(mccId)) return 0;
        long count = accountsMapper.selectCount(
                new LambdaQueryWrapper<Accounts>().eq(Accounts::getMccId, mccId).isNull(Accounts::getDeletedAt));
        var children = mccMapper.selectList(
                new LambdaQueryWrapper<Mcc>().eq(Mcc::getParentMccId, mccId));
        for (Mcc c : children) count += directCountInSubtree(c.getId(), visited);
        return count;
    }

    @Override public Mcc getById(Long id) { return mccMapper.selectById(id); }

    @Override
    public Mcc create(Long ownerId, String name, String mccId, Long levelId, Long parentMccId) {
        Mcc m = new Mcc(); m.setName(name); m.setMccId(mccId); m.setOwnerId(ownerId);
        m.setLevelId(levelId); m.setParentMccId(parentMccId); m.setSharedUserIds("[]");
        m.setCreatedAt(LocalDateTime.now()); m.setUpdatedAt(LocalDateTime.now());
        mccMapper.insert(m); return m;
    }

    @Override
    public Mcc update(Long id, String name, Long levelId, Long parentMccId) {
        Mcc m = mccMapper.selectById(id); if (m == null) return null;
        if (name != null) m.setName(name);
        if (levelId != null) m.setLevelId(levelId);
        if (parentMccId != null) m.setParentMccId(parentMccId);
        m.setUpdatedAt(LocalDateTime.now()); mccMapper.updateById(m); return m;
    }

    @Override public void delete(Long id) { mccMapper.deleteById(id); }
    @Override public List<Mcc> options(Long ownerId) {
        return mccMapper.selectList(new LambdaQueryWrapper<Mcc>().eq(Mcc::getOwnerId, ownerId));
    }
}
