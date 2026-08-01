package com.lmserver.controller.fb;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.FbBms;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.FbService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * FB BM 管理控制器 — /api/fb/bms/*，BM的CRUD+软删除+下拉选项
 */

/**
 * FB BM 管理控制器 — /api/fb/bms/*，BM的CRUD+软删除+下拉选项
 */

@RestController
@RequestMapping("/api/fb/bms")
@RequiredArgsConstructor
public class FbBmController {

    private final FbService fbService;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<FbBms> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return fbService.listBms(principal.getUserId(), page, size, search, status);
    }

    @GetMapping("/{id}")
    /** 获取单条记录详情 — 按主键 ID 查询 */
    public ApiResponse<FbBms> detail(@PathVariable Long id) {
        FbBms b = fbService.getBmById(id);
        return b != null ? ApiResponse.ok(b) : ApiResponse.fail("BM不存在");
    }

    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<FbBms> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        String name = body.get("name"), bmId = body.get("bm_id");
        if (name == null || bmId == null) return ApiResponse.fail("名称和BM ID不能为空");
        return ApiResponse.ok(fbService.createBm(principal.getUserId(), name, bmId, body.get("note")));
    }

    @PutMapping("/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<FbBms> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FbBms b = fbService.updateBm(id, body.get("name"), body.get("note"));
        return b != null ? ApiResponse.ok(b) : ApiResponse.fail("BM不存在");
    }

    @DeleteMapping("/{id}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable Long id) { fbService.deleteBm(id); return ApiResponse.ok(); }

    @GetMapping("/options")
    /** 获取下拉选项 — 返回 id + name 的简略列表 */
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(fbService.bmOptions(principal.getUserId()));
    }
}
