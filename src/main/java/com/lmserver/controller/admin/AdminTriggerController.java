package com.lmserver.controller.admin;

import com.lmserver.config.ScheduledTasks;
import com.lmserver.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员触发器控制器 — /api/admin/trigger-*。
 * 手动触发定时任务，仅 developer 角色可调用。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminTriggerController {

    private final ScheduledTasks scheduledTasks;

    /** 手动触发周清理 */
    @PostMapping("/trigger-cleanup")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ApiResponse<Void> triggerCleanup() {
        scheduledTasks.weeklyCleanup();
        return ApiResponse.ok();
    }

    /** 手动触发掉包检测 */
    @PostMapping("/trigger-delist")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ApiResponse<Void> triggerDelist() {
        scheduledTasks.checkDelist();
        return ApiResponse.ok();
    }
}
