package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.FbAdReports;
import com.lmserver.mapper.fb.FbAdReportsMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fb/reports")
@RequiredArgsConstructor
public class FbAdReportController {

    private final FbAdReportsMapper mapper;

    @GetMapping("/list")
    public PagedResponse<FbAdReports> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String productName) {
        var qw = new LambdaQueryWrapper<FbAdReports>().eq(FbAdReports::getUserId, principal.getUserId());
        if (productName != null && !productName.isBlank()) qw.eq(FbAdReports::getProductName, productName);
        qw.orderByDesc(FbAdReports::getReportDate);
        var pg = mapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @PostMapping("/create")
    public ApiResponse<FbAdReports> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody FbAdReports r) { r.setUserId(principal.getUserId()); mapper.insert(r); return ApiResponse.ok(r); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { mapper.deleteById(id); return ApiResponse.ok(); }
}
