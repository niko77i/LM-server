package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.OptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 选项管理: agents / statuses / mcc-levels / sales-persons / regions
 * <p>
 * 通过路径变量 {type} 区分不同选项类型，避免 5 个 Controller 重复代码。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;
    private static final java.util.Set<String> VALID_TYPES = java.util.Set.of(
            "agents", "statuses", "mcc-levels", "sales-persons", "regions");

    /** GET /api/{type}/list — 列表（按 owner_id 隔离，分页） */
    @GetMapping("/api/{type}/list")
    public PagedResponse<?> list(@PathVariable String type,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        validate(type);
        List<?> all = optionService.list(type, principal.getUserId(), principal.getPlatform());
        int total = all.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<?> items = from < total ? all.subList(from, to) : List.of();
        return PagedResponse.of(items, total, page, size);
    }

    /** POST /api/{type}/create — 新增 */
    @PostMapping("/api/{type}/create")
    public ApiResponse<?> create(@PathVariable String type,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        validate(type);
        String name = body.get("name");
        if (name == null || name.isBlank()) return ApiResponse.fail("名称不能为空");
        // regions 支持 timezone
        Object result = optionService.create(type, name, principal.getUserId(), principal.getPlatform());
        return ApiResponse.ok(result);
    }

    /** PUT /api/{type}/{id} — 更新 */
    @PutMapping("/api/{type}/{id}")
    public ApiResponse<?> update(@PathVariable String type, @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        validate(type);
        String name = body.get("name");
        if (name == null || name.isBlank()) return ApiResponse.fail("名称不能为空");
        Object result = optionService.update(type, id, name);
        return result != null ? ApiResponse.ok(result) : ApiResponse.fail("记录不存在");
    }

    /** DELETE /api/{type}/{id} — 删除 */
    @DeleteMapping("/api/{type}/{id}")
    public ApiResponse<Void> delete(@PathVariable String type, @PathVariable Long id) {
        validate(type);
        optionService.delete(type, id);
        return ApiResponse.ok();
    }

    private void validate(String type) {
        if (!VALID_TYPES.contains(type)) {
            throw new IllegalArgumentException("无效的选项类型: " + type);
        }
    }
}
