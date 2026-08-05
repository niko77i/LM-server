package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.AdReports;
import com.lmserver.mapper.gg.AdReportsMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 广告报告分析控制器 — 对齐 Python ad_reports_dashboard/trends/compare/dates/analyze。
 */
@Slf4j
@RestController
@RequestMapping("/api/ad-reports")
@RequiredArgsConstructor
public class AdReportAnalysisController {

    private final AdReportsMapper adReportsMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    // ═══════ Dashboard ═══════

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "") String productName, @RequestParam(defaultValue = "") String region,
            @RequestParam(defaultValue = "") String fromDate, @RequestParam(defaultValue = "") String toDate) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, fromDate, toDate, params);

        // 当前周期汇总
        Map<String, Object> summary = querySummary(where, params);
        double totalCost = toDouble(summary.get("total_cost"));
        double totalImpressions = toDouble(summary.get("total_impressions"));
        double totalClicks = toDouble(summary.get("total_clicks"));
        double totalInstalls = toDouble(summary.get("total_installs"));
        double totalInApp = toDouble(summary.get("total_in_app"));
        double avgCpi = totalInApp > 0 ? Math.round(totalCost / totalInApp * 100.0) / 100.0 : 0;
        double avgCtr = totalImpressions > 0 ? Math.round(totalClicks / totalImpressions * 10000.0) / 10000.0 : 0;
        double avgCvr = totalClicks > 0 ? Math.round(totalInstalls / totalClicks * 10000.0) / 10000.0 : 0;

        Map<String, Object> summaryMap = new LinkedHashMap<>();
        summaryMap.put("total_cost", Math.round(totalCost * 100.0) / 100.0);
        summaryMap.put("total_impressions", (long) totalImpressions);
        summaryMap.put("total_clicks", (long) totalClicks);
        summaryMap.put("total_installs", (long) totalInstalls);
        summaryMap.put("total_in_app", Math.round(totalInApp * 100.0) / 100.0);
        summaryMap.put("avg_cpi", avgCpi);
        summaryMap.put("avg_ctr", avgCtr);
        summaryMap.put("avg_cvr", avgCvr);

        // 环比
        Map<String, Object> periodCompare = calcPeriodCompare(userId, productName, region, fromDate, toDate, summaryMap);

        // 异常检测
        List<Map<String, Object>> anomalies = detectAnomalies(where, params);

        // Campaign 分组统计
        List<Map<String, Object>> campaigns = queryCampaignStats(where, params);

        // 素材关联数
        long assetCount = 0;
        if (!productName.isBlank()) {
            assetCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_assets pa JOIN products p ON pa.product_id=p.id WHERE p.product_name=?",
                Long.class, productName);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summaryMap);
        result.put("period_compare", periodCompare);
        result.put("anomalies", anomalies);
        result.put("campaigns", campaigns);
        result.put("asset_count", assetCount);
        return ApiResponse.ok(result);
    }

    // ═══════ Trends ═══════

    @GetMapping("/trends")
    public ApiResponse<Map<String, Object>> trends(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "") String productName, @RequestParam(defaultValue = "") String region,
            @RequestParam(defaultValue = "") String fromDate, @RequestParam(defaultValue = "") String toDate,
            @RequestParam(defaultValue = "cpi") String metric,
            @RequestParam(defaultValue = "product_name") String groupBy) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, fromDate, toDate, params);
        String groupCol = "product_name".equals(groupBy) ? "product_name" : "campaign";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT " + groupCol + " AS name, report_date, SUM(cost) AS total_cost, SUM(installs) AS total_installs, "
            + "SUM(impressions) AS total_impressions, SUM(clicks) AS total_clicks, SUM(in_app_actions) AS total_in_app "
            + "FROM ad_reports WHERE " + where + " GROUP BY " + groupCol + ", report_date ORDER BY report_date",
            params.toArray());

        Map<String, List<Map<String, Object>>> seriesMap = new LinkedHashMap<>();
        for (var r : rows) {
            String name = String.valueOf(r.get("name"));
            seriesMap.computeIfAbsent(name, k -> new ArrayList<>())
                .add(Map.of("date", r.get("report_date"), "value", computeMetric(r, metric)));
        }

        List<Map<String, Object>> series = new ArrayList<>();
        for (var e : seriesMap.entrySet()) {
            series.add(Map.of("name", e.getKey(), "data", e.getValue()));
        }
        return ApiResponse.ok(Map.of("series", series));
    }

    // ═══════ Compare ═══════

    @GetMapping("/compare")
    public ApiResponse<List<Map<String, Object>>> compare(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "") String productName, @RequestParam(defaultValue = "") String region,
            @RequestParam(defaultValue = "product_name") String groupBy,
            @RequestParam(defaultValue = "") String fromDate, @RequestParam(defaultValue = "") String toDate,
            @RequestParam(defaultValue = "cpi") String sortBy) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, fromDate, toDate, params);
        String groupCol = "product_name".equals(groupBy) ? "product_name" : "campaign";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT " + groupCol + " AS name, SUM(cost) AS total_cost, SUM(impressions) AS total_impressions, "
            + "SUM(clicks) AS total_clicks, SUM(installs) AS total_installs, SUM(in_app_actions) AS total_in_app "
            + "FROM ad_reports WHERE " + where + " GROUP BY " + groupCol, params.toArray());

        List<Map<String, Object>> items = new ArrayList<>();
        for (var r : rows) {
            double cost = toDouble(r.get("total_cost"));
            double imp = toDouble(r.get("total_impressions"));
            double clicks = toDouble(r.get("total_clicks"));
            double inst = toDouble(r.get("total_installs"));
            double inApp = toDouble(r.get("total_in_app"));
            items.add(Map.of(
                "name", r.get("name"),
                "total_cost", Math.round(cost * 100.0) / 100.0,
                "total_impressions", (long) imp,
                "total_clicks", (long) clicks,
                "total_installs", (long) inst,
                "total_in_app", Math.round(inApp * 100.0) / 100.0,
                "cpi", inApp > 0 ? Math.round(cost / inApp * 100.0) / 100.0 : 0,
                "ctr", imp > 0 ? Math.round(clicks / imp * 10000.0) / 10000.0 : 0,
                "cvr", clicks > 0 ? Math.round(inst / clicks * 10000.0) / 10000.0 : 0));
        }
        items.sort((a, b) -> Double.compare(toDouble(b.get(sortBy)), toDouble(a.get(sortBy))));
        return ApiResponse.ok(items);
    }

    // ═══════ Stats ═══════

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "") String productName, @RequestParam(defaultValue = "") String region,
            @RequestParam(defaultValue = "") String fromDate, @RequestParam(defaultValue = "") String toDate) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, fromDate, toDate, params);

        Map<String, Object> s = querySummary(where, params);
        s.put("record_count", jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ad_reports WHERE " + where, Long.class, params.toArray()));
        return ApiResponse.ok(s);
    }

    // ═══════ Dates ═══════

    @GetMapping("/dates")
    public ApiResponse<Map<String, Object>> dates(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "") String productName, @RequestParam(defaultValue = "") String region) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        List<String> whereParts = new ArrayList<>();
        whereParts.add("user_id=?"); params.add(userId);
        if (!productName.isBlank()) {
            String[] names = productName.split(",");
            if (names.length == 1) { whereParts.add("product_name=?"); params.add(names[0].trim()); }
            else { whereParts.add("product_name IN (" + String.join(",", Collections.nCopies(names.length, "?")) + ")");
                   for (String n : names) params.add(n.trim()); }
        }
        if (!region.isBlank()) { whereParts.add("region=?"); params.add(region); }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT report_date, COUNT(*) AS cnt FROM ad_reports WHERE "
            + String.join(" AND ", whereParts) + " GROUP BY report_date ORDER BY report_date",
            params.toArray());

        Map<String, Object> datesMap = new LinkedHashMap<>();
        for (var r : rows) datesMap.put(String.valueOf(r.get("report_date")), r.get("cnt"));
        return ApiResponse.ok(Map.of("dates", datesMap));
    }

    // ═══════ Analyze ═══════

    @PostMapping("/analyze")
    public ApiResponse<Map<String, Object>> analyze(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        Long userId = principal.getUserId();
        String productName = String.valueOf(body.getOrDefault("product_name", ""));
        String region = String.valueOf(body.getOrDefault("region", ""));
        String question = String.valueOf(body.getOrDefault("question", ""));
        if (question.isBlank()) return ApiResponse.fail("请输入分析问题");

        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, "", "", params);

        // 构建数据摘要
        Map<String, Object> summary = querySummary(where, params);
        List<Map<String, Object>> campaigns = queryCampaignStats(where, params);
        Map<String, Object> dataCtx = Map.of("summary", summary, "top_campaigns", campaigns.subList(0, Math.min(5, campaigns.size())));

        // 返回数据上下文供前端调用 AI
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);
        result.put("data_context", dataCtx);
        result.put("suggestion", "请前端将数据上下文提交至配置的AI服务进行分析");
        return ApiResponse.ok(result);
    }

    // ═══════ 辅助方法 ═══════

    private String buildWhere(Long userId, String productName, String region,
            String fromDate, String toDate, List<Object> params) {
        List<String> parts = new ArrayList<>();
        parts.add("user_id=?"); params.add(userId);
        if (!productName.isBlank()) {
            String[] names = productName.split(",");
            if (names.length == 1) { parts.add("product_name=?"); params.add(names[0].trim()); }
            else {
                parts.add("product_name IN (" + String.join(",", Collections.nCopies(names.length, "?")) + ")");
                for (String n : names) params.add(n.trim());
            }
        }
        if (!region.isBlank()) { parts.add("region=?"); params.add(region); }
        if (!fromDate.isBlank()) { parts.add("report_date >= ?"); params.add(fromDate); }
        if (!toDate.isBlank()) { parts.add("report_date <= ?"); params.add(toDate); }
        return String.join(" AND ", parts);
    }

    private Map<String, Object> querySummary(String where, List<Object> params) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT COALESCE(SUM(cost),0) AS total_cost, COALESCE(SUM(impressions),0) AS total_impressions, "
                + "COALESCE(SUM(clicks),0) AS total_clicks, COALESCE(SUM(installs),0) AS total_installs, "
                + "COALESCE(SUM(in_app_actions),0) AS total_in_app FROM ad_reports WHERE " + where,
                params.toArray());
        } catch (Exception e) {
            return Map.of("total_cost", 0, "total_impressions", 0, "total_clicks", 0, "total_installs", 0, "total_in_app", 0);
        }
    }

    private List<Map<String, Object>> queryCampaignStats(String where, List<Object> params) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT campaign, SUM(cost) AS total_cost, SUM(installs) AS total_installs, "
            + "SUM(impressions) AS total_impressions, SUM(clicks) AS total_clicks, SUM(in_app_actions) AS total_in_app "
            + "FROM ad_reports WHERE " + where + " AND campaign IS NOT NULL AND campaign != '' "
            + "GROUP BY campaign ORDER BY total_cost DESC", params.toArray());
        List<Map<String, Object>> result = new ArrayList<>();
        for (var r : rows) {
            double cost = toDouble(r.get("total_cost"));
            double imp = toDouble(r.get("total_impressions"));
            double clicks = toDouble(r.get("total_clicks"));
            double inst = toDouble(r.get("total_installs"));
            double inApp = toDouble(r.get("total_in_app"));
            result.add(Map.of(
                "campaign", r.get("campaign"),
                "total_cost", Math.round(cost * 100.0) / 100.0,
                "total_installs", Math.round(inst * 100.0) / 100.0,
                "total_impressions", (long) imp,
                "total_clicks", (long) clicks,
                "total_in_app", Math.round(inApp * 100.0) / 100.0,
                "avg_cpi", inApp > 0 ? Math.round(cost / Math.max(inApp, 1) * 100.0) / 100.0 : 0,
                "ctr", imp > 0 ? Math.round(clicks / Math.max(imp, 1) * 10000.0) / 10000.0 : 0,
                "cvr", clicks > 0 ? Math.round(inst / Math.max(clicks, 1) * 10000.0) / 10000.0 : 0));
        }
        return result;
    }

    private Map<String, Object> calcPeriodCompare(Long userId, String productName, String region,
            String fromDate, String toDate, Map<String, Object> currentSummary) {
        Map<String, Object> period = new LinkedHashMap<>();
        if (fromDate.isBlank() || toDate.isBlank()) return period;
        try {
            LocalDate fd = LocalDate.parse(fromDate), td = LocalDate.parse(toDate);
            long days = ChronoUnit.DAYS.between(fd, td) + 1;
            LocalDate prevFrom = fd.minusDays(days), prevTo = fd.minusDays(1);

            List<Object> params = new ArrayList<>();
            String where = buildWhere(userId, productName, region, prevFrom.toString(), prevTo.toString(), params);
            Map<String, Object> prev = querySummary(where, params);

            double prevCost = toDouble(prev.get("total_cost"));
            double prevInstalls = toDouble(prev.get("total_installs"));
            double prevInApp = toDouble(prev.get("total_in_app"));

            if (prevCost > 0) {
                double currCost = toDouble(currentSummary.get("total_cost"));
                period.put("cost_change_pct", Math.round((currCost - prevCost) / prevCost * 10000.0) / 100.0);
                period.put("installs_change_pct", prevInstalls > 0
                    ? Math.round((toDouble(currentSummary.get("total_installs")) - prevInstalls) / prevInstalls * 10000.0) / 100.0 : 0);
                double prevCpi = prevInApp > 0 ? prevCost / prevInApp : 0;
                double currCpi = toDouble(currentSummary.get("avg_cpi"));
                period.put("cpi_change_pct", prevCpi > 0
                    ? Math.round((currCpi - prevCpi) / prevCpi * 10000.0) / 100.0 : 0);
            }
        } catch (Exception ignored) {}
        return period;
    }

    private List<Map<String, Object>> detectAnomalies(String where, List<Object> params) {
        List<Map<String, Object>> anomalies = new ArrayList<>();
        try {
            // 按 campaign, report_date 分组，按日期倒序
            List<Map<String, Object>> allStats = jdbcTemplate.queryForList(
                "SELECT campaign, report_date, SUM(cost) AS day_cost, SUM(installs) AS day_installs, "
                + "SUM(in_app_actions) AS day_in_app FROM ad_reports WHERE " + where
                + " AND campaign IS NOT NULL AND campaign != '' "
                + "GROUP BY campaign, report_date ORDER BY campaign, report_date DESC", params.toArray());

            Map<String, List<Map<String, Object>>> campaignData = new LinkedHashMap<>();
            for (var s : allStats) {
                campaignData.computeIfAbsent(String.valueOf(s.get("campaign")), k -> new ArrayList<>()).add(s);
            }

            for (var entry : campaignData.entrySet()) {
                List<Map<String, Object>> stats = entry.getValue();
                if (stats.size() < 3) continue;
                List<Map<String, Object>> recent = stats.subList(0, Math.min(3, stats.size()));
                List<Map<String, Object>> older = stats.subList(Math.min(3, stats.size()),
                        Math.min(10, stats.size()));
                if (older.isEmpty()) continue;

                double avgRecentCost = recent.stream().mapToDouble(s -> toDouble(s.get("day_cost"))).average().orElse(0);
                double avgOlderCost = older.stream().mapToDouble(s -> toDouble(s.get("day_cost"))).average().orElse(0);
                double avgRecentInstalls = recent.stream().mapToDouble(s -> toDouble(s.get("day_installs"))).average().orElse(0);
                double avgOlderInstalls = older.stream().mapToDouble(s -> toDouble(s.get("day_installs"))).average().orElse(0);
                double recentCostSum = recent.stream().mapToDouble(s -> toDouble(s.get("day_cost"))).sum();
                double recentInAppSum = recent.stream().mapToDouble(s -> toDouble(s.get("day_in_app"))).sum();
                double olderCostSum = older.stream().mapToDouble(s -> toDouble(s.get("day_cost"))).sum();
                double olderInAppSum = older.stream().mapToDouble(s -> toDouble(s.get("day_in_app"))).sum();
                double avgRecentCpi = recentInAppSum > 0 ? recentCostSum / recentInAppSum : 0;
                double avgOlderCpi = olderInAppSum > 0 ? olderCostSum / olderInAppSum : 0;

                // 花费暴涨 > 50% 但安装下降
                if (avgOlderCost > 0 && avgRecentCost > avgOlderCost * 1.5 && avgRecentInstalls < avgOlderInstalls) {
                    anomalies.add(Map.of(
                        "campaign", entry.getKey(), "date", recent.get(0).get("report_date"),
                        "type", "cost_spike",
                        "detail", "花费暴涨" + Math.round((avgRecentCost / avgOlderCost - 1) * 100) + "%，安装下降"
                            + Math.round((1 - avgRecentInstalls / Math.max(avgOlderInstalls, 1)) * 100) + "%"));
                }
                // CPI 飙升 > 30%
                if (avgOlderCpi > 0 && avgRecentCpi > avgOlderCpi * 1.3) {
                    anomalies.add(Map.of(
                        "campaign", entry.getKey(), "date", recent.get(0).get("report_date"),
                        "type", "cpi_spike",
                        "detail", "CPI飙升至$" + Math.round(avgRecentCpi * 100.0) / 100.0 + "（均值$"
                            + Math.round(avgOlderCpi * 100.0) / 100.0 + "）"));
                }
            }
        } catch (Exception e) { log.warn("异常检测失败: {}", e.getMessage()); }
        return anomalies;
    }

    private double computeMetric(Map<String, Object> r, String metric) {
        double cost = toDouble(r.get("total_cost"));
        double imp = toDouble(r.get("total_impressions"));
        double clicks = toDouble(r.get("total_clicks"));
        double inst = toDouble(r.get("total_installs"));
        double inApp = toDouble(r.get("total_in_app"));
        return switch (metric) {
            case "cost" -> cost;
            case "installs" -> inst;
            case "impressions" -> imp;
            case "clicks" -> clicks;
            case "cpi" -> cost / Math.max(inApp, 1);
            case "ctr" -> clicks / Math.max(imp, 1);
            case "cvr" -> inst / Math.max(clicks, 1);
            default -> cost / Math.max(inApp, 1);
        };
    }

    private double toDouble(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0; }
    }
}
