package com.lmserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.common.ImportHistory;
import com.lmserver.mapper.common.ImportHistoryMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 导入历史查询 — /api/import-history/*。
 */
@RestController
@RequestMapping("/api/import-history")
@RequiredArgsConstructor
public class ImportHistoryController {

    private final ImportHistoryMapper mapper;

    @GetMapping("/list")
    public ApiResponse<List<ImportHistory>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(mapper.selectList(
                new LambdaQueryWrapper<ImportHistory>().eq(ImportHistory::getUserId, principal.getUserId())
                        .orderByDesc(ImportHistory::getCreatedAt)));
    }
}
