package com.lmserver.controller.fb;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.FbExtractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * FB 数据提取控制器 — /api/fb/extract/*。
 * 对齐设计文档 v1.2：动态分组解析/去重/回流过滤/异步Sheets。
 */
@Slf4j
@RestController
@RequestMapping("/api/fb/extract")
@RequiredArgsConstructor
public class FbExtractController {

    private final FbExtractService extractService;

    /**
     * 解析提取文本 → 结构化数据（含尾部校验）。
     * 流程：数据透视表~总成效 → 动态分组 → 去重 → 回流过滤 → 校验
     */
    @PostMapping("/parse")
    public ApiResponse<Map<String, Object>> parse(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) return ApiResponse.fail("文本不能为空");

        try {
            FbExtractService.ParseResult result = extractService.parseExtract(text);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("data", result.data());
            resp.put("warnings", result.warnings());
            resp.put("groupSize", result.groupSize());
            resp.put("validation", result.validation());
            return ApiResponse.ok(resp);
        } catch (Exception e) {
            log.error("解析失败", e);
            return ApiResponse.fail("解析失败: " + e.getMessage());
        }
    }

    /**
     * 检查重复 — 查询已存在的记录，避免重复导入。
     */
    @PostMapping("/check-duplicates")
    public ApiResponse<List<Map<String, String>>> checkDuplicates(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String productName = (String) body.get("product_name");
        String lineName = (String) body.getOrDefault("line_name", "");
        String reportDate = (String) body.get("report_date");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> records = (List<Map<String, String>>) body.getOrDefault("records", List.of());

        List<Map<String, String>> duplicates = extractService.checkDuplicates(
                principal.getUserId(), productName, lineName, reportDate, records);
        return ApiResponse.ok(duplicates);
    }

    /**
     * 保存提取数据（含异步写 Google Sheets）。
     * 写入 fb_ad_reports 表，持久化 SheetsSyncLog，异步写 Sheets。
     */
    @PostMapping("/save")
    public ApiResponse<Integer> save(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String productName = (String) body.get("product_name");
        String lineName = (String) body.getOrDefault("line_name", "");
        String reportDate = (String) body.get("report_date");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> records = (List<Map<String, String>>) body.getOrDefault("records", List.of());

        if (productName == null || reportDate == null) {
            return ApiResponse.fail("产品名和报告日期不能为空");
        }
        int saved = extractService.saveExtract(
                principal.getUserId(), productName, lineName, reportDate, records);
        return ApiResponse.ok(saved);
    }
}
