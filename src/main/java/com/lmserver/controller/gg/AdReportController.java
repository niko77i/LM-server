package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.AdReports;
import com.lmserver.mapper.gg.AdReportsMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * GG 广告报告控制器 — /api/ad-reports/*，GG广告投放数据的CRUD
 */

/**
 * GG 广告报告控制器 — /api/ad-reports/*，GG广告投放数据的CRUD
 */

@RestController
@RequestMapping("/api/ad-reports")
@RequiredArgsConstructor
public class AdReportController {

    private final AdReportsMapper mapper;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<AdReports> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String productName, @RequestParam(required = false) String region,
            @RequestParam(required = false) String reportDate) {
        var qw = new LambdaQueryWrapper<AdReports>().eq(AdReports::getUserId, principal.getUserId());
        if (productName != null && !productName.isBlank()) qw.eq(AdReports::getProductName, productName);
        if (region != null && !region.isBlank()) qw.eq(AdReports::getRegion, region);
        if (reportDate != null && !reportDate.isBlank()) qw.eq(AdReports::getReportDate, java.time.LocalDate.parse(reportDate));
        qw.orderByDesc(AdReports::getReportDate);
        var pg = mapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<AdReports> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AdReports report) {
        report.setUserId(principal.getUserId());
        mapper.insert(report);
        return ApiResponse.ok(report);
    }

    @PutMapping("/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<AdReports> update(@PathVariable Long id, @RequestBody AdReports report) {
        report.setId(id); mapper.updateById(report);
        return ApiResponse.ok(mapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable Long id) { mapper.deleteById(id); return ApiResponse.ok(); }
}
