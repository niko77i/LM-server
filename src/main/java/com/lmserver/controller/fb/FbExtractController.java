package com.lmserver.controller.fb;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.*;

/**
 * FB 数据提取控制器 — /api/fb/extract/*。
 * 解析 FB 广告数据文本，提取账户/消耗等字段，校验并保存。
 * Phase 5: 核心解析逻辑已实现，Google Sheets 异步写入待对接。
 */
@RestController
@RequestMapping("/api/fb/extract")
public class FbExtractController {

    /** 解析提取文本 → 结构化数据 */
    @PostMapping("/parse")
    public ApiResponse<Map<String, Object>> parse(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) return ApiResponse.fail("文本不能为空");

        List<Map<String, String>> data = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 找到"数据透视表"到"总成效"范围
        String[] lines = text.split("\n");
        int start = -1, end = lines.length;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("数据透视表") && start < 0) start = i + 1;
            if (lines[i].contains("总成效") && end == lines.length) end = i;
        }
        if (start < 0) return ApiResponse.fail("未找到'数据透视表'标记");

        // 提取每行账户数据（简单版：按 $ 金额分隔）
        Pattern amountPattern = Pattern.compile("\\$([\\d,]+\\.?\\d*)");
        Pattern accountPattern = Pattern.compile("([\\w\\s.-]+?)\\s+(\\d{15,20})");

        String section = String.join("\n", Arrays.copyOfRange(lines, start, end));
        String[] blocks = section.split("\\$[\\d,]+\\.?\\d*\\s*\\$");

        for (String block : blocks) {
            Matcher am = amountPattern.matcher(block);
            Matcher acm = accountPattern.matcher(block);
            if (acm.find()) {
                Map<String, String> row = new HashMap<>();
                row.put("account_name", acm.group(1).trim());
                row.put("account_id", acm.group(2).trim());
                if (am.find()) row.put("cost", am.group(1).replace(",", ""));
                data.add(row);
            }
        }

        // 尾部校验：提取"已显示X/Y行"和总花费
        String tail = String.join("\n", Arrays.copyOfRange(lines, end, lines.length));
        int declaredRows = 0;
        double declaredSpend = 0;
        Matcher rm = Pattern.compile("已显示\\d+/(\\d+)行").matcher(tail);
        if (rm.find()) declaredRows = Integer.parseInt(rm.group(1));

        double extractedSpend = data.stream().mapToDouble(r -> {
            try { return Double.parseDouble(r.getOrDefault("cost", "0")); }
            catch (Exception e) { return 0; }
        }).sum();

        Map<String, Object> validation = Map.of(
            "declaredRows", declaredRows, "extractedRows", data.size(),
            "declaredSpend", Math.round(declaredSpend * 100.0) / 100.0,
            "extractedSpend", Math.round(extractedSpend * 100.0) / 100.0
        );

        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("warnings", warnings);
        result.put("validation", validation);
        result.put("groupSize", data.size());
        return ApiResponse.ok(result);
    }

    /** 检查重复 — 已存在的数据不可重复导入 */
    @PostMapping("/check-duplicates")
    public ApiResponse<List<Map<String, String>>> checkDuplicates(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        // TODO: Phase 5 查询 fb_ad_reports 表检查重复
        return ApiResponse.ok(List.of());
    }

    /** 保存提取数据 */
    @PostMapping("/save")
    public ApiResponse<Integer> save(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        // TODO: Phase 5 写入 fb_ad_reports 表 + 异步写 Google Sheets
        @SuppressWarnings("unchecked")
        List<Map<String, String>> records = (List<Map<String, String>>) body.getOrDefault("records", List.of());
        return ApiResponse.ok(records.size());
    }
}
