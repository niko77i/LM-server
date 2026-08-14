package com.lmserver.controller.admin;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.mapper.common.*;
import com.lmserver.mapper.gg.*;
import com.lmserver.mapper.fb.*;
import com.lmserver.service.DataImportExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员数据管理 — /api/admin/data/*。仅 ADMIN/DEVELOPER 角色。
 */
@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN')")
public class AdminDataController {

    private final AccountsMapper accountsMapper;
    private final ProductsMapper productsMapper;
    private final VideosMapper videosMapper;
    private final UsersMapper usersMapper;
    private final FbBmsMapper fbBmsMapper;
    private final FbAccountsMapper fbAccountsMapper;
    private final DataImportExportService dataService;

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(Map.of(
            "accounts", accountsMapper.selectCount(null),
            "products", productsMapper.selectCount(null),
            "videos", videosMapper.selectCount(null),
            "users", usersMapper.selectCount(null),
            "fbBms", fbBmsMapper.selectCount(null),
            "fbAccounts", fbAccountsMapper.selectCount(null)
        ));
    }

    private final com.lmserver.mapper.gg.AdReportsMapper adReportsMapper;
    private final com.lmserver.mapper.fb.FbAdReportsMapper fbAdReportsMapper;
    @PostMapping("/import")
    public ApiResponse<Integer> importData(@RequestBody Map<String, Object> body) {
        int count = 0;
        @SuppressWarnings("unchecked") List<Map<String,Object>> ads = (List<Map<String,Object>>) body.getOrDefault("ad_reports", List.of());
        for (var r : ads) { try { var report = new com.lmserver.entity.gg.AdReports();
            report.setUserId(Long.valueOf(r.get("user_id").toString())); report.setProductName((String)r.get("product_name"));
            report.setCost(r.get("cost")!=null?Double.valueOf(r.get("cost").toString()):0); adReportsMapper.insert(report); count++; } catch(Exception ignored){} }
        @SuppressWarnings("unchecked") List<Map<String,Object>> fbs = (List<Map<String,Object>>) body.getOrDefault("fb_ad_reports", List.of());
        for (var r : fbs) { try { var report = new com.lmserver.entity.fb.FbAdReports();
            report.setUserId(Long.valueOf(r.get("user_id").toString())); report.setProductName((String)r.get("product_name"));
            report.setCost(r.get("cost")!=null?Double.valueOf(r.get("cost").toString()):0); fbAdReportsMapper.insert(report); count++; } catch(Exception ignored){} }
        return ApiResponse.ok(count);
    }

    /** 管理员导出指定用户数据（对齐 GG-Server /api/admin/data/export/{uid}） */
    @GetMapping("/export/{userId}")
    public void exportUserData(@PathVariable Long userId, HttpServletResponse resp) throws java.io.IOException {
        String json = dataService.exportUserData(userId);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=gg-server-export.json");
        resp.getWriter().write(json);
    }
}
