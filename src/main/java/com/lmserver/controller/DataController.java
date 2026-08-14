package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.DataImportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据导入导出控制器 — /api/data/*，用户级数据备份与恢复。
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final DataImportExportService dataService;

    /**
     * 导出数据 — 导出当前用户的所有业务数据为 JSON 文件。
     */
    @GetMapping("/export")
    public ApiResponse<String> exportData(@AuthenticationPrincipal UserPrincipal principal) {
        String json = dataService.exportUserData(principal.getUserId());
        return json != null ? ApiResponse.ok(json) : ApiResponse.fail("导出失败");
    }

    /**
     * 导入数据 — 从 JSON 文件恢复用户数据。
     */
    @GetMapping("/export/download")
    public void exportDownload(@AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse resp) throws java.io.IOException {
        String json = dataService.exportUserData(principal.getUserId());
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=lm-server-export.json");
        resp.getWriter().write(json);
    }

    @PostMapping("/import")
    public ApiResponse<Integer> importData(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        int count = dataService.importUserData(principal.getUserId(), file);
        return count >= 0 ? ApiResponse.ok(count) : ApiResponse.fail("导入失败，请检查文件格式");
    }

    /**
     * 导入历史 — 获取当前用户的导入操作记录。
     */
    @GetMapping("/history")
    public ApiResponse<?> importHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(dataService.getImportHistory(principal.getUserId()));
    }

    /** 导入历史（对齐 GG-Server /api/data/import-history，返回 {success, history} 无 data 层） */
    @GetMapping("/import-history")
    public Map<String, Object> importHistoryV2(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("history", dataService.getImportHistory(principal.getUserId()));
        return result;
    }
}
