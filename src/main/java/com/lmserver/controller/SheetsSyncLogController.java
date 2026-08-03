package com.lmserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.SheetsSyncLog;
import com.lmserver.mapper.gg.SheetsSyncLogMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sheets 同步日志查询 — /api/sheets-sync-log/*。
 */
@RestController
@RequestMapping("/api/sheets-sync-log")
@RequiredArgsConstructor
public class SheetsSyncLogController {

    private final SheetsSyncLogMapper mapper;

    @GetMapping("/list")
    public ApiResponse<List<SheetsSyncLog>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(mapper.selectList(
                new LambdaQueryWrapper<SheetsSyncLog>().eq(SheetsSyncLog::getUserId, principal.getUserId())
                        .orderByDesc(SheetsSyncLog::getCreatedAt)));
    }

    @PostMapping("/retry/{id}")
    public ApiResponse<Void> retry(@PathVariable Long id) {
        var log = mapper.selectById(id);
        if (log != null) { log.setStatus("pending"); log.setRetryCount((log.getRetryCount() != null ? log.getRetryCount() : 0) + 1); log.setUpdatedAt(java.time.LocalDateTime.now()); mapper.updateById(log); }
        return ApiResponse.ok();
    }
}
