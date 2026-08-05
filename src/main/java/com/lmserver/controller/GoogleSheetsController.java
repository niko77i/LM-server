package com.lmserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.sheets.FbReportRow;
import com.lmserver.dto.sheets.ZuobiaoRow;
import com.lmserver.entity.common.Config;
import com.lmserver.entity.common.Users;
import com.lmserver.mapper.common.ConfigMapper;
import com.lmserver.mapper.common.UsersMapper;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Google Sheets 集成控制器 — 对齐 Python main.py google_sheets_update_zuobiao。
 */
@Slf4j
@RestController
@RequestMapping("/api/google-sheets")
@RequiredArgsConstructor
public class GoogleSheetsController {

    private final GoogleSheetsService sheetsService;
    @Autowired private UsersMapper usersMapper;
    @Autowired private ConfigMapper configMapper;
    @Autowired private com.lmserver.mapper.gg.AdReportsMapper adReportsMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** GG 做表 upsert — 对齐 Python /api/google-sheets/update-zuobiao */
    @PostMapping("/update-zuobiao")
    public ApiResponse<Map<String, Object>> updateZuobiao(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {

        String productName = str(body, "product_name");
        String region = str(body, "region");
        String reportDate = str(body, "report_date");
        String salesPerson = str(body, "sales_person");
        String spreadsheetId = str(body, "spreadsheet_id");
        Object agencyRatioObj = body.get("agency_ratio");
        Integer agencyRatio = agencyRatioObj instanceof Number n ? n.intValue() : null;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawRows = (List<Map<String, Object>>) body.getOrDefault("rows", List.of());

        if (productName == null || productName.isBlank())
            return ApiResponse.fail("产品名不能为空");
        if (rawRows.isEmpty())
            return ApiResponse.fail("做表数据不能为空");

        // 获取 operator_name：优先用 display_name
        String operatorName = getOperatorName(principal.getUserId(), principal.getUsername());

        // 按月份匹配表格
        if (spreadsheetId == null || spreadsheetId.isBlank()) {
            spreadsheetId = resolveSpreadsheetId(principal.getUserId(), operatorName, reportDate);
            if (spreadsheetId == null)
                return ApiResponse.fail("请先在个人中心配置 Google 表格");
        }

        List<ZuobiaoRow> rows = new ArrayList<>();
        for (Map<String, Object> r : rawRows) {
            rows.add(ZuobiaoRow.builder()
                    .account(str(r, "account"))
                    .customerId(str(r, "customerId"))
                    .cost(toDouble(r.get("cost")))
                    .campaign(str(r, "campaign"))
                    .isYanghu(Boolean.TRUE.equals(r.get("is_yanghu")))
                    .impressions(toInt(r.get("impressions")))
                    .clicks(toInt(r.get("clicks")))
                    .installs(toDouble(r.get("installs")))
                    .inAppActions(toDouble(r.get("inAppActions")))
                    .costPerInApp(toDouble(r.get("costPerInApp")))
                    .build());
        }

        Map<String, Object> result = sheetsService.upsertZuobiao(
                spreadsheetId, rows, productName, region, reportDate,
                salesPerson, agencyRatio, operatorName);

        if (result.containsKey("error"))
            return ApiResponse.fail(result.get("error").toString());

        // 同步写 ad_reports 表（排除养户行，去重聚合后 upsert）
        int dbSaved = syncToAdReports(principal.getUserId(), productName, region, reportDate, rows);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("sheets_status", "syncing");
        resp.put("updated", result.get("updated"));
        resp.put("inserted", result.get("inserted"));
        resp.put("db_saved", dbSaved);
        return ApiResponse.ok(resp);
    }

    /** FB 做表 upsert */
    @PostMapping("/fb-reports/upsert")
    public ApiResponse<Map<String, Object>> upsertFbReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {

        String spreadsheetId = str(body, "spreadsheet_id");
        String productName = str(body, "product_name");
        String reportDate = str(body, "report_date");
        String region = str(body, "region");
        String salesPerson = str(body, "sales_person");
        String operatorName = getOperatorName(principal.getUserId(), principal.getUsername());
        Object agencyRatioObj = body.get("agency_ratio");
        Integer agencyRatio = agencyRatioObj instanceof Number n ? n.intValue() : null;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawRows = (List<Map<String, Object>>) body.getOrDefault("rows", List.of());

        List<FbReportRow> rows = rawRows.stream().map(r -> FbReportRow.builder()
                .accountName(str(r, "account_name"))
                .accountId(str(r, "account_id"))
                .cost(toDouble(r.get("cost")))
                .channelNo(str(r, "channel_no"))
                .build()).toList();

        Map<String, Object> result = sheetsService.upsertFbReports(
                spreadsheetId, rows, principal.getUserId(), productName,
                reportDate, region, salesPerson, agencyRatio, operatorName);

        if (result.containsKey("error"))
            return ApiResponse.fail(result.get("error").toString());
        return ApiResponse.ok(result);
    }

    // ── operator_name：display_name 优先，回退 username ──

    private String getOperatorName(Long userId, String username) {
        try {
            Users user = usersMapper.selectById(userId);
            if (user != null) {
                String dn = user.getDisplayName();
                if (dn != null && !dn.isBlank()) return dn;
                if (user.getUsername() != null && !user.getUsername().isBlank()) return user.getUsername();
            }
        } catch (Exception ignored) {}
        return username != null ? username : "";
    }

    // ── 按月份匹配表格：{operatorName}{YYYY.MM} ──

    private String resolveSpreadsheetId(Long userId, String operatorName, String reportDate) {
        try {
            String monthKey = reportDate.length() >= 7 ? reportDate.substring(0, 7).replace("-", ".") : "";
            String expected = operatorName + monthKey;

            // 读取用户 Google Sheets 配置 (key: google_sheets_{userId})
            Config cfg = configMapper.selectOne(
                    new LambdaQueryWrapper<Config>().eq(Config::getKey, "google_sheets_" + userId));
            if (cfg == null || cfg.getValue() == null) return null;

            List<Map<String, String>> sheets = objectMapper.readValue(cfg.getValue(),
                    new TypeReference<List<Map<String, String>>>() {});

            // 按月份匹配
            for (var s : sheets) {
                String name = s.getOrDefault("spreadsheet_name", "");
                if (name.contains(expected)) return s.get("spreadsheet_id");
            }
            // 回退到第一个配置的表格
            if (!sheets.isEmpty()) return sheets.get(0).get("spreadsheet_id");
        } catch (Exception e) {
            log.warn("解析 Google Sheets 配置失败: {}", e.getMessage());
        }
        return null;
    }

    // ── 基础读写 ──

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

    // ── 工具方法 ──

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString().trim() : "";
    }

