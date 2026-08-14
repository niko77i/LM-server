package com.lmserver.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.lmserver.dto.sheets.FbReportRow;
import com.lmserver.dto.sheets.ZuobiaoRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Google Sheets 服务 — 对齐 Python google_sheets_service.py。
 * GG 做表 14 列 upsert（A-N，含公式列 + 格式化）
 * + FB 做表 12 列 upsert（A-L）。
 */
@Slf4j
@Service
public class GoogleSheetsService {

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    private Sheets sheets;
    private boolean initialized;

    private synchronized void init() {
        if (initialized) return;
        try {
            // 优先从 classpath 加载（不依赖工作目录），回退文件系统
            String resourcePath = credentialsPath
                    .replace("src/main/resources/", "")
                    .replace("classpath:", "");
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                is = new FileInputStream(credentialsPath);
            }
            GoogleCredentials creds = GoogleCredentials
                    .fromStream(is)
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

    // ═══════════ GG 做表 upsert（对齐 Python upsert_zuobiao）═══════════

    /**
     * GG 做表数据 upsert — 对齐 Python upsert_zuobiao。
     *
     * <pre>
     * A=日期 | B=运营 | C=账户名称 | D=客户ID | E=账号消耗 | F=留空
     * G=产品名/养户 | H=商务/止戈 | I=投放国家 | J=广告系列 | K=留空
     * L=代投比例 | M=F*L(公式) | N=F-K+M(公式)
     * </pre>
     *
     * @return {"updated": N, "inserted": N} 或 {"error": "..."}
     */
    public Map<String, Object> upsertZuobiao(String spreadsheetId, List<ZuobiaoRow> rows,
            String productName, String region, String reportDate,
            String salesPerson, Integer agencyRatio, String operatorName) {
        init();
        if (sheets == null) return Map.of("error", "Sheets 未初始化");
        if (rows == null || rows.isEmpty()) return Map.of("updated", 0, "inserted", 0);

        try {
            // 1. 获取表格信息（取第一个 Sheet）
            Spreadsheet sp = sheets.spreadsheets().get(spreadsheetId).execute();
            List<Sheet> sheetList = sp.getSheets();
            if (sheetList == null || sheetList.isEmpty()) {
                // 极端情况：创建默认 Sheet1
                sheets.spreadsheets().batchUpdate(spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                                new Request().setAddSheet(new AddSheetRequest()
                                        .setProperties(new SheetProperties().setTitle("Sheet1"))))))
                        .execute();
                sp = sheets.spreadsheets().get(spreadsheetId).execute();
            }
            SheetProperties props = sp.getSheets().get(0).getProperties();
            String sheetName = props.getTitle();
            int sheetIdInt = props.getSheetId();
            int sheetRows = props.getGridProperties().getRowCount();

            // 2. 读取现有数据 A-N
            ValueRange vr = sheets.spreadsheets().values()
                    .get(spreadsheetId, "'" + sheetName + "'!A:N").execute();
            List<List<Object>> existing = vr.getValues();
            if (existing == null) existing = new ArrayList<>();

            // 找最后一行（A列有日期）
            int lastRow = 0;
            String lastDate = "";
            for (int i = existing.size() - 1; i >= 0; i--) {
                List<Object> r = existing.get(i);
                if (!r.isEmpty() && r.get(0) != null && !r.get(0).toString().isBlank()) {
                    lastRow = i + 1;
                    lastDate = r.get(0).toString().trim();
                    break;
                }
            }

            // 3. 构建去重索引: (A=日期, D=客户ID, J=广告系列)
            Map<String, Integer> existingIndex = new HashMap<>();
            for (int i = 0; i < existing.size(); i++) {
                List<Object> r = existing.get(i);
                String d = r.size() > 0 && r.get(0) != null ? r.get(0).toString().trim() : "";
                String cid = r.size() > 3 && r.get(3) != null ? r.get(3).toString().trim() : "";
                String cam = r.size() > 9 && r.get(9) != null ? r.get(9).toString().trim() : "";
                if (!d.isEmpty() || !cid.isEmpty() || !cam.isEmpty()) {
                    existingIndex.put(d + "|" + cid + "|" + cam, i);
                }
            }

            // 4. 构建 14 列新行（养户行覆盖 G/H/L）
            String percentStr = agencyRatio != null ? agencyRatio + "%" : "";
            List<List<Object>> newRows = new ArrayList<>();
            for (ZuobiaoRow row : rows) {
                newRows.add(row.toSheetRow(reportDate, operatorName,
                        productName, salesPerson, region, agencyRatio));
            }

            // 5. 分拣：updates（已有索引）vs appends（新数据）
            List<int[]> updateEntries = new ArrayList<>();  // [rowIndex, newRowIndex]
            List<Integer> appendIndices = new ArrayList<>();

            for (int i = 0; i < newRows.size(); i++) {
                List<Object> nr = newRows.get(i);
                String key = nr.get(0).toString().trim() + "|"
                        + nr.get(3).toString().trim() + "|"
                        + nr.get(9).toString().trim();
                if (existingIndex.containsKey(key)) {
                    updateEntries.add(new int[]{existingIndex.get(key), i});
                } else {
                    appendIndices.add(i);
                }
            }

            log.info("[GG-Sheets] 更新 {} 行，新增 {} 行", updateEntries.size(), appendIndices.size());

            // 6. 确保行数足够
            int maxRowNeeded = 0;
            for (int[] ue : updateEntries) {
                maxRowNeeded = Math.max(maxRowNeeded, ue[0] + 1);
            }
            if (!appendIndices.isEmpty()) {
                int appendStart = lastRow + 1;
                if (!lastDate.isEmpty() && !lastDate.equals(reportDate != null ? reportDate.trim() : "")) {
                    appendStart++;
                }
                maxRowNeeded = Math.max(maxRowNeeded, appendStart + appendIndices.size() - 1);
            }
            if (maxRowNeeded > sheetRows) {
                sheets.spreadsheets().batchUpdate(spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                                new Request().setAppendDimension(new AppendDimensionRequest()
                                        .setSheetId(sheetIdInt)
                                        .setDimension("ROWS")
                                        .setLength(maxRowNeeded - sheetRows + 10)))))
                        .execute();
                log.info("[GG-Sheets] 扩展行数 {} → {}", sheetRows, maxRowNeeded + 10);
            }

            // 7. 批量更新已有行
            if (!updateEntries.isEmpty()) {
                List<ValueRange> data = new ArrayList<>();
                for (int[] ue : updateEntries) {
                    int rowNum = ue[0] + 1; // 1-indexed
                    List<Object> rowData = newRows.get(ue[1]);
                    // 填入公式 M=F*L, N=F-K+M
                    rowData.set(12, "=F" + rowNum + "*L" + rowNum);
                    rowData.set(13, "=F" + rowNum + "-K" + rowNum + "+M" + rowNum);
                    data.add(new ValueRange()
                            .setRange("'" + sheetName + "'!A" + rowNum + ":N" + rowNum)
                            .setValues(List.of(rowData)));
                }
                sheets.spreadsheets().values().batchUpdate(spreadsheetId,
                        new BatchUpdateValuesRequest()
                                .setValueInputOption("USER_ENTERED")
                                .setData(data))
                        .execute();
            }

            // 8. 追加新行
            int appendStart = 0;
            int appendEnd = 0;
            if (!appendIndices.isEmpty()) {
                appendStart = lastRow + 1;
                if (!lastDate.isEmpty() && !lastDate.equals(reportDate != null ? reportDate.trim() : "")) {
                    appendStart++;
                }
                appendEnd = appendStart + appendIndices.size() - 1;

                List<List<Object>> appendValues = new ArrayList<>();
                for (int i = 0; i < appendIndices.size(); i++) {
                    int rowNum = appendStart + i;
                    List<Object> rowData = newRows.get(appendIndices.get(i));
                    // 填入公式
                    rowData.set(12, "=F" + rowNum + "*L" + rowNum);
                    rowData.set(13, "=F" + rowNum + "-K" + rowNum + "+M" + rowNum);
                    appendValues.add(rowData);
                }
                sheets.spreadsheets().values().update(spreadsheetId,
                        "'" + sheetName + "'!A" + appendStart + ":N" + appendEnd,
                        new ValueRange().setValues(appendValues))
                        .setValueInputOption("USER_ENTERED")
                        .execute();
            }

            // 9. 格式化 — D列文本，E列数字千分位（仅新追加行）
            if (!appendIndices.isEmpty()) {
                sheets.spreadsheets().batchUpdate(spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                                new Request().setRepeatCell(new RepeatCellRequest()
                                        .setRange(new GridRange()
                                                .setSheetId(sheetIdInt)
                                                .setStartColumnIndex(3)
                                                .setEndColumnIndex(4)
                                                .setStartRowIndex(appendStart - 1)
                                                .setEndRowIndex(appendEnd))
                                        .setCell(new CellData().setUserEnteredFormat(
                                                new CellFormat().setNumberFormat(
                                                        new NumberFormat().setType("TEXT"))))
                                        .setFields("userEnteredFormat.numberFormat")),
                                new Request().setRepeatCell(new RepeatCellRequest()
                                        .setRange(new GridRange()
                                                .setSheetId(sheetIdInt)
                                                .setStartColumnIndex(4)
                                                .setEndColumnIndex(5)
                                                .setStartRowIndex(appendStart - 1)
                                                .setEndRowIndex(appendEnd))
                                        .setCell(new CellData().setUserEnteredFormat(
                                                new CellFormat().setNumberFormat(
                                                        new NumberFormat().setType("NUMBER")
                                                                .setPattern("#,##0.00"))))
                                        .setFields("userEnteredFormat.numberFormat")))))
                        .execute();
            }

            return Map.of("updated", updateEntries.size(), "inserted", appendIndices.size());

        } catch (Exception e) {
            log.error("[GG-Sheets] 失败: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }

    // ═══════════ FB 做表 upsert（对齐 Python upsert_fb_reports）═══════════

    /**
     * FB 做表数据写入 — A 日期|B 运营|C 账户名称|D 广告账户ID|E 账号消耗|F 报给客户|G 客户名称|H 商务|I 投放国家|J 渠道号|K 平台实际|L 代投比例
     */
    public Map<String, Object> upsertFbReports(String spreadsheetId, List<FbReportRow> rows,
            Long userId, String productName, String reportDate, String region,
            String salesPerson, Integer agencyRatio, String operatorName) {
        init();
        if (sheets == null) return Map.of("error", "Sheets 未初始化");
        if (rows == null || rows.isEmpty()) return Map.of("updated", 0, "inserted", 0);

        String percentStr = agencyRatio != null ? agencyRatio + "%" : "";

        List<List<Object>> newRows = new ArrayList<>();
        for (FbReportRow fr : rows) {
            List<Object> row = new ArrayList<>(12);
            row.add(reportDate != null ? reportDate : "");                   // A
            row.add(operatorName != null ? operatorName : "");               // B
            row.add(fr.getAccountName() != null ? fr.getAccountName() : ""); // C
            // D列加单引号防止 Sheets 将纯数字 ID 转为科学计数法
            String aid = fr.getAccountId();
            row.add(aid != null && !aid.isEmpty() ? "'" + aid : "");         // D
            row.add(fr.getCost() != null ? fr.getCost() : 0);                // E
            row.add("");                                                      // F 报给客户
            row.add(productName != null ? productName : "");                 // G
            row.add(salesPerson != null ? salesPerson : "");                 // H
            row.add(region != null ? region : "");                           // I
            row.add(fr.getChannelNo() != null ? fr.getChannelNo() : "");     // J
            row.add("");                                                      // K 平台实际
            row.add(percentStr);                                              // L 代投比例
            newRows.add(row);
        }

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
                    lastRow = i + 1;
                    lastDate = r.get(0).toString().trim();
                    break;
                }
            }
            if (reportDate != null && !reportDate.isEmpty()
                    && !lastDate.isEmpty() && !lastDate.equals(reportDate)) {
                lastRow++;
            }

            // 去重索引：按 (A=日期, D=账户ID, G=产品名, J=线名) 四元组
            Map<String, Integer> existingMap = new HashMap<>();
            for (int i = 0; i < existing.size(); i++) {
                List<Object> r = existing.get(i);
                String d = r.size() > 0 && r.get(0) != null ? r.get(0).toString().trim() : "";
                String aid = r.size() > 3 && r.get(3) != null ? r.get(3).toString().trim() : "";
                String prod = r.size() > 6 && r.get(6) != null ? r.get(6).toString().trim() : "";
                String line = r.size() > 9 && r.get(9) != null ? r.get(9).toString().trim() : "";
                if (!d.isEmpty() && !aid.isEmpty()) {
                    existingMap.put(d + "|" + aid + "|" + prod + "|" + line, i);
                }
            }

            // 扩容
            int maxRows = sp.getSheets().get(0).getProperties().getGridProperties().getRowCount();
            int needed = lastRow + newRows.size() + 1;
            if (needed > maxRows) {
                sheets.spreadsheets().batchUpdate(spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(List.of(
                                new Request().setAppendDimension(new AppendDimensionRequest()
                                        .setSheetId(sp.getSheets().get(0).getProperties().getSheetId())
                                        .setDimension("ROWS")
                                        .setLength(needed - maxRows + 100)))))
                        .execute();
            }

            int updated = 0, inserted = 0;
            List<ValueRange> updates = new ArrayList<>();
            for (List<Object> nr : newRows) {
                String key = nr.get(0).toString().trim() + "|"
                        + nr.get(3).toString().trim() + "|"
                        + nr.get(6).toString().trim() + "|"
                        + nr.get(9).toString().trim();
                int target;
                if (existingMap.containsKey(key)) {
                    target = existingMap.get(key);
                    updated++;
                } else {
                    lastRow++;
                    target = lastRow;
                    inserted++;
                }
                updates.add(new ValueRange()
                        .setRange("'" + sheetName + "'!A" + (target + 1) + ":L" + (target + 1))
                        .setValues(List.of(nr)));
            }

            if (!updates.isEmpty()) {
                sheets.spreadsheets().values().batchUpdate(spreadsheetId,
                        new BatchUpdateValuesRequest()
                                .setValueInputOption("USER_ENTERED")
                                .setData(updates))
                        .execute();
            }
            log.info("[FB-Sheets] upsert: {}更新 {}新增", updated, inserted);
            return Map.of("updated", updated, "inserted", inserted);

        } catch (Exception e) {
            log.error("[FB-Sheets] 失败: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
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

    /** 列出指定 spreadsheet 的所有 sheet 名称 */
    public List<String> listSheets(String spreadsheetId) throws Exception {
        init();
        if (sheets == null) throw new RuntimeException("Sheets not initialized");
        Spreadsheet sp = sheets.spreadsheets().get(spreadsheetId).execute();
        List<String> names = new ArrayList<>();
        if (sp.getSheets() != null) {
            for (Sheet s : sp.getSheets()) {
                if (s.getProperties() != null && s.getProperties().getTitle() != null) {
                    names.add(s.getProperties().getTitle());
                }
            }
        }
        return names;
    }
}
