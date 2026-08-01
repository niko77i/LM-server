package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/list")
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
    public ApiResponse<Accounts> detail(@PathVariable Long id) {
        Accounts a = accountService.getById(id);
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    @PostMapping("/create")
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
    public ApiResponse<Accounts> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Accounts a = accountService.update(id,
                (String) body.get("name"), lng(body, "mcc_id"), lng(body, "agent_id"),
                lng(body, "status_id"), (String) body.get("timezone"));
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/options")
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(accountService.options(principal.getUserId()));
    }

    private Long lng(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Long.valueOf(v.toString()) : null; }
}
