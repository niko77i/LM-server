package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.RechargeRecords;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.RechargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * 充值管理控制器 — /api/recharge/*，GG平台充值记录的CRUD
 */

@RestController
@RequestMapping("/api/recharge")
@RequiredArgsConstructor
public class RechargeController {

    private final RechargeService rechargeService;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<RechargeRecords> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String accountId) {
        return rechargeService.list(principal.getUserId(), page, size, accountId);
    }

    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<RechargeRecords> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String accountId = (String) body.get("account_id");
        String amount = (String) body.get("amount");
        if (accountId == null || amount == null) return ApiResponse.fail("账户ID和金额不能为空");
        return ApiResponse.ok(rechargeService.create(principal.getUserId(), accountId, amount,
                (String) body.get("operator"), (String) body.get("status"), lng(body, "agent_id")));
    }

    @PutMapping("/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<RechargeRecords> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RechargeRecords r = rechargeService.update(id, (String) body.get("amount"),
                (String) body.get("status"), (String) body.get("operator"));
        return r != null ? ApiResponse.ok(r) : ApiResponse.fail("记录不存在");
    }

    @PostMapping("/batch-create")
    public ApiResponse<Integer> batchCreate(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody List<Map<String, Object>> items) {
        int c = 0;
        for (Map<String, Object> item : items) {
            rechargeService.create(principal.getUserId(),
                    (String) item.get("account_id"), (String) item.get("amount"),
                    (String) item.get("operator"), (String) item.get("status"),
                    item.get("agent_id") != null ? Long.valueOf(item.get("agent_id").toString()) : null);
            c++;
        }
        return ApiResponse.ok(c);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        rechargeService.delete(id);
        return ApiResponse.ok();
    }

    private Long lng(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Long.valueOf(v.toString()) : null; }
}
