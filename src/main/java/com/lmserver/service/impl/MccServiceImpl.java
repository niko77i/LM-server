package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Mcc;
import com.lmserver.mapper.gg.MccMapper;
import com.lmserver.service.MccService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MccServiceImpl implements MccService {

    private final MccMapper mccMapper;

    @Override
    public PagedResponse<Mcc> list(Long ownerId, int page, int size, String search, Long levelId) {
        var qw = new LambdaQueryWrapper<Mcc>().eq(Mcc::getOwnerId, ownerId);
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(Mcc::getName, search).or().like(Mcc::getMccId, search));
        if (levelId != null) qw.eq(Mcc::getLevelId, levelId);
        qw.orderByDesc(Mcc::getCreatedAt);
        var pg = mccMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
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
