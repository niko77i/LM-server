package com.lmserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.AuditLog;
import com.lmserver.mapper.common.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/**
 * 审计日志控制器 — /api/audit-log/*，操作审计记录的查询
 */

@RestController
@RequestMapping("/api/audit-log")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogMapper mapper;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<AuditLog> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String action) {
        var qw = new LambdaQueryWrapper<AuditLog>();
        if (action != null && !action.isBlank()) qw.eq(AuditLog::getAction, action);
        qw.orderByDesc(AuditLog::getCreatedAt);
        var pg = mapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }
}
