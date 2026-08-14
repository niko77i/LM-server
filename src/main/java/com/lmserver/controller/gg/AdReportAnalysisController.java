package com.lmserver.controller.gg;

import com.lmserver.dto.response.*;
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
 * 广告报告分析控制器 — 对齐 Python main.py ad_reports_dashboard / trends / compare / dates / analyze。
 *
 * <h3>端点</h3>
 * <ul>
 * <li>GET /api/ad-reports/dashboard — 仪表盘：汇总指标 + 环比 + 异常检测(cost_spike/cpi_spike) + Campaign分组 + 素材关联数</li>
 * <li>GET /api/ad-reports/trends   — 趋势图：按 metric(cost/cpi/ctr/cvr等) × group_by(product/campaign) × 日期 生成时序数据</li>
 * <li>GET /api/ad-reports/compare  — 对比：按 product/campaign 聚合后排序对比</li>
 * <li>GET /api/ad-reports/stats    — 简版统计：totalCost/Impressions/Clicks/RecordCount</li>
 * <li>GET /api/ad-reports/dates    — 日期标记：返回有数据的日期→计数映射，供前端日历高亮</li>
 * <li>POST /api/ad-reports/analyze — AI分析：构建数据摘要上下文，供前端提交AI服务分析</li>
 * </ul>
 *
 * <h3>异常检测规则</h3>
 * <ul>
 * <li>cost_spike — 近3天日均花费 &gt; 前7天均值 × 1.5 且安装下降</li>
 * <li>cpi_spike  — 近3天CPI &gt; 前7天CPI × 1.3</li>
 * </ul>
 *
 * <h3>环比</h3>
 * 若传入 from_date / to_date，自动计算前一等长周期数据，返回 cost/installs/cpi 变化百分比。
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
    public ApiResponse<DashboardDto> dashboard(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name="product_name", defaultValue = "") String productName, @RequestParam(defaultValue = "") String region,
            @RequestParam(name="from_date", defaultValue = "") String fromDate, @RequestParam(name="to_date", defaultValue = "") String toDate) {

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

        DashboardSummaryDto summaryDto = new DashboardSummaryDto(
                Math.round(totalCost * 100.0) / 100.0, (long) totalImpressions, (long) totalClicks,
                (long) totalInstalls, Math.round(totalInApp * 100.0) / 100.0, avgCpi, avgCtr, avgCvr);

        // 环比
        Map<String, Double> periodCompare = calcPeriodCompare(userId, productName, region, fromDate, toDate, summary);

        // 异常检测
        List<AnomalyDto> anomalies = detectAnomalies(where, params);

        // Campaign 分组统计
        List<CampaignStatDto> campaigns = queryCampaignStats(where, params);

        // 素材关联数
        long assetCount = 0;
        if (!productName.isBlank()) {
            assetCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_assets pa JOIN products p ON pa.product_id=p.id WHERE p.product_name=?",
                Long.class, productName);
        }

        return ApiResponse.ok(new DashboardDto(summaryDto, periodCompare, anomalies, campaigns, assetCount));
    }

    // ═══════ Trends ═══════

    @GetMapping("/trends")
    public ApiResponse<TrendDto> trends(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name="product_name", defaultValue = "") String productName, @RequestParam(defaultValue = "") String region,
            @RequestParam(name="from_date", defaultValue = "") String fromDate, @RequestParam(name="to_date", defaultValue = "") String toDate,
            @RequestParam(defaultValue = "cpi") String metric,
            @RequestParam(name="group_by", defaultValue = "product_name") String groupBy) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, fromDate, toDate, params);
        String groupCol = "product_name".equals(groupBy) ? "product_name" : "campaign";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT " + groupCol + " AS name, report_date, SUM(cost) AS total_cost, SUM(installs) AS total_installs, "
            + "SUM(impressions) AS total_impressions, SUM(clicks) AS total_clicks, SUM(in_app_actions) AS total_in_app "
            + "FROM ad_reports WHERE " + where + " GROUP BY " + groupCol + ", report_date ORDER BY report_date",
            params.toArray());

        Map<String, List<TrendPointDto>> seriesMap = new LinkedHashMap<>();
        for (var r : rows) {
            String name = String.valueOf(r.get("name"));
            seriesMap.computeIfAbsent(name, k -> new ArrayList<>())
                .add(new TrendPointDto(r.get("report_date"), computeMetric(r, metric)));
        }

        List<TrendSeriesDto> series = new ArrayList<>();
        for (var e : seriesMap.entrySet()) {
            series.add(new TrendSeriesDto(e.getKey(), e.getValue()));
        }
        return ApiResponse.ok(new TrendDto(series));
    }

    // ═══════ Compare ═══════

    @GetMapping("/compare")
    public ApiResponse<List<CompareItemDto>> compare(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name="product_name", defaultValue = "") String productName, @RequestParam(defaultValue = "") String region,
            @RequestParam(name="group_by", defaultValue = "product_name") String groupBy,
            @RequestParam(name="from_date", defaultValue = "") String fromDate, @RequestParam(name="to_date", defaultValue = "") String toDate,
            @RequestParam(name="sort_by", defaultValue = "cpi") String sortBy) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, fromDate, toDate, params);
        String groupCol = "product_name".equals(groupBy) ? "product_name" : "campaign";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT " + groupCol + " AS name, SUM(cost) AS total_cost, SUM(impressions) AS total_impressions, "
            + "SUM(clicks) AS total_clicks, SUM(installs) AS total_installs, SUM(in_app_actions) AS total_in_app "
            + "FROM ad_reports WHERE " + where + " GROUP BY " + groupCol, params.toArray());

        List<CompareItemDto> items = new ArrayList<>();
        for (var r : rows) {
            double cost = toDouble(r.get("total_cost"));
            double imp = toDouble(r.get("total_impressions"));
            double clicks = toDouble(r.get("total_clicks"));
            double inst = toDouble(r.get("total_installs"));
            double inApp = toDouble(r.get("total_in_app"));
            items.add(new CompareItemDto(
                String.valueOf(r.get("name")),
                Math.round(cost * 100.0) / 100.0, (long) imp, (long) clicks, (long) inst,
                Math.round(inApp * 100.0) / 100.0,
                inApp > 0 ? Math.round(cost / inApp * 100.0) / 100.0 : 0,
                imp > 0 ? Math.round(clicks / imp * 10000.0) / 10000.0 : 0,
                clicks > 0 ? Math.round(inst / clicks * 10000.0) / 10000.0 : 0));
        }
        items.sort((a, b) -> Double.compare(getField(b, sortBy), getField(a, sortBy)));
        return ApiResponse.ok(items);
    }

    // ═══════ Stats ═══════

    @GetMapping("/stats")
    public ApiResponse<AdStatsDto> stats(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name="product_name", defaultValue = "") String productName, @RequestParam(defaultValue = "") String region,
            @RequestParam(name="from_date", defaultValue = "") String fromDate, @RequestParam(name="to_date", defaultValue = "") String toDate) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, fromDate, toDate, params);

        Map<String, Object> s = querySummary(where, params);
        long recordCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ad_reports WHERE " + where, Long.class, params.toArray());
        return ApiResponse.ok(new AdStatsDto(
                toDouble(s.get("total_cost")), ((Number) s.get("total_impressions")).longValue(),
                ((Number) s.get("total_clicks")).longValue(), ((Number) s.get("total_installs")).longValue(),
                toDouble(s.get("total_in_app")), recordCount));
    }

    // ═══════ Dates ═══════

    @GetMapping("/dates")
    public ApiResponse<AdDatesDto> dates(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name="product_name", defaultValue = "") String productName, @RequestParam(defaultValue = "") String region) {

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
        return ApiResponse.ok(new AdDatesDto(datesMap));
    }

    // ═══════ Analyze ═══════

    @PostMapping("/analyze")
    public ApiResponse<AnalyzeResultDto> analyze(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        Long userId = principal.getUserId();
        String productName = String.valueOf(body.getOrDefault("product_name", ""));
        String region = String.valueOf(body.getOrDefault("region", ""));
        String question = String.valueOf(body.getOrDefault("question", ""));
        if (question.isBlank()) return ApiResponse.fail("请输入分析问题");

        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, "", "", params);

        Map<String, Object> summary = querySummary(where, params);
        List<CampaignStatDto> campaigns = queryCampaignStats(where, params);
        Map<String, Object> dataCtx = Map.of("summary", summary, "top_campaigns",
                campaigns.subList(0, Math.min(5, campaigns.size())));

        return ApiResponse.ok(new AnalyzeResultDto(question, dataCtx,
                "请前端将数据上下文提交至配置的AI服务进行分析"));
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

    private List<CampaignStatDto> queryCampaignStats(String where, List<Object> params) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT campaign, SUM(cost) AS total_cost, SUM(installs) AS total_installs, "
            + "SUM(impressions) AS total_impressions, SUM(clicks) AS total_clicks, SUM(in_app_actions) AS total_in_app "
            + "FROM ad_reports WHERE " + where + " AND campaign IS NOT NULL AND campaign != '' "
            + "GROUP BY campaign ORDER BY total_cost DESC", params.toArray());
        List<CampaignStatDto> result = new ArrayList<>();
        for (var r : rows) {
            double cost = toDouble(r.get("total_cost"));
            double imp = toDouble(r.get("total_impressions"));
            double clicks = toDouble(r.get("total_clicks"));
            double inst = toDouble(r.get("total_installs"));
            double inApp = toDouble(r.get("total_in_app"));
            result.add(new CampaignStatDto(
                String.valueOf(r.get("campaign")),
                Math.round(cost * 100.0) / 100.0, Math.round(inst * 100.0) / 100.0,
                (long) imp, (long) clicks, Math.round(inApp * 100.0) / 100.0,
                inApp > 0 ? Math.round(cost / Math.max(inApp, 1) * 100.0) / 100.0 : 0,
                imp > 0 ? Math.round(clicks / Math.max(imp, 1) * 10000.0) / 10000.0 : 0,
                clicks > 0 ? Math.round(inst / Math.max(clicks, 1) * 10000.0) / 10000.0 : 0));
        }
        return result;
    }

    private Map<String, Double> calcPeriodCompare(Long userId, String productName, String region,
            String fromDate, String toDate, Map<String, Object> currentSummary) {
        Map<String, Double> period = new LinkedHashMap<>();
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
                    ? Math.round((toDouble(currentSummary.get("total_installs")) - prevInstalls) / prevInstalls * 10000.0) / 100.0 : 0d);
                double prevCpi = prevInApp > 0 ? prevCost / prevInApp : 0;
                double currCpi = toDouble(currentSummary.get("avg_cpi"));
                period.put("cpi_change_pct", prevCpi > 0
                    ? Math.round((currCpi - prevCpi) / prevCpi * 10000.0) / 100.0 : 0d);
            }
        } catch (Exception ignored) {}
        return period;
    }

    private List<AnomalyDto> detectAnomalies(String where, List<Object> params) {
        List<AnomalyDto> anomalies = new ArrayList<>();
        try {
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

                if (avgOlderCost > 0 && avgRecentCost > avgOlderCost * 1.5 && avgRecentInstalls < avgOlderInstalls) {
                    anomalies.add(new AnomalyDto(entry.getKey(), recent.get(0).get("report_date"), "cost_spike",
                        "花费暴涨" + Math.round((avgRecentCost / avgOlderCost - 1) * 100) + "%，安装下降"
                            + Math.round((1 - avgRecentInstalls / Math.max(avgOlderInstalls, 1)) * 100) + "%"));
                }
                if (avgOlderCpi > 0 && avgRecentCpi > avgOlderCpi * 1.3) {
                    anomalies.add(new AnomalyDto(entry.getKey(), recent.get(0).get("report_date"), "cpi_spike",
                        "CPI飙升至$" + Math.round(avgRecentCpi * 100.0) / 100.0 + "（均值$"
                            + Math.round(avgOlderCpi * 100.0) / 100.0 + "）"));
                }
            }
        } catch (Exception e) { log.warn("异常检测失败: {}", e.getMessage()); }
        return anomalies;
    }

    /** Compare 排序辅助 — 按字段名从 CompareItemDto 取值 */
    private double getField(CompareItemDto item, String field) {
        return switch (field) {
            case "total_cost" -> item.getTotalCost();
            case "total_installs" -> item.getTotalInstalls();
            case "total_impressions" -> item.getTotalImpressions();
            case "total_clicks" -> item.getTotalClicks();
            case "total_in_app" -> item.getTotalInApp();
            case "ctr" -> item.getCtr();
            case "cvr" -> item.getCvr();
            default -> item.getCpi();
        };
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

    // ═══════ Cross-User ═══════

    /** 跨用户对比 — 同一产品不同用户的 CPI 聚合对比，按 avg_cpi 升序 */
    @GetMapping("/cross-user")
    public ApiResponse<CrossUserResultDto> crossUser(
            @RequestParam String productName,
            @RequestParam(name="from_date", defaultValue = "") String fromDate,
            @RequestParam(name="to_date", defaultValue = "") String toDate) {

        StringBuilder where = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        String[] names = productName.split(",");
        if (names.length == 1) { where.append(" AND product_name=?"); params.add(names[0].trim()); }
        else { where.append(" AND product_name IN (").append(String.join(",", Collections.nCopies(names.length, "?"))).append(")"); for (String n : names) params.add(n.trim()); }
        if (!fromDate.isBlank()) { where.append(" AND report_date >= ?"); params.add(fromDate); }
        if (!toDate.isBlank()) { where.append(" AND report_date <= ?"); params.add(toDate); }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT ar.user_id, u.display_name, u.username, SUM(ar.cost) AS total_cost, SUM(ar.installs) AS total_installs, "
            + "SUM(ar.in_app_actions) AS total_in_app, COUNT(DISTINCT ar.report_date) AS report_days "
            + "FROM ad_reports ar LEFT JOIN users u ON ar.user_id=u.id WHERE " + where
            + " GROUP BY ar.user_id, u.display_name, u.username", params.toArray());

        List<CrossUserDto> users = new ArrayList<>();
        for (var r : rows) {
            double cost = toDouble(r.get("total_cost"));
            double inApp = toDouble(r.get("total_in_app"));
            users.add(new CrossUserDto(
                    ((Number) r.get("user_id")).longValue(),
                    String.valueOf(r.get("display_name")),
                    String.valueOf(r.get("username")),
                    Math.round(cost * 100.0) / 100.0,
                    ((Number) r.get("total_installs")).longValue(),
                    inApp,
                    inApp > 0 ? Math.round(cost / inApp * 100.0) / 100.0 : 0,
                    ((Number) r.get("report_days")).longValue()));
        }
        users.sort((a, b) -> Double.compare(a.getAvgCpi(), b.getAvgCpi()));
        return ApiResponse.ok(new CrossUserResultDto(users));
    }

    // ═══════ Multi-Analysis ═══════

    /** 多维自由分析 — X/Y轴指标 + 分组维度 + 散点数据，对齐 Python multi_analysis */
    @GetMapping("/multi-analysis")
    public ApiResponse<MultiAnalysisDto> multiAnalysis(
            @RequestParam(name="x_axis", defaultValue = "cost") String xAxis, @RequestParam(name="y_axis", defaultValue = "cpi") String yAxis,
            @RequestParam(name="size_by", defaultValue = "") String sizeBy, @RequestParam(name="group_by", defaultValue = "campaign") String groupBy,
            @RequestParam(name="product_name", defaultValue = "") String productName, @RequestParam(defaultValue = "") String campaign,
            @RequestParam(defaultValue = "") String region, @RequestParam(name="from_date", defaultValue = "") String fromDate,
            @RequestParam(name="to_date", defaultValue = "") String toDate, @AuthenticationPrincipal UserPrincipal principal) {

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, region, fromDate, toDate, params);
        if (!campaign.isBlank()) { where += " AND campaign=?"; params.add(campaign); }

        String groupCol = switch (groupBy) { case "product_name" -> "product_name"; case "account" -> "account"; default -> "campaign"; };

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT " + groupCol + " AS name, SUM(cost) AS total_cost, SUM(installs) AS total_installs, "
            + "SUM(impressions) AS total_impressions, SUM(clicks) AS total_clicks, SUM(in_app_actions) AS total_in_app "
            + "FROM ad_reports WHERE " + where + " GROUP BY " + groupCol, params.toArray());

        List<ScatterPointDto> points = new ArrayList<>();
        Map<String, Double> xVals = new LinkedHashMap<>(), yVals = new LinkedHashMap<>();
        for (var r : rows) {
            double x = computeRaw(r, xAxis), y = computeRaw(r, yAxis);
            String name = String.valueOf(r.get("name"));
            double sz = sizeBy.isBlank() ? 0 : computeRaw(r, sizeBy);
            points.add(new ScatterPointDto(name,
                    Math.round(x * 100.0) / 100.0, Math.round(y * 100.0) / 100.0,
                    sz, Math.round(toDouble(r.get("total_cost")) * 100.0) / 100.0));
            xVals.put(name, x); yVals.put(name, y);
        }

        double xAvg = xVals.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double yAvg = yVals.values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double pearson = calcPearson(xVals, yVals, xAvg, yAvg);

        String insight = pearson > 0.7 ? xAxis + "与" + yAxis + "呈强正相关(r=" + String.format("%.2f", pearson) + ")"
                : pearson < -0.7 ? xAxis + "与" + yAxis + "呈强负相关" : "无明显线性相关";

        return ApiResponse.ok(new MultiAnalysisDto(points, xAxis, yAxis, groupBy,
                Math.round(xAvg * 100.0) / 100.0, Math.round(yAvg * 100.0) / 100.0,
                Math.round(pearson * 10000.0) / 10000.0, insight));
    }

    /** 多轮 AI 对话 — 带分析上下文 */
    @PostMapping("/multi-ai-chat")
    public ApiResponse<MultiAiChatResultDto> multiAiChat(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String productName = String.valueOf(body.getOrDefault("product_name", ""));
        String question = String.valueOf(body.getOrDefault("question", ""));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history = (List<Map<String, Object>>) body.getOrDefault("history", List.of());
        if (question.isBlank()) return ApiResponse.fail("请输入问题");

        Long userId = principal.getUserId();
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, productName, "", "", "", params);

        Map<String, Object> summary = querySummary(where, params);
        List<CampaignStatDto> campaigns = queryCampaignStats(where, params);

        Map<String, Object> ctx = Map.of("summary", summary, "top_campaigns",
                campaigns.subList(0, Math.min(10, campaigns.size())), "history", history);

        return ApiResponse.ok(new MultiAiChatResultDto(question, ctx,
                "请前端将上下文+历史提交至AI服务获取回复"));
    }

    // ── 统计辅助 ──

    private double computeRaw(Map<String, Object> r, String metric) {
        double cost = toDouble(r.get("total_cost")), imp = toDouble(r.get("total_impressions"));
        double clicks = toDouble(r.get("total_clicks")), inst = toDouble(r.get("total_installs"));
        double inApp = toDouble(r.get("total_in_app"));
        return switch (metric) { case "cost" -> cost; case "installs" -> inst; case "impressions" -> imp; case "clicks" -> clicks; case "cpi" -> cost / Math.max(inApp, 1); case "ctr" -> clicks / Math.max(imp, 1); case "cvr" -> inst / Math.max(clicks, 1); default -> cost / Math.max(inApp, 1); };
    }

    private double calcPearson(Map<String, Double> xv, Map<String, Double> yv, double xAvg, double yAvg) {
        double cov = 0, sx = 0, sy = 0; int n = 0;
        for (var e : xv.entrySet()) {
            String k = e.getKey(); if (!yv.containsKey(k)) continue;
            double dx = e.getValue() - xAvg, dy = yv.get(k) - yAvg;
            cov += dx * dy; sx += dx * dx; sy += dy * dy; n++;
        }
        return n > 2 && sx > 0 && sy > 0 ? cov / Math.sqrt(sx * sy) : 0;
    }

    private double toDouble(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0; }
    }
}
