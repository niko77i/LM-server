package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Google Sheets 集成控制器 — /api/google-sheets/*。
 * 读写 Google 电子表格，服务账号认证。
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

    /** 追加数据到 Sheet 末尾 */
    @PostMapping("/append")
    public ApiResponse<String> append(@RequestBody Map<String, Object> body) {
        try {
            String spreadsheetId = (String) body.get("spreadsheet_id");
            String range = (String) body.getOrDefault("range", "A1");
            @SuppressWarnings("unchecked")
            List<List<Object>> values = (List<List<Object>>) body.get("values");
            sheetsService.append(spreadsheetId, range, values);
            return ApiResponse.ok("追加成功");
        } catch (Exception e) {
            log.error("Sheet 追加失败", e);
            return ApiResponse.fail("追加失败: " + e.getMessage());
        }
    }
}
