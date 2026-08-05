package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.common.AuditLog;
import com.lmserver.entity.gg.Packages;
import com.lmserver.entity.gg.Products;
import com.lmserver.mapper.common.AuditLogMapper;
import com.lmserver.mapper.gg.PackagesMapper;
import com.lmserver.mapper.gg.ProductsMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/audit-log")
@RequiredArgsConstructor
public class AuditController {

    @Autowired private AuditLogMapper auditLogMapper;
    @Autowired private ProductsMapper productsMapper;
    @Autowired private PackagesMapper packagesMapper;

    @GetMapping("/list")
    public com.lmserver.dto.response.PagedResponse<AuditLog> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AuditLog>()
                .orderByDesc(AuditLog::getCreatedAt);
        var pg = auditLogMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), qw);
        return com.lmserver.dto.response.PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @PostMapping("/restore/{logId}")
    public ApiResponse<Void> restore(@PathVariable Long logId, @AuthenticationPrincipal UserPrincipal principal) {
        AuditLog log = auditLogMapper.selectById(logId);
        if (log == null || !"delete_product".equals(log.getAction())) return ApiResponse.fail("无法恢复");
        Products p = new Products();
        p.setProductName(log.getTargetName());
        p.setOwnerId(principal.getUserId());
        p.setIsArchived(0L);
        p.setCreatedAt(LocalDateTime.now());
        productsMapper.insert(p);
        log.setDetail("{\"restored_by\":" + principal.getUserId() + "}");
        auditLogMapper.updateById(log);
        return ApiResponse.ok();
    }
}
