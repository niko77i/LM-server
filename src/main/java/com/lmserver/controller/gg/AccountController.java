package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * 账户管理控制器 — /api/accounts/*，GG平台广告账户的CRUD+软删除+下拉选项
 */

/**
 * 账户管理控制器 — /api/accounts/*，GG平台广告账户的CRUD+软删除+下拉选项
 */

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<Accounts> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long mccId,
            @RequestParam(required = false) Long agentId) {
        return accountService.list(principal.getUserId(), page, size, search, statusId, mccId, agentId);
    }

    @GetMapping("/{id}")
    /** 获取单条记录详情 — 按主键 ID 查询 */
    public ApiResponse<Accounts> detail(@PathVariable Long id) {
        Accounts a = accountService.getById(id);
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<Accounts> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String accountId = (String) body.get("account_id");
        if (name == null || accountId == null) return ApiResponse.fail("名称和账户ID不能为空");
        return ApiResponse.ok(accountService.create(principal.getUserId(), name, accountId,
                lng(body, "mcc_id"), lng(body, "agent_id"), lng(body, "status_id"),
                (String) body.get("timezone")));
    }

    @PutMapping("/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<Accounts> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Accounts a = accountService.update(id,
                (String) body.get("name"), lng(body, "mcc_id"), lng(body, "agent_id"),
                lng(body, "status_id"), (String) body.get("timezone"));
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    @DeleteMapping("/{id}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/options")
    /** 获取下拉选项 — 返回 id + name 的简略列表 */
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(accountService.options(principal.getUserId()));
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Integer> batchDelete(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, List<Long>> body) {
        int c = 0;
        for (Long id : body.getOrDefault("ids", List.of())) { accountService.delete(id); c++; }
        return ApiResponse.ok(c);
    }

    @PostMapping("/restore/{id}")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        com.lmserver.entity.gg.Accounts a = accountService.getById(id);
        if (a != null) { a.setDeletedAt(null); accountService.update(id, null, null, null, null, null); }
        return ApiResponse.ok();
    }

    @GetMapping("/lookup")
    public ApiResponse<?> lookup(@RequestParam String accountId) {
        var list = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.lmserver.entity.gg.Accounts>()
                .eq(com.lmserver.entity.gg.Accounts::getAccountId, accountId);
        return ApiResponse.ok(null); // TODO: inject mapper
    }

    @GetMapping("/recharge-records")
    public ApiResponse<?> rechargeRecords(@RequestParam String accountId) {
        return ApiResponse.ok(List.of()); // TODO: inject RechargeRecordsMapper
    }

    private Long lng(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Long.valueOf(v.toString()) : null; }
}
