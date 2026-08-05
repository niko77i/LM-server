package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.AdReports;
import com.lmserver.entity.common.Config;
import com.lmserver.mapper.common.ConfigMapper;
import com.lmserver.mapper.gg.AdReportsMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
/**
 * GG 广告报告控制器 — /api/ad-reports/*，GG广告投放数据的CRUD
 */

@RestController
@RequestMapping("/api/ad-reports")
@RequiredArgsConstructor
public class AdReportController {

    private final AdReportsMapper mapper;
    private final ConfigMapper configMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public ApiResponse<Void> delete(@PathVariable Long id) { mapper.deleteById(id); return ApiResponse.ok(); }

    @GetMapping("/export")
    public void export(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String productName, HttpServletResponse resp) throws IOException {
        var qw = new LambdaQueryWrapper<AdReports>().eq(AdReports::getUserId, principal.getUserId());
        if (productName != null && !productName.isBlank()) qw.eq(AdReports::getProductName, productName);
        var list = mapper.selectList(qw);
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=ad-reports.csv");
        resp.getWriter().write("日期,产品,地区,账户,客户ID,广告系列,消耗,展示,点击,安装,应用内操作\n");
        for (AdReports r : list) {
            resp.getWriter().write(String.format("%s,%s,%s,%s,%s,%s,%.2f,%d,%d,%.0f,%.0f\n",
                    r.getReportDate(), r.getProductName(), r.getRegion(), r.getAccount(),
                    r.getCustomerId(), r.getCampaign(), r.getCost() != null ? r.getCost() : 0,
                    r.getImpressions() != null ? r.getImpressions() : 0,
                    r.getClicks() != null ? r.getClicks() : 0,
                    r.getInstalls() != null ? r.getInstalls() : 0,
                    r.getInAppActions() != null ? r.getInAppActions() : 0));
        }
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Integer> batchDelete(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, List<Long>> body) {
        int c = 0;
        for (Long id : body.getOrDefault("ids", List.of())) { mapper.deleteById(id); c++; }
        return ApiResponse.ok(c);
    }

    @PostMapping("/analysis")
    public ApiResponse<String> analysis(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        // 从数据库读取 AI 配置
        Config aiConfig = configMapper.selectById("ai_analysis");
        if (aiConfig == null || aiConfig.getValue() == null) return ApiResponse.fail("AI配置不存在");
        try {
            Map<String, Object> cfg = objectMapper.readValue(aiConfig.getValue(), Map.class);
            String endpoint = (String) cfg.get("endpoint");
            String apiKey = (String) cfg.get("api_key");
            String model = (String) cfg.getOrDefault("model", "deepseek-v4-flash");
            // 获取用户最近的报告数据作为上下文
            var reports = mapper.selectList(new LambdaQueryWrapper<AdReports>()
                    .eq(AdReports::getUserId, principal.getUserId()).orderByDesc(AdReports::getReportDate).last("LIMIT 50"));
            StringBuilder ctx = new StringBuilder("报告数据:\n");
            for (var r : reports) ctx.append(String.format("%s %s cost:%.2f clicks:%d\n",
                    r.getReportDate(), r.getProductName(), r.getCost() != null ? r.getCost() : 0,
                    r.getClicks() != null ? r.getClicks() : 0));
            // 调用 LLM
            Map<String, Object> reqBody = Map.of("model", model, "messages", List.of(
                    Map.of("role", "system", "content", "你是广告投放分析助手。根据数据回答问题。"),
                    Map.of("role", "user", "content", ctx + "\n问题: " + question)));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            ResponseEntity<Map> resp = restTemplate.exchange(endpoint, HttpMethod.POST,
                    new HttpEntity<>(reqBody, headers), Map.class);
            Map<String, Object> respBody = resp.getBody();
            List<Map<String, Object>> choices = respBody != null ? (List<Map<String, Object>>) respBody.get("choices") : List.of();
            String answer = !choices.isEmpty() ? (String) ((Map<String, Object>) choices.get(0).get("message")).get("content") : "无响应";
            return ApiResponse.ok(answer);
        } catch (Exception e) { return ApiResponse.fail("AI分析失败: " + e.getMessage()); }
    }

    @PostMapping("/dedup-check")
    public ApiResponse<Map<String,Integer>> dedupCheck(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Map<String,String>> rows = (List<Map<String,String>>) body.getOrDefault("rows", List.of());
        int dup=0; for (var r : rows) { var existing = mapper.selectList(new LambdaQueryWrapper<AdReports>()
                .eq(AdReports::getUserId, principal.getUserId()).eq(AdReports::getCustomerId, r.get("customer_id"))
                .eq(AdReports::getCampaign, r.get("campaign")).eq(AdReports::getReportDate, java.time.LocalDate.parse(r.get("report_date"))));
                if (!existing.isEmpty()) dup++; }
        return ApiResponse.ok(Map.of("duplicates", dup, "total", rows.size()));
    }

    @GetMapping("/products")
    public ApiResponse<List<String>> products(@AuthenticationPrincipal UserPrincipal principal) {
        var list = mapper.selectList(new LambdaQueryWrapper<AdReports>().select(AdReports::getProductName)
                .eq(AdReports::getUserId, principal.getUserId()).groupBy(AdReports::getProductName));
        return ApiResponse.ok(list.stream().map(AdReports::getProductName).distinct().toList());
    }
}
