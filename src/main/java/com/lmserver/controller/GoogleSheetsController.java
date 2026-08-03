package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.sheets.FbReportRow;
import com.lmserver.dto.sheets.ZuobiaoRow;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Google Sheets 集成控制器 — /api/google-sheets/*。
 * 基础读写 + GG 做表 upsert + FB 做表 upsert。
 */
@Slf4j
@RestController
@RequestMapping("/api/google-sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {

    private final GoogleSheetsService sheetsService;

    /** 读取 Sheet 指定范围 */
    @GetMapping("/read")
    public ApiResponse<List<List<Object>>> read(@RequestParam String spreadsheetId,
            @RequestParam(defaultValue = "A1:Z1000") String range) {
        try {
            List<List<Object>> values = sheetsService.read(spreadsheetId, range);
            return ApiResponse.ok(values != null ? values : List.of());
        } catch (Exception e) {
            log.error("Sheet 读取失败", e);
            return ApiResponse.fail("读取失败: " + e.getMessage());
        }
    }

    /** 写入 Sheet */
    @PostMapping("/write")
    public ApiResponse<String> write(@RequestBody Map<String, Object> body) {
        try {
            String spreadsheetId = (String) body.get("spreadsheet_id");
            String range = (String) body.getOrDefault("range", "A1");
            @SuppressWarnings("unchecked")
            List<List<Object>> values = (List<List<Object>>) body.get("values");
            sheetsService.write(spreadsheetId, range, values);
            return ApiResponse.ok("写入成功");
        } catch (Exception e) {
            log.error("Sheet 写入失败", e);
            return ApiResponse.fail("写入失败: " + e.getMessage());
        }
    }

    /** GG 做表 upsert — 14列去重/扩容/批量更新 */
    @PostMapping("/zuobiao/upsert")
    public ApiResponse<Map<String, Object>> upsertZuobiao(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String spreadsheetId = (String) body.get("spreadsheet_id");
        String productName = (String) body.get("product_name");
        String reportDate = (String) body.get("report_date");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawRows = (List<Map<String, Object>>) body.getOrDefault("rows", List.of());

        List<ZuobiaoRow> rows = rawRows.stream().map(r -> ZuobiaoRow.builder()
                .date((String) r.get("date")).operator((String) r.get("operator"))
                .customerName((String) r.get("customer_name")).salesPerson((String) r.get("sales_person"))
                .country((String) r.get("country")).channelNo((String) r.get("channel_no"))
                .seriesName((String) r.get("series_name")).packageName((String) r.get("package_name"))
                .accountId((String) r.get("account_id")).imageUrl((String) r.get("image_url"))
                .landingPage((String) r.get("landing_page")).accountCost(toDouble(r.get("account_cost")))
                .build()).toList();

        Map<String, Object> result = sheetsService.upsertZuobiao(spreadsheetId, rows, productName, reportDate);
        if (result.containsKey("error")) return ApiResponse.fail(result.get("error").toString());
        return ApiResponse.ok(result);
    }

    /** FB 做表 upsert — 12列去重/扩容/批量更新 */
    @PostMapping("/fb-reports/upsert")
    public ApiResponse<Map<String, Object>> upsertFbReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String spreadsheetId = (String) body.get("spreadsheet_id");
        String productName = (String) body.get("product_name");
        String reportDate = (String) body.get("report_date");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawRows = (List<Map<String, Object>>) body.getOrDefault("rows", List.of());

        List<FbReportRow> rows = rawRows.stream().map(r -> FbReportRow.builder()
                .date((String) r.get("date")).operator((String) r.get("operator"))
                .accountName((String) r.get("account_name")).accountId((String) r.get("account_id"))
                .cost(toDouble(r.get("cost"))).reportToClient(toDouble(r.get("report_to_client")))
                .customerName((String) r.get("customer_name")).salesPerson((String) r.get("sales_person"))
                .country((String) r.get("country")).channelNo((String) r.get("channel_no"))
                .platformActual(toDouble(r.get("platform_actual"))).agencyRatio(toDouble(r.get("agency_ratio")))
                .build()).toList();

        Map<String, Object> result = sheetsService.upsertFbReports(
                spreadsheetId, rows, principal.getUserId(), productName, reportDate);
        if (result.containsKey("error")) return ApiResponse.fail(result.get("error").toString());
        return ApiResponse.ok(result);
    }

    @PostMapping("/sync-trigger")
    public ApiResponse<String> syncTrigger(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok("同步任务已创建"); // TODO: 异步Sheet同步
    }

    private Double toDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0.0; }
    }
}
