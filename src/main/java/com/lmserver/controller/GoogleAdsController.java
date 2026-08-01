package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Google Ads 集成控制器 — /api/google-ads/*。
 * Phase 5: 待对接 Google Ads API（拉取广告系列报告）。
 */
@RestController
@RequestMapping("/api/google-ads")
public class GoogleAdsController {

    /** 列出经理账户下的子账户（占位） */
    @GetMapping("/accounts")
    public ApiResponse<String> listAccounts(@RequestParam String managerId) {
        // TODO: Phase 5 对接 Google Ads API
        return ApiResponse.ok("[Ads账户列表占位] managerId=" + managerId);
    }

    /** 拉取广告系列报告（占位） */
    @PostMapping("/report")
    public ApiResponse<String> fetchReport(@RequestBody Map<String, String> body) {
        // TODO: Phase 5 对接 Google Ads API
        return ApiResponse.ok("[Ads报告占位] " + body);
    }
}
