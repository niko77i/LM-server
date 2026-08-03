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
import java.util.Map;
import org.springframework.web.bind.annotation.*;
/**
 * FB 广告报告控制器 — /api/fb/reports/*，FB广告投放数据的导入查询
 */

@RestController
@RequestMapping("/api/fb/reports")
@RequiredArgsConstructor
public class FbAdReportController {

    private final FbAdReportsMapper mapper;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
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
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<FbAdReports> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody FbAdReports r) { r.setUserId(principal.getUserId()); mapper.insert(r); return ApiResponse.ok(r); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { mapper.deleteById(id); return ApiResponse.ok(); }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String productName) {
        var qw = new LambdaQueryWrapper<FbAdReports>().eq(FbAdReports::getUserId, principal.getUserId());
        if (productName != null && !productName.isBlank()) qw.eq(FbAdReports::getProductName, productName);
        var list = mapper.selectList(qw);
        double totalCost = list.stream().mapToDouble(r -> r.getCost() != null ? r.getCost() : 0).sum();
        long totalPurchases = list.stream().mapToLong(r -> r.getPurchases() != null ? r.getPurchases() : 0).sum();
        return ApiResponse.ok(Map.of("totalCost", totalCost, "totalPurchases", totalPurchases,
                "recordCount", list.size()));
    }

    @GetMapping("/export")
    public void export(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String productName, jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException {
        var qw = new LambdaQueryWrapper<FbAdReports>().eq(FbAdReports::getUserId, principal.getUserId());
        if (productName != null && !productName.isBlank()) qw.eq(FbAdReports::getProductName, productName);
        var list = mapper.selectList(qw);
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=fb-reports.csv");
        var w = resp.getWriter();
        w.write("日期,产品,线名,账户,账户ID,消耗,展示,点击,注册,购买,CPA\n");
        for (FbAdReports r : list) w.write(String.format("%s,%s,%s,%s,%s,%.2f,%d,%d,%d,%d,%.2f\n",
                r.getReportDate(), r.getProductName(), r.getLineName(), r.getAccountName(),
                r.getAccountId(), r.getCost() != null ? r.getCost() : 0,
                r.getImpressions() != null ? r.getImpressions() : 0,
                r.getClicks() != null ? r.getClicks() : 0,
                r.getRegistrations() != null ? r.getRegistrations() : 0,
                r.getPurchases() != null ? r.getPurchases() : 0,
                r.getCostPerPurchase() != null ? r.getCostPerPurchase() : 0));
    }

    private final com.lmserver.mapper.gg.SheetsSyncLogMapper syncLogMapper;
    @PostMapping("/sync-retry/{logId}")
    public ApiResponse<Void> syncRetry(@PathVariable Long logId) {
        var log = syncLogMapper.selectById(logId);
        if (log != null) { log.setStatus("pending"); log.setRetryCount((log.getRetryCount() != null ? log.getRetryCount() : 0) + 1); syncLogMapper.updateById(log); }
        return ApiResponse.ok();
    }
}
