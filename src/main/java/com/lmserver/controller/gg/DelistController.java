package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.DelistChecks;
import com.lmserver.mapper.gg.DelistChecksMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/**
 * 掉包检测控制器 — /api/delist/*，查询Google Play应用下架检测结果
 */

@RestController
@RequestMapping("/api/delist")
@RequiredArgsConstructor
public class DelistController {

    private final DelistChecksMapper mapper;

    @GetMapping("/checks")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<DelistChecks> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long productId) {
        var qw = new LambdaQueryWrapper<DelistChecks>();
        if (productId != null) qw.eq(DelistChecks::getProductId, productId);
        qw.orderByDesc(DelistChecks::getCheckedAt);
        var pg = mapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @GetMapping("/product/{productId}")
    /** 按产品 ID 查询 — 获取指定产品的检查结果 */
    public ApiResponse<?> getByProduct(@PathVariable Long productId) {
        var list = mapper.selectList(new LambdaQueryWrapper<DelistChecks>().eq(DelistChecks::getProductId, productId));
        return ApiResponse.ok(list);
    }
}
