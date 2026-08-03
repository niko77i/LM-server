package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.service.GoogleAdsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Google Ads 集成控制器 — /api/google-ads/*，凭证从数据库 config 表动态获取。
 */
@Slf4j
@RestController
@RequestMapping("/api/google-ads")
@RequiredArgsConstructor
public class GoogleAdsController {

    private final GoogleAdsService adsService;

    /** 列出经理账户下的子账户列表 */
    @GetMapping("/accounts")
    public ApiResponse<List<String>> listAccounts(@RequestBody Map<String, String> auth) {
        List<String> accounts = adsService.listAccounts(auth);
        return ApiResponse.ok(accounts);
    }

    /** 拉取指定账户的广告系列报告 */
    @PostMapping("/report")
    public ApiResponse<List<Map<String, Object>>> fetchReport(@RequestBody Map<String, String> body) {
        String accountId = body.get("account_id");
        String startDate = body.getOrDefault("start_date", "");
        String endDate = body.getOrDefault("end_date", "");
        if (accountId == null) return ApiResponse.fail("账户ID不能为空");

        // auth 参数从 body 中提取（与 Python 一致：client_id, client_secret, refresh_token, developer_token, manager_id）
        Map<String, String> auth = new HashMap<>(body);
        List<Map<String, Object>> rows = adsService.fetchCampaignReport(auth, accountId, startDate, endDate);
        return ApiResponse.ok(rows);
    }

    @PostMapping("/sync")
    public ApiResponse<String> syncCampaigns(@RequestBody Map<String, String> body) {
        return ApiResponse.ok("同步已触发"); // TODO: Google Ads API实际调用
    }
}
