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

@RestController
@RequestMapping("/api/fb/accounts")
@RequiredArgsConstructor
public class FbAccountController {

    private final FbService fbService;

    @GetMapping("/list")
    public PagedResponse<FbAccounts> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long statusId) {
        return fbService.listAccounts(principal.getUserId(), page, size, search, statusId);
    }

    @GetMapping("/{id}")
    public ApiResponse<FbAccounts> detail(@PathVariable Long id) {
        FbAccounts a = fbService.getAccountById(id);
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    @PostMapping("/create")
    public ApiResponse<FbAccounts> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name"), accountId = (String) body.get("account_id");
        if (name == null || accountId == null) return ApiResponse.fail("名称和账户ID不能为空");
        Long statusId = body.get("status_id") != null ? Long.valueOf(body.get("status_id").toString()) : null;
        return ApiResponse.ok(fbService.createAccount(principal.getUserId(), name, accountId, statusId, (String) body.get("timezone")));
    }

    @PutMapping("/{id}")
    public ApiResponse<FbAccounts> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long statusId = body.get("status_id") != null ? Long.valueOf(body.get("status_id").toString()) : null;
        FbAccounts a = fbService.updateAccount(id, (String) body.get("name"), statusId, (String) body.get("timezone"));
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { fbService.deleteAccount(id); return ApiResponse.ok(); }
}
