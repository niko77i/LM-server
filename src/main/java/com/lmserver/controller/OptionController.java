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
/**
 * 选项管理控制器 — /api/{type}/*，通过路径变量统一分发5个选项表的CRUD
 */

/**
 * 选项管理控制器 — /api/{type}/*，通过路径变量统一分发5个选项表的CRUD
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;
    private static final java.util.Set<String> VALID_TYPES = java.util.Set.of(
            "agents", "statuses", "mcc-levels", "sales-persons", "regions");
    @GetMapping("/api/{type}/list")
    /** 分页列表查询 — 支持多条件筛选 */
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
    @PostMapping("/api/{type}/create")
    /** 新增记录 — 返回创建后的完整对象 */
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
    @PutMapping("/api/{type}/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<?> update(@PathVariable String type, @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        validate(type);
        String name = body.get("name");
        if (name == null || name.isBlank()) return ApiResponse.fail("名称不能为空");
        Object result = optionService.update(type, id, name);
        return result != null ? ApiResponse.ok(result) : ApiResponse.fail("记录不存在");
    }
    @DeleteMapping("/api/{type}/{id}")
    /** 删除记录 */
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
