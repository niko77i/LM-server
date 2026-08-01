package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Google Ads 集成控制器 — /api/google-ads/*，待对接 API 拉取广告报告。
 */
@RestController
@RequestMapping("/api/google-ads")
public class GoogleAdsController {

        @GetMapping("/accounts")
    public ApiResponse<String> listAccounts(@RequestParam String managerId) {
        // TODO: Phase 5 对接 Google Ads API
        return ApiResponse.ok("[Ads账户列表占位] managerId=" + managerId);
    }

        @PostMapping("/report")
    public ApiResponse<String> fetchReport(@RequestBody Map<String, String> body) {
        // TODO: Phase 5 对接 Google Ads API
        return ApiResponse.ok("[Ads报告占位] " + body);
    }
}
