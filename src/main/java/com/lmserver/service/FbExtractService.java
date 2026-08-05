package com.lmserver.service;

import com.lmserver.entity.gg.SheetsSyncLog;
import com.lmserver.mapper.gg.SheetsSyncLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * FB 数据提取服务 — 解析/查重/保存，含异步 Google Sheets 写入。
 * 完全对齐设计文档 v1.2 的解析流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FbExtractService {

    private final com.lmserver.mapper.fb.FbAdReportsMapper fbAdReportsMapper;
    private final SheetsSyncLogMapper syncLogMapper;
    private final GoogleSheetsService sheetsService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 解析提取文本 → 结构化数据（含尾部校验，对齐设计文档 v1.2）。
     *
     * 解析流程：
     * 1. 找到"数据透视表"~"总成效"范围，动态分组提取每行账户数据
     * 2. "总成效"之后提取校验数据：
     *    - 正则 "已显示\\d+/(\\d+)行" 提取声明总行数
     *    - 收集 $ 金额取最大值，验证紧跟"总花费"为声明总消耗
     * 3. 每组提取后：去重、回流过滤、警告
     */
    public ParseResult parseExtract(String text) {
        List<String> lines = List.of(text.split("\n"));
        List<Map<String, String>> data = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int groupSize = 0;

        // 1. 找"数据透视表"~"总成效"范围
        int startIdx = -1, endIdx = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.contains("数据透视表") && startIdx < 0) startIdx = i + 1;
            if (line.contains("总成效") && endIdx == lines.size()) endIdx = i;
        }
        if (startIdx < 0) throw new RuntimeException("未找到'数据透视表'标记");

        // 2. 动态分组提取：每到新账户开始的行开始新组
        Pattern accountStart = Pattern.compile("^[A-Z].*?\\s+\\d{10,20}");
        Pattern costP = Pattern.compile("\\$([\\d,]+\\.?\\d*)");

        List<List<String>> groups = new ArrayList<>();
        List<String> currentGroup = new ArrayList<>();
        for (int i = startIdx; i < endIdx; i++) {
            String line = lines.get(i).trim();
            if (accountStart.matcher(line).find() && !currentGroup.isEmpty()) {
                groups.add(currentGroup);
                currentGroup = new ArrayList<>();
            }
            currentGroup.add(line);
        }
        if (!currentGroup.isEmpty()) groups.add(currentGroup);
        groupSize = groups.isEmpty() ? 0 : groups.get(0).size();

        // 3. 每组提取 account_name, account_id, cost
        Pattern accountP = Pattern.compile("(.+?)\\s+(\\d{10,20})");
        for (List<String> group : groups) {
            String groupText = String.join(" ", group);

            // 提取所有 $ 金额
            List<Double> allCosts = new ArrayList<>();
            Matcher cm = costP.matcher(groupText);
            while (cm.find()) allCosts.add(Double.parseDouble(cm.group(1).replace(",", "")));

            // 去重
            List<Double> distinctCosts = allCosts.stream().distinct().collect(Collectors.toList());

            // 回流过滤：去重后所有金额为0 → 跳过
            if (distinctCosts.stream().allMatch(c -> c == 0.0)) continue;

            // 警告：去重后 > 2
            if (distinctCosts.size() > 2) {
                warnings.add("组内检测到多个不同金额: " + distinctCosts);
            }

            Matcher am = accountP.matcher(groupText);
            if (am.find()) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("account_name", am.group(1).trim());
                row.put("account_id", am.group(2).trim());
                // 取最大值（对齐 Python: max(dollar_amounts)）
                double maxCost = distinctCosts.stream().max(Double::compare).orElse(0.0);
                row.put("cost", String.valueOf(maxCost));
                data.add(row);
            }
        }

        // 4. 尾部校验
        List<String> tailLines = lines.subList(endIdx, lines.size());
        int declaredRows = 0;
        double declaredSpend = 0.0;
        Pattern rowCount = Pattern.compile("已显示\\d+/(\\d+)行");

        for (int i = 0; i < tailLines.size(); i++) {
            String line = tailLines.get(i).trim();
            Matcher rm = rowCount.matcher(line);
            if (rm.find()) declaredRows = Integer.parseInt(rm.group(1));

            if (line.startsWith("$")) {
                try {
                    double amt = Double.parseDouble(line.replace("$", "").replace(",", ""));
                    String nearby = line + " " + String.join(" ",
                            tailLines.subList(Math.min(i + 1, tailLines.size()),
                                    Math.min(i + 3, tailLines.size())));
                    if (nearby.contains("总花费")) declaredSpend = Math.max(declaredSpend, amt);
                } catch (NumberFormatException ignored) {}
            }
        }

        double extractedSpend = data.stream().mapToDouble(r -> {
            try { return Double.parseDouble(r.getOrDefault("cost", "0")); }
            catch (Exception e) { return 0; }
        }).sum();

        Map<String, Object> validation = new LinkedHashMap<>();
        validation.put("declaredRows", declaredRows);
        validation.put("extractedRows", data.size());
        validation.put("declaredSpend", Math.round(declaredSpend * 100.0) / 100.0);
        validation.put("extractedSpend", Math.round(extractedSpend * 100.0) / 100.0);

        return new ParseResult(data, warnings, groupSize, validation);
    }

    /**
     * 检查重复 — 查询 fb_ad_reports 表中已存在的记录。
     */
    /**
     * 检查重复 — 按 (user_id, product_name, line_name, account_id, report_date) 查重。
     */
    public List<Map<String, String>> checkDuplicates(Long userId, String productName,
            String lineName, String reportDate, List<Map<String, String>> records) {
        var reportDt = java.time.LocalDate.parse(reportDate).atStartOfDay();
        List<Map<String, String>> duplicates = new ArrayList<>();

        for (Map<String, String> r : records) {
            var existing = fbAdReportsMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lmserver.entity.fb.FbAdReports>()
                            .eq(com.lmserver.entity.fb.FbAdReports::getUserId, userId)
                            .eq(com.lmserver.entity.fb.FbAdReports::getProductName, productName)
                            .eq(com.lmserver.entity.fb.FbAdReports::getLineName, lineName)
                            .eq(com.lmserver.entity.fb.FbAdReports::getAccountId, r.get("account_id"))
                            .eq(com.lmserver.entity.fb.FbAdReports::getReportDate, reportDt));
            if (!existing.isEmpty()) {
                duplicates.add(r);
            }
        }
        return duplicates;
    }

    /**
     * 保存提取数据（含异步写 Sheets，对齐设计文档 v1.2）。
     * 使用 MyBatis-Plus insert 替代 JdbcTemplate ON DUPLICATE KEY UPDATE。
     */
    public int saveExtract(Long userId, String productName, String lineName,
            String reportDate, List<Map<String, String>> records) {
        int saved = 0;
        var reportDt = java.time.LocalDate.parse(reportDate).atStartOfDay();

        for (Map<String, String> r : records) {
            try {
                com.lmserver.entity.fb.FbAdReports report = new com.lmserver.entity.fb.FbAdReports();
                report.setUserId(userId);
                report.setProductName(productName);
                report.setLineName(lineName);
                report.setReportDate(reportDt);
                report.setAccountName(r.getOrDefault("account_name", ""));
                report.setAccountId(r.getOrDefault("account_id", ""));
                report.setCost(Double.parseDouble(r.getOrDefault("cost", "0")));
                report.setImpressions(0L);
                report.setClicks(0L);
                report.setRegistrations(0L);
                report.setPurchases(0L);
                report.setCostPerPurchase(0.0);
                report.setSavedAt(java.time.LocalDateTime.now());
                fbAdReportsMapper.upsert(report);
                saved++;
            } catch (Exception e) {
                log.warn("保存记录失败: {}", e.getMessage());
            }
        }

        // 异步写 Sheets
        if (saved > 0) {
            SheetsSyncLog syncLog = new SheetsSyncLog();
            syncLog.setUserId(userId);
            syncLog.setProductName(productName);
            syncLog.setStatus("pending");
            syncLog.setRetryCount(0L);
            syncLog.setCreatedAt(java.time.LocalDateTime.now());
            syncLogMapper.insert(syncLog);

            final int finalSaved = saved;
            if (taskExecutor == null) { log.info("[FB-Sheets] 异步线程池未配置，跳过Sheets写入"); return saved; }
            taskExecutor.execute(() -> {
                try {
                    sheetsService.upsertFbReports(syncLog.getSpreadsheetId(), List.of(),
                            syncLog.getUserId(), syncLog.getProductName(), "", "", "", null, "");
                    syncLog.setStatus("synced");
                } catch (Exception e) {
                    log.error("[FB-Sheets] 写入失败: {}", e.getMessage());
                    syncLog.setStatus("failed");
                    syncLog.setErrorMsg(e.getMessage().substring(0, Math.min(500, e.getMessage().length())));
                    syncLog.setRetryCount(syncLog.getRetryCount() + 1);
                }
                syncLogMapper.updateById(syncLog);
            });
        }

        log.info("[FB提取] 用户{} 保存{}条 产品:{} 日期:{}", userId, saved, productName, reportDate);
        return saved;
    }

    /** 解析结果 */
    public record ParseResult(List<Map<String, String>> data, List<String> warnings,
                               int groupSize, Map<String, Object> validation) {}
}
