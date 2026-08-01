package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.Copywritings;
import com.lmserver.mapper.common.CopywritingsMapper;
import com.lmserver.service.CopywritingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CopywritingServiceImpl implements CopywritingService {

    private final CopywritingsMapper mapper;

    @Override
    public PagedResponse<Copywritings> list(Long ownerId, int page, int size, String region) {
        var qw = new LambdaQueryWrapper<Copywritings>().eq(Copywritings::getOwnerId, ownerId);
        if (region != null && !region.isBlank()) qw.eq(Copywritings::getRegion, region);
        qw.orderByDesc(Copywritings::getCreatedAt);
        var pg = mapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @Override
    public Copywritings create(Long ownerId, String region, String content, Integer isPublic) {
        Copywritings c = new Copywritings(); c.setOwnerId(ownerId); c.setRegion(region);
        c.setContent(content); c.setIsPublic(isPublic != null ? isPublic.longValue() : 0L);
        mapper.insert(c); return c;
    }

    @Override
    public Copywritings update(Long id, String region, String content, String effectiveness) {
        Copywritings c = mapper.selectById(id); if (c == null) return null;
        if (region != null) c.setRegion(region);
        if (content != null) c.setContent(content);
        if (effectiveness != null) c.setEffectiveness(effectiveness);
        mapper.updateById(c); return c;
    }

    @Override public void delete(Long id) { mapper.deleteById(id); }
    @Override public void batchDelete(List<Long> ids) { mapper.deleteBatchIds(ids); }
}
