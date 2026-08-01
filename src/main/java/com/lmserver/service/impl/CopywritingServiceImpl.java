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
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<Copywritings> list(Long ownerId, int page, int size, String region) {
        var qw = new LambdaQueryWrapper<Copywritings>().eq(Copywritings::getOwnerId, ownerId);
        if (region != null && !region.isBlank()) qw.eq(Copywritings::getRegion, region);
        qw.orderByDesc(Copywritings::getCreatedAt);
        var pg = mapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @Override
    /** 新增记录 — 返回创建后的完整对象 */
    public Copywritings create(Long ownerId, String region, String content, Integer isPublic) {
        Copywritings c = new Copywritings(); c.setOwnerId(ownerId); c.setRegion(region);
        c.setContent(content); c.setIsPublic(isPublic != null ? isPublic.longValue() : 0L);
        mapper.insert(c); return c;
    }

    @Override
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public Copywritings update(Long id, String region, String content, String effectiveness) {
        Copywritings c = mapper.selectById(id); if (c == null) return null;
        if (region != null) c.setRegion(region);
        if (content != null) c.setContent(content);
        if (effectiveness != null) c.setEffectiveness(effectiveness);
        mapper.updateById(c); return c;
    }

    /** 删除记录 */
    @Override public void delete(Long id) { mapper.deleteById(id); }
    /** 批量删除 — 按 ID 列表批量删除 */
    @Override public void batchDelete(List<Long> ids) { mapper.deleteBatchIds(ids); }
}
