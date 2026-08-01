package com.lmserver.controller.fb;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.fb.FbAdReports;
import com.lmserver.mapper.fb.FbAdReportsMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.*;

/**
 * FB 数据提取控制器 — /api/fb/extract/*。
 * 解析 FB 广告数据文本，提取账户/消耗字段，查重后写入数据库。
 */
@Slf4j
@RestController
@RequestMapping("/api/fb/extract")
@RequiredArgsConstructor
public class FbExtractController {

    private final FbAdReportsMapper fbAdReportsMapper;

    /**
     * 解析提取文本 → 结构化数据。
     * 找到"数据透视表"~"总成效"范围，用正则提取每行账户数据。
     * 尾部校验"已显示X/Y行"和总花费。
     */
    @PostMapping("/parse")
    public ApiResponse<Map<String, Object>> parse(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) return ApiResponse.fail("文本不能为空");

        List<Map<String, String>> data = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String[] lines = text.split("\n");

        int start = -1, end = lines.length;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("数据透视表") && start < 0) start = i + 1;
            if (lines[i].contains("总成效") && end == lines.length) end = i;
        }
        if (start < 0) return ApiResponse.fail("未找到'数据透视表'标记");

        // 正则提取：账户名 + Facebook ID + 金额
        Pattern accountPattern = Pattern.compile("(.+?)\\s+(\\d{10,20})\\s*.*?");
        Pattern costPattern = Pattern.compile("\\$([\\d,]+\\.?\\d*)");

        String section = String.join("\n", Arrays.copyOfRange(lines, start, end));
        String[] parts = section.split("\\n\\s*(?=[A-Z])");

        for (String part : parts) {
            Matcher acm = accountPattern.matcher(part);
            Matcher cm = costPattern.matcher(part);
            if (acm.find()) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("account_name", acm.group(1).trim());
                row.put("account_id", acm.group(2).trim());
                row.put("cost", cm.find() ? cm.group(1).replace(",", "") : "0");
                data.add(row);
            }
        }

        // 尾部校验
        String tail = String.join("\n", Arrays.copyOfRange(lines, end, lines.length));
        int declaredRows = 0;
        Matcher rm = Pattern.compile("已显示\\d+/(\\d+)行").matcher(tail);
        if (rm.find()) declaredRows = Integer.parseInt(rm.group(1));

        double spend = data.stream().mapToDouble(r -> {
            try { return Double.parseDouble(r.getOrDefault("cost", "0")); }
            catch (Exception e) { return 0; }
        }).sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("data", data);
        result.put("warnings", warnings);
        result.put("validation", Map.of(
            "declaredRows", declaredRows, "extractedRows", data.size(),
            "extractedSpend", Math.round(spend * 100.0) / 100.0
        ));
        return ApiResponse.ok(result);
    }

    /**
     * 保存提取数据到数据库。
     * 写入 fb_ad_reports 表，使用 INSERT IGNORE 避免重复。
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

        int saved = 0;
        for (Map<String, String> r : records) {
            try {
                FbAdReports report = new FbAdReports();
                report.setUserId(principal.getUserId());
                report.setProductName(productName);
                report.setLineName(lineName);
                report.setReportDate(java.time.LocalDate.parse(reportDate).atStartOfDay());
                report.setAccountName(r.getOrDefault("account_name", ""));
                report.setAccountId(r.getOrDefault("account_id", ""));
                report.setCost(Double.parseDouble(r.getOrDefault("cost", "0")));
                report.setImpressions(0L);
                report.setClicks(0L);
                report.setRegistrations(0L);
                report.setPurchases(0L);
                report.setCostPerPurchase(0.0);
                report.setSavedAt(java.time.LocalDateTime.now());
                fbAdReportsMapper.insert(report);
                saved++;
            } catch (Exception e) {
                log.warn("保存记录失败: {}", e.getMessage());
            }
        }

        log.info("[FB提取] 用户{} 保存{}/{}条 产品:{} 日期:{}",
                principal.getUserId(), saved, records.size(), productName, reportDate);
        return ApiResponse.ok(saved);
    }
}
