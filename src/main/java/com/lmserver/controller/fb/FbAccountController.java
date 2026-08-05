package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.FbAccountBm;
import com.lmserver.entity.fb.FbAccountBmHistory;
import com.lmserver.entity.fb.FbAccounts;
import com.lmserver.mapper.fb.FbAccountBmHistoryMapper;
import com.lmserver.mapper.fb.FbAccountBmMapper;
import com.lmserver.mapper.fb.FbAccountsMapper;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.FbService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * FB 账户管理控制器 — 对齐 Python fb_routes.py。
 */
@RestController
@RequestMapping("/api/fb/accounts")
@RequiredArgsConstructor
public class FbAccountController {

    private final FbService fbService;

    @Autowired private FbAccountsMapper accountsMapper;
    @Autowired private FbAccountBmMapper accountBmMapper;
    @Autowired private FbAccountBmHistoryMapper accountBmHistoryMapper;

    @GetMapping("/list")
    public PagedResponse<FbAccounts> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search, @RequestParam(required = false) Long statusId) {
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
        return ApiResponse.ok(fbService.createAccount(principal.getUserId(), name, accountId, statusId,
                (String) body.get("timezone")));
    }

    @PutMapping("/{id}")
    public ApiResponse<FbAccounts> update(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        Long statusId = body.get("status_id") != null ? Long.valueOf(body.get("status_id").toString()) : null;
        FbAccounts a = fbService.updateAccount(id, (String) body.get("name"), statusId, (String) body.get("timezone"));
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("账户不存在");
    }

    /** 软删除 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        FbAccounts a = accountsMapper.selectById(id);
        if (a != null && a.getOwnerId().equals(principal.getUserId())) {
            a.setDeletedAt(LocalDateTime.now());
            accountsMapper.updateById(a);
        }
        return ApiResponse.ok();
    }

    /** 已删除账户列表 */
    @GetMapping("/deleted")
    public PagedResponse<FbAccounts> deleted(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        var qw = new LambdaQueryWrapper<FbAccounts>()
                .eq(FbAccounts::getOwnerId, principal.getUserId())
                .isNotNull(FbAccounts::getDeletedAt)
                .orderByDesc(FbAccounts::getUpdatedAt);
        var pg = accountsMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    /** 恢复软删除 */
    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restore(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        FbAccounts a = accountsMapper.selectById(id);
        if (a != null && a.getOwnerId().equals(principal.getUserId())) {
            a.setDeletedAt(null);
            accountsMapper.updateById(a);
        }
        return ApiResponse.ok();
    }

    /** 物理删除 */
    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> permanentDelete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        FbAccounts a = accountsMapper.selectById(id);
        if (a == null || !a.getOwnerId().equals(principal.getUserId())) return ApiResponse.fail("账户不存在或无权限");
        if (a.getDeletedAt() == null) return ApiResponse.fail("请先删除账户再永久删除");
        // 清理关联
        accountBmMapper.delete(new LambdaQueryWrapper<FbAccountBm>().eq(FbAccountBm::getAccountId, id));
        accountBmHistoryMapper.delete(new LambdaQueryWrapper<FbAccountBmHistory>().eq(FbAccountBmHistory::getAccountId, id));
        accountsMapper.deleteById(id);
        return ApiResponse.ok();
    }

    /** BM 迁移历史 */
    @GetMapping("/{id}/bm-history")
    public ApiResponse<?> bmHistory(@PathVariable Long id) {
        return ApiResponse.ok(accountBmHistoryMapper.selectList(
                new LambdaQueryWrapper<FbAccountBmHistory>()
                        .eq(FbAccountBmHistory::getAccountId, id)
                        .orderByDesc(FbAccountBmHistory::getCreatedAt)));
    }
}
