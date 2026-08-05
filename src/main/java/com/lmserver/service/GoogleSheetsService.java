package com.lmserver.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.lmserver.dto.sheets.FbReportRow;
import com.lmserver.dto.sheets.ZuobiaoRow;
import com.lmserver.entity.gg.Packages;
import com.lmserver.entity.gg.Products;
import com.lmserver.mapper.gg.PackagesMapper;
import com.lmserver.mapper.gg.ProductsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Google Sheets 服务 — GG 做表 14 列 upsert + FB 做表 12 列 upsert。
 * 完整对齐设计文档 8.1 节：读取现有数据→找最后行→去重索引→自动扩容→批量更新。
 */
@Slf4j
@Service
public class GoogleSheetsService {

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    @Autowired
    private ProductsMapper productsMapper;
    @Autowired
    private PackagesMapper packagesMapper;

    private Sheets sheets;
    private boolean initialized;

    private synchronized void init() {
        if (initialized) return;
        if (!new java.io.File(credentialsPath).exists()) {
            log.warn("Google Sheets 凭证不存在: {}", credentialsPath);
            initialized = true; return;
        }
        try {
            GoogleCredentials creds = GoogleCredentials
                    .fromStream(new FileInputStream(credentialsPath))
                    .createScoped("https://www.googleapis.com/auth/spreadsheets");
            sheets = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(creds))
                    .setApplicationName("LM-Server").build();
            log.info("Google Sheets 初始化完成");
        } catch (Exception e) {
            log.error("Google Sheets 初始化失败: {}", e.getMessage());
        }
        initialized = true;
    }

    // ═══════════ GG 做表 upsert（14 列 A-L 数据 + M 利润 + N 客户实际消耗）═══════════

    /**
     * GG 做表数据 upsert — 对齐 Python upsert_zuobiao。
     * A 日期|B 运营|C 客户名称|D 商务|E 投放国家|F 渠道号|G 系列名|H 包名|I 账户ID|J 素材图|K 落地页|L 账号消耗
     */
    public Map<String, Object> upsertZuobiao(String spreadsheetId, List<ZuobiaoRow> rows,
            String productName, String reportDate) {
        init();
        if (sheets == null) return Map.of("error", "Sheets 未初始化");

        // ── 包名校验：产品包系列名 vs 数据广告系列名取交集 ──
        boolean seriesMismatchWarning = false;
        if (productName != null && !productName.isBlank()) {
            var productQw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Products>()
                    .eq(Products::getProductName, productName);
            Products product = productsMapper.selectOne(productQw);
            if (product != null) {
                // 产品下所有正常状态的包系列名
                var pkgQw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Packages>()
                        .eq(Packages::getProductId, product.getId())
                        .eq(Packages::getStatus, "正常");
                Set<String> pkgSeriesNames = packagesMapper.selectList(pkgQw).stream()
                        .map(Packages::getSeriesName)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.toSet());

                // 数据中的广告系列名（G 列）
                Set<String> campaigns = rows.stream()
                        .map(ZuobiaoRow::getSeriesName)
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.toSet());

                // 养户行：没有系列名也没有包名的行
                boolean hasMaintenanceRow = rows.stream()
                        .anyMatch(r -> (r.getSeriesName() == null || r.getSeriesName().isBlank())
                                && (r.getPackageName() == null || r.getPackageName().isBlank()));

                // 取交集
                campaigns.retainAll(pkgSeriesNames);

                if (campaigns.isEmpty()) {
                    if (!hasMaintenanceRow) {
                        return Map.of("error", "产品选择有误！「" + productName
                                + "」的包系列与数据中的广告系列不匹配，请重新选择产品。");
                    }
                    // 无交集但有养户行 → 只写入养户行，跳过其他行
                    seriesMismatchWarning = true;
                    rows = rows.stream()
                            .filter(r -> (r.getSeriesName() == null || r.getSeriesName().isBlank())
                                    && (r.getPackageName() == null || r.getPackageName().isBlank()))
                            .collect(Collectors.toList());
                    log.warn("[GG-Sheets] 包系列与广告系列无交集，仅写入 {} 行养户数据", rows.size());
                }
            }
        }

        int updated = 0, appended = 0;
        try {
            Spreadsheet sp = sheets.spreadsheets().get(spreadsheetId).execute();
            String sheetName = sp.getSheets().get(0).getProperties().getTitle();

            // 1. 读取现有数据 A-N
            ValueRange vr = sheets.spreadsheets().values()
                    .get(spreadsheetId, "'" + sheetName + "'!A:N").execute();
            List<List<Object>> existing = vr.getValues();
            if (existing == null) existing = new ArrayList<>();

            // 2. 找最后一行（A 列有日期）
            int lastRow = 0;
            String lastDate = "";
            for (int i = existing.size() - 1; i >= 0; i--) {
                List<Object> r = existing.get(i);
                if (!r.isEmpty() && r.get(0) != null && !r.get(0).toString().isBlank()) {
                    lastRow = i + 1; lastDate = r.get(0).toString().trim(); break;
                }
            }
            // 3. 新日期空一行
            if (reportDate != null && !reportDate.isEmpty() && !lastDate.isEmpty() && !lastDate.equals(reportDate)) {
                lastRow++;
            }
            // 4. 去重索引: (日期 + 账户ID)
            Map<String, Integer> dedup = new HashMap<>();
            for (int i = 0; i < existing.size(); i++) {
                List<Object> r = existing.get(i);
                if (r.size() > 8 && r.get(0) != null && r.get(8) != null) {
                    dedup.put(r.get(0).toString().trim() + "|" + r.get(8).toString().trim(), i);
                }
            }
            // 5. 自动扩容
            int maxRows = sp.getSheets().get(0).getProperties().getGridProperties().getRowCount();
            int needed = lastRow + rows.size() + 1;
            if (needed > maxRows) {
                sheets.spreadsheets().batchUpdate(spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                                new Request().setAppendDimension(new AppendDimensionRequest()
                                        .setSheetId(sp.getSheets().get(0).getProperties().getSheetId())
                                        .setDimension("ROWS").setLength(needed - maxRows + 100)))))
                        .execute();
            }
            // 6. 批量更新
            List<ValueRange> updates = new ArrayList<>();
            for (ZuobiaoRow zr : rows) {
                List<Object> row = zr.toSheetRow();
                String key = reportDate + "|" + zr.getAccountId();
                int target;
                if (dedup.containsKey(key)) { target = dedup.get(key); updated++; }
                else { target = lastRow; lastRow++; appended++; }
                updates.add(new ValueRange()
                        .setRange("'" + sheetName + "'!A" + (target + 1) + ":L" + (target + 1))
                        .setValues(List.of(row)));
            }
            if (!updates.isEmpty()) {
                sheets.spreadsheets().values().batchUpdate(spreadsheetId,
                        new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(updates)).execute();
            }
            log.info("[GG-Sheets] upsert: {}更新 {}追加, 表格:{}", updated, appended, spreadsheetId);
        } catch (Exception e) {
            log.error("[GG-Sheets] 失败: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
        if (seriesMismatchWarning) {
            return Map.of("updated", updated, "appended", appended,
                    "warning", "产品「" + productName + "」的包系列与数据中的广告系列无交集，请确认产品选择是否正确。");
        }
        return Map.of("updated", updated, "appended", appended);
    }

    // ═══════════ FB 做表 upsert（12 列 A-L）═══════════

    /**
     * FB 做表数据写入 — 对齐 Python upsert_fb_reports。
     * A 日期|B 运营|C 账户名称|D 广告账户ID|E 账号消耗|F 报给客户|G 客户名称|H 商务|I 投放国家|J 渠道号|K 平台实际|L 代投比例
     */
    public Map<String, Object> upsertFbReports(String spreadsheetId, List<FbReportRow> rows,
            Long userId, String productName, String reportDate) {
        init();
        if (sheets == null) return Map.of("error", "Sheets 未初始化");
        int updated = 0, appended = 0;
        try {
            Spreadsheet sp = sheets.spreadsheets().get(spreadsheetId).execute();
            String sheetName = sp.getSheets().get(0).getProperties().getTitle();

            ValueRange vr = sheets.spreadsheets().values()
                    .get(spreadsheetId, "'" + sheetName + "'!A:L").execute();
            List<List<Object>> existing = vr.getValues();
            if (existing == null) existing = new ArrayList<>();

            int lastRow = 0;
            String lastDate = "";
            for (int i = existing.size() - 1; i >= 0; i--) {
                List<Object> r = existing.get(i);
                if (!r.isEmpty() && r.get(0) != null && !r.get(0).toString().isBlank()) {
                    lastRow = i + 1; lastDate = r.get(0).toString().trim(); break;
                }
            }
            if (reportDate != null && !reportDate.isEmpty() && !lastDate.isEmpty() && !lastDate.equals(reportDate)) {
                lastRow++;
            }

            Map<String, Integer> dedup = new HashMap<>();
            for (int i = 0; i < existing.size(); i++) {
                List<Object> r = existing.get(i);
                if (r.size() > 3 && r.get(0) != null && r.get(3) != null) {
                    dedup.put(r.get(0).toString().trim() + "|" + r.get(3).toString().trim(), i);
                }
            }

            int maxRows = sp.getSheets().get(0).getProperties().getGridProperties().getRowCount();
            int needed = lastRow + rows.size() + 1;
            if (needed > maxRows) {
                sheets.spreadsheets().batchUpdate(spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                                new Request().setAppendDimension(new AppendDimensionRequest()
                                        .setSheetId(sp.getSheets().get(0).getProperties().getSheetId())
                                        .setDimension("ROWS").setLength(needed - maxRows + 100)))))
                        .execute();
            }

            List<ValueRange> updates = new ArrayList<>();
            for (FbReportRow fr : rows) {
                List<Object> row = fr.toSheetRow();
                String key = reportDate + "|" + fr.getAccountId();
                int target;
                if (dedup.containsKey(key)) { target = dedup.get(key); updated++; }
                else { target = lastRow; lastRow++; appended++; }
                updates.add(new ValueRange()
                        .setRange("'" + sheetName + "'!A" + (target + 1) + ":L" + (target + 1))
                        .setValues(List.of(row)));
            }
            if (!updates.isEmpty()) {
                sheets.spreadsheets().values().batchUpdate(spreadsheetId,
                        new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(updates)).execute();
            }
            log.info("[FB-Sheets] upsert: {}更新 {}追加", updated, appended);
        } catch (Exception e) {
            log.error("[FB-Sheets] 失败: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
        return Map.of("updated", updated, "appended", appended);
    }

    // ═══════════ 基础读写 ═══════════
    public List<List<Object>> read(String spreadsheetId, String range) throws Exception {
        init();
        if (sheets == null) throw new RuntimeException("Sheets not initialized");
        return sheets.spreadsheets().values().get(spreadsheetId, range).execute().getValues();
    }

    public void write(String spreadsheetId, String range, List<List<Object>> values) throws Exception {
        init();
        if (sheets == null) throw new RuntimeException("Sheets not initialized");
        sheets.spreadsheets().values().update(spreadsheetId, range,
                new ValueRange().setValues(values)).setValueInputOption("USER_ENTERED").execute();
    }
}
