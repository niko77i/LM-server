package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Mcc;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.MccService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * MCC 管理控制器 — /api/mcc/*，GG平台MCC的CRUD+下拉选项
 */

/**
 * MCC 管理控制器 — /api/mcc/*，GG平台MCC的CRUD+下拉选项
 */

@RestController
@RequestMapping("/api/mcc")
@RequiredArgsConstructor
public class MccController {

    private final MccService mccService;
    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<Mcc> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long levelId) {
        return mccService.list(principal.getUserId(), page, size, search, levelId);
    }
    @GetMapping("/{id}")
    /** 获取单条记录详情 — 按主键 ID 查询 */
    public ApiResponse<Mcc> detail(@PathVariable Long id) {
        Mcc m = mccService.getById(id);
        return m != null ? ApiResponse.ok(m) : ApiResponse.fail("MCC不存在");
    }
    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<Mcc> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String mccId = (String) body.get("mcc_id");
        if (name == null || name.isBlank() || mccId == null || mccId.isBlank()) {
            return ApiResponse.fail("名称和MCC ID不能为空");
        }
        Long levelId = body.get("level_id") != null ? Long.valueOf(body.get("level_id").toString()) : null;
        Long parentId = body.get("parent_mcc_id") != null ? Long.valueOf(body.get("parent_mcc_id").toString()) : null;
        return ApiResponse.ok(mccService.create(principal.getUserId(), name, mccId, levelId, parentId));
    }
    @PutMapping("/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<Mcc> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long levelId = body.get("level_id") != null ? Long.valueOf(body.get("level_id").toString()) : null;
        Long parentId = body.get("parent_mcc_id") != null ? Long.valueOf(body.get("parent_mcc_id").toString()) : null;
        Mcc m = mccService.update(id, name, levelId, parentId);
        return m != null ? ApiResponse.ok(m) : ApiResponse.fail("MCC不存在");
    }
    @DeleteMapping("/{id}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mccService.delete(id);
        return ApiResponse.ok();
    }
    @GetMapping("/options")
    /** 获取下拉选项 — 返回 id + name 的简略列表 */
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(mccService.options(principal.getUserId()));
    }
}
