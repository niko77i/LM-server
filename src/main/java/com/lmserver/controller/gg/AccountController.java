package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.dto.response.SyncResult;
import com.lmserver.entity.gg.AccountMccHistory;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.entity.gg.RechargeRecords;
import com.lmserver.mapper.gg.AccountMccHistoryMapper;
import com.lmserver.mapper.gg.AccountsMapper;
import com.lmserver.mapper.gg.RechargeRecordsMapper;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * GG 账户管理控制器 — v1.5: 21 接口。
 * 双向Sheet同步、软删除/恢复/物理删除、已删除列表、H列解绑。
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountsMapper accountsMapper;
    private final RechargeRecordsMapper rechargeRecordsMapper;
    private final AccountMccHistoryMapper mccHistoryMapper;

    @GetMapping("/list") public PagedResponse<Accounts> list(
            @AuthenticationPrincipal UserPrincipal p, @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int size, @RequestParam(required=false) String search,
            @RequestParam(required=false) Long statusId, @RequestParam(required=false) Long mccId,
            @RequestParam(required=false) Long agentId) {
        return accountService.list(p.getUserId(), page, size, search, statusId, mccId, agentId);
    }

    @GetMapping("/{id}") public ApiResponse<Accounts> detail(@PathVariable Long id) {
        Accounts a = accountService.getById(id); return a != null ? ApiResponse.ok(a) : ApiResponse.fail("不存在");
    }

    @PostMapping("/create") public ApiResponse<Accounts> create(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(accountService.create(p.getUserId(), (String)body.get("name"),
                (String)body.get("account_id"), lng(body,"mcc_id"), lng(body,"agent_id"),
                lng(body,"status_id"), (String)body.get("timezone")));
    }

    @PutMapping("/{id}") public ApiResponse<Accounts> update(@PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Accounts a = accountService.update(id, (String)body.get("name"), lng(body,"mcc_id"),
                lng(body,"agent_id"), lng(body,"status_id"), (String)body.get("timezone"));
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("不存在");
    }

    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id) {
        accountService.delete(id); return ApiResponse.ok();
    }

    @PostMapping("/{id}/restore") public ApiResponse<Void> restore(@PathVariable Long id) {
        Accounts a = accountsMapper.selectById(id);
        if (a != null) { a.setDeletedAt(null); accountsMapper.updateById(a); }
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}/permanent") public ApiResponse<Void> permanentDelete(@PathVariable Long id) {
        rechargeRecordsMapper.delete(new LambdaQueryWrapper<RechargeRecords>().eq(RechargeRecords::getAccountId,
                accountsMapper.selectById(id) != null ? accountsMapper.selectById(id).getAccountId() : ""));
        mccHistoryMapper.delete(new LambdaQueryWrapper<AccountMccHistory>().eq(AccountMccHistory::getAccountId, id));
        accountsMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/deleted") public PagedResponse<Accounts> deleted(@AuthenticationPrincipal UserPrincipal p,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) {
        var qw = new LambdaQueryWrapper<Accounts>().eq(Accounts::getOwnerId, p.getUserId()).isNotNull(Accounts::getDeletedAt);
        var pg = accountsMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @GetMapping("/options") public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal p) {
        return ApiResponse.ok(accountService.options(p.getUserId()));
    }

    @PostMapping("/batch-delete") public ApiResponse<Integer> batchDelete(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, List<Long>> body) {
        int c = 0; for (Long id : body.getOrDefault("ids", List.of())) { accountService.delete(id); c++; } return ApiResponse.ok(c);
    }

    @PostMapping("/batch-update") public ApiResponse<Integer> batchUpdate(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Long> ids = (List<Long>) body.getOrDefault("ids", List.of());
        int c = 0; for (Long id : ids) { accountService.update(id, str(body,"name"), lng(body,"mcc_id"),
                lng(body,"agent_id"), lng(body,"status_id"), str(body,"timezone")); c++; }
        return ApiResponse.ok(c);
    }

    @PostMapping("/sync-from-sheet") public ApiResponse<SyncResult> syncFromSheet(
            @AuthenticationPrincipal UserPrincipal p, @RequestBody Map<String, Object> body) {
        String spreadsheetId = (String) body.get("spreadsheet_id");
        boolean dryRun = Boolean.TRUE.equals(body.get("dry_run"));
        if (spreadsheetId == null) return ApiResponse.fail("缺少spreadsheet_id");
        return ApiResponse.ok(accountService.syncFromSheet(p.getUserId(), spreadsheetId, dryRun));
    }

    @GetMapping("/lookup") public ApiResponse<Accounts> lookup(@RequestParam String accountId) {
        var a = accountsMapper.selectOne(new LambdaQueryWrapper<Accounts>().eq(Accounts::getAccountId, accountId));
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("不存在");
    }

    @GetMapping("/recharge-records") public ApiResponse<List<RechargeRecords>> rechargeRecords(@RequestParam String accountId) {
        return ApiResponse.ok(rechargeRecordsMapper.selectList(new LambdaQueryWrapper<RechargeRecords>()
                .eq(RechargeRecords::getAccountId, accountId).orderByDesc(RechargeRecords::getCreatedAt)));
    }

    @GetMapping("/mcc-history") public ApiResponse<List<AccountMccHistory>> mccHistory(@RequestParam Long accountId) {
        return ApiResponse.ok(mccHistoryMapper.selectList(new LambdaQueryWrapper<AccountMccHistory>()
                .eq(AccountMccHistory::getAccountId, accountId).orderByDesc(AccountMccHistory::getCreatedAt)));
    }

    @PostMapping("/batch-lookup") public ApiResponse<List<Accounts>> batchLookup(@RequestBody Map<String, List<String>> body) {
        return ApiResponse.ok(accountsMapper.selectList(new LambdaQueryWrapper<Accounts>()
                .in(Accounts::getAccountId, body.getOrDefault("account_ids", List.of()))));
    }

    private Long lng(Map<String,Object> m, String k) { Object v=m.get(k); return v!=null ? Long.valueOf(v.toString()) : null; }
    private String str(Map<String,Object> m, String k) { Object v=m.get(k); return v!=null ? v.toString() : null; }
}
