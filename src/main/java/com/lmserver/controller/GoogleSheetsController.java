package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Google Sheets 集成控制器 — /api/google-sheets/*。
 * Phase 5: 待对接 Google Sheets API（读取/写入/同步）。
 */
@RestController
@RequestMapping("/api/google-sheets")
public class GoogleSheetsController {

    /** 读取 Sheet 数据（占位） */
    @GetMapping("/read")
    public ApiResponse<String> read(@RequestParam String spreadsheetId,
            @RequestParam(defaultValue = "A1:Z1000") String range) {
        // TODO: Phase 5 对接 Google Sheets API
        return ApiResponse.ok("[Sheet读取占位] " + spreadsheetId + " " + range);
    }

    /** 写入 Sheet 数据（占位） */
    @PostMapping("/write")
    public ApiResponse<String> write(@RequestBody Map<String, Object> body) {
        // TODO: Phase 5 对接 Google Sheets API
        return ApiResponse.ok("[Sheet写入占位]");
    }

    /** 同步日志查询 */
    @GetMapping("/sync-log")
    public ApiResponse<?> syncLog() {
        // TODO: Phase 5 从 sheets_sync_log 表查询
        return ApiResponse.ok("[同步日志占位]");
    }

    /** 重试失败同步 */
    @PostMapping("/retry/{logId}")
    public ApiResponse<String> retry(@PathVariable Long logId) {
        // TODO: Phase 5 重新执行失败同步
        return ApiResponse.ok("[重试占位] id=" + logId);
    }
}
