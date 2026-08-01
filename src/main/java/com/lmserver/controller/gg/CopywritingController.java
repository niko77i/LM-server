package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.Copywritings;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.CopywritingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * 文案管理控制器 — /api/copywriting/*，营销文案的CRUD+批量删除
 */

@RestController
@RequestMapping("/api/copywriting")
@RequiredArgsConstructor
public class CopywritingController {

    private final CopywritingService service;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<Copywritings> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String region) {
        return service.list(principal.getUserId(), page, size, region);
    }

    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<Copywritings> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String region = (String) body.getOrDefault("region", "通用");
        String content = (String) body.get("content");
        if (content == null || content.isBlank()) return ApiResponse.fail("内容不能为空");
        return ApiResponse.ok(service.create(principal.getUserId(), region, content,
                body.get("is_public") != null ? Integer.valueOf(body.get("is_public").toString()) : 0));
    }

    @PutMapping("/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<Copywritings> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Copywritings c = service.update(id, body.get("region"), body.get("content"), body.get("effectiveness"));
        return c != null ? ApiResponse.ok(c) : ApiResponse.fail("不存在");
    }

    @DeleteMapping("/{id}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable Long id) { service.delete(id); return ApiResponse.ok(); }

    @PostMapping("/batch-delete")
    /** 批量删除 — 按 ID 列表批量删除 */
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        service.batchDelete(body.getOrDefault("ids", List.of())); return ApiResponse.ok();
    }
}
