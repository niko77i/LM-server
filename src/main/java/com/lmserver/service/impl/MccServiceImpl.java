package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.MccDto;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.entity.gg.Mcc;
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
    public PagedResponse<MccDto> list(Long ownerId, int page, int size, String search, String level) {
        Page<MccDto> pg = new Page<>(page, size);
        List<MccDto> items = mccMapper.selectMccDtos(pg, ownerId,
                search != null && !search.isBlank() ? search : null,
                level != null && !level.isBlank() ? level : null);

        // 填充 isOwner 和 totalAccountCount（递归计算子树账户数）
        for (MccDto dto : items) {
            dto.setOwner(dto.getOwnerId() != null && dto.getOwnerId().equals(ownerId));
            dto.setTotalAccountCount(directCountInSubtree(dto.getId(), new HashSet<>()));
        }

        return PagedResponse.of(items, pg.getTotal(), page, size);
    }

    @Override
    public MccDto detail(Long id) {
        MccDto dto = mccMapper.selectMccDtoById(id);
        if (dto != null) {
            dto.setTotalAccountCount(directCountInSubtree(dto.getId(), new HashSet<>()));
        }
        return dto;
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
