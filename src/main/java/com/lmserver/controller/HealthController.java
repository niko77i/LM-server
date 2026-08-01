package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * 健康检查控制器 — GET /api/health，返回服务运行状态
 */

@RestController
public class HealthController {

    @GetMapping("/api/health")
    /** 健康检查 — 返回服务运行状态 */
    public ApiResponse<String> health() {
        return ApiResponse.ok("LM-Server is running");
    }
}
