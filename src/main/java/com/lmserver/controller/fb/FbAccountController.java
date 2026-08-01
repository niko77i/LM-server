package com.lmserver.controller.fb;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.FbAccounts;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.FbService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * FB 账户管理控制器 — /api/fb/accounts/*，FB广告账户的CRUD+软删除
 */

@RestController
@RequestMapping("/api/fb/accounts")
@RequiredArgsConstructor
public class FbAccountController {

    private final FbService fbService;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<FbAccounts> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long statusId) {
        return fbService.listAccounts(principal.getUserId(), page, size, search, statusId);
    }

    @GetMapping("/{id}")
    /** 获取单条记录详情 — 按主键 ID 查询 */
    public ApiResponse<FbAccounts> detail(@PathVariable Long id) {
        FbAccounts a = fbService.getAccountById(id);
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<FbAccounts> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name"), accountId = (String) body.get("account_id");
        if (name == null || accountId == null) return ApiResponse.fail("名称和账户ID不能为空");
        Long statusId = body.get("status_id") != null ? Long.valueOf(body.get("status_id").toString()) : null;
        return ApiResponse.ok(fbService.createAccount(principal.getUserId(), name, accountId, statusId, (String) body.get("timezone")));
    }

    @PutMapping("/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<FbAccounts> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long statusId = body.get("status_id") != null ? Long.valueOf(body.get("status_id").toString()) : null;
        FbAccounts a = fbService.updateAccount(id, (String) body.get("name"), statusId, (String) body.get("timezone"));
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    @DeleteMapping("/{id}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable Long id) { fbService.deleteAccount(id); return ApiResponse.ok(); }
}