    private double toDouble(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0; }
    }

    /** 同步做表数据到 ad_reports 表 — 对齐 Python 聚合+upsert 逻辑 */
    private int syncToAdReports(Long userId, String productName, String region,
            String reportDate, List<ZuobiaoRow> rows) {
        int saved = 0;
        try {
            List<ZuobiaoRow> dbRows = rows.stream()
                    .filter(r -> !r.isYanghu())
                    .filter(r -> r.getCustomerId() != null && !r.getCustomerId().isBlank())
                    .filter(r -> r.getCampaign() != null && !r.getCampaign().isBlank())
                    .toList();
            if (dbRows.isEmpty() || region == null || reportDate == null) return 0;

            Map<String, ZuobiaoRow> aggregated = new LinkedHashMap<>();
            for (ZuobiaoRow row : dbRows) {
                String key = reportDate + "|" + productName + "|"
                        + (row.getAccount() != null ? row.getAccount() : "") + "|"
                        + row.getCustomerId() + "|" + row.getCampaign();
                aggregated.put(key, row);
            }

            java.time.LocalDateTime rptDate = java.time.LocalDate.parse(reportDate).atStartOfDay();

            for (var entry : aggregated.entrySet()) {
                ZuobiaoRow row = entry.getValue();
                var existing = adReportsMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lmserver.entity.gg.AdReports>()
                                .eq(com.lmserver.entity.gg.AdReports::getUserId, userId)
                                .eq(com.lmserver.entity.gg.AdReports::getProductName, productName)
                                .eq(com.lmserver.entity.gg.AdReports::getAccount, row.getAccount())
                                .eq(com.lmserver.entity.gg.AdReports::getCustomerId, row.getCustomerId())
                                .eq(com.lmserver.entity.gg.AdReports::getCampaign, row.getCampaign())
                                .eq(com.lmserver.entity.gg.AdReports::getReportDate, rptDate));

                if (!existing.isEmpty()) {
                    com.lmserver.entity.gg.AdReports ar = existing.get(0);
                    ar.setCost(row.getCost());
                    ar.setImpressions((long) row.getImpressions());
                    ar.setClicks((long) row.getClicks());
                    ar.setInstalls((long) row.getInstalls());
                    ar.setInAppActions(row.getInAppActions());
                    ar.setCostPerInApp(row.getCostPerInApp());
                    ar.setRegion(region);
                    adReportsMapper.updateById(ar);
                } else {
                    com.lmserver.entity.gg.AdReports ar = new com.lmserver.entity.gg.AdReports();
                    ar.setUserId(userId);
                    ar.setProductName(productName);
                    ar.setRegion(region);
                    ar.setReportDate(rptDate);
                    ar.setAccount(row.getAccount());
                    ar.setCustomerId(row.getCustomerId());
                    ar.setCampaign(row.getCampaign());
                    ar.setCost(row.getCost());
                    ar.setImpressions((long) row.getImpressions());
                    ar.setClicks((long) row.getClicks());
                    ar.setInstalls((long) row.getInstalls());
                    ar.setInAppActions(row.getInAppActions());
                    ar.setCostPerInApp(row.getCostPerInApp());
                    ar.setSavedAt(java.time.LocalDateTime.now());
                    adReportsMapper.insert(ar);
                }
                saved++;
            }
        } catch (Exception e) {
            log.warn("同步到 ad_reports 失败: {}", e.getMessage());
        }
        return saved;
    }

    private int toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
}
