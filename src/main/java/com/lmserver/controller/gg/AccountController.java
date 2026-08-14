package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.AccountDto;
import com.lmserver.dto.response.AccountListResponse;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.dto.response.RechargeRecordDto;
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
import java.util.*;

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

    @GetMapping("/list") public AccountListResponse list(
            @AuthenticationPrincipal UserPrincipal p, @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int size, @RequestParam(required=false) String search,
            @RequestParam(required=false) String status, @RequestParam(name="mcc_id", required=false) Long mccId,
            @RequestParam(required=false) String agent) {
        return accountService.list(p.getUserId(), page, size, search, status, mccId, agent);
    }

    @GetMapping("/{id}") public ApiResponse<AccountDto> detail(@PathVariable Long id) {
        AccountDto a = accountService.detail(id); return a != null ? ApiResponse.ok(a) : ApiResponse.fail("不存在");
    }

    @PostMapping("/create") public ApiResponse<Accounts> create(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(accountService.create(p.getUserId(), (String)body.get("name"),
                (String)body.get("account_id"), lng(body,"mcc_id"), lng(body,"agent_id"),
                lng(body,"status_id"), (String)body.get("timezone")));
    }

    @PutMapping("/{id}") public ApiResponse<Accounts> update(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal p, @RequestBody Map<String, Object> body) {
        Accounts a = accountService.update(id, p.getUserId(),
                (String)body.get("name"), lng(body,"mcc_id"), lng(body,"agent_id"),
                lng(body,"status_id"), (String)body.get("timezone"));
        return a != null ? ApiResponse.ok(a) : ApiResponse.fail("不存在或无权限");
    }

    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal p) {
        accountService.delete(id, p.getUserId()); return ApiResponse.ok();
    }

    @PostMapping("/{id}/restore") public ApiResponse<Void> restore(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal p) {
        accountService.restore(id, p.getUserId()); return ApiResponse.ok();
    }

    @DeleteMapping("/{id}/permanent") public ApiResponse<Void> permanentDelete(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal p) {
        accountService.permanentDelete(id, p.getUserId()); return ApiResponse.ok();
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
        return ApiResponse.ok(accountService.batchDelete(body.getOrDefault("ids", List.of()), p.getUserId()));
    }

    @PostMapping("/batch-update") public ApiResponse<Integer> batchUpdate(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Long> ids = (List<Long>) body.getOrDefault("ids", List.of());
        return ApiResponse.ok(accountService.batchUpdate(ids, p.getUserId(), str(body,"name"),
                lng(body,"mcc_id"), lng(body,"agent_id"), lng(body,"status_id"), str(body,"timezone")));
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

    @GetMapping("/recharge-records") public ApiResponse<List<RechargeRecordDto>> rechargeRecords(@RequestParam String accountId) {
        return ApiResponse.ok(rechargeRecordsMapper.selectDtosByAccountId(accountId));
    }

    @GetMapping("/mcc-history") public ApiResponse<List<AccountMccHistory>> mccHistory(@RequestParam Long accountId) {
        return ApiResponse.ok(mccHistoryMapper.selectList(new LambdaQueryWrapper<AccountMccHistory>()
                .eq(AccountMccHistory::getAccountId, accountId).orderByDesc(AccountMccHistory::getCreatedAt)));
    }

    @PostMapping("/batch-lookup") public ApiResponse<List<Accounts>> batchLookup(@RequestBody Map<String, List<String>> body) {
        return ApiResponse.ok(accountsMapper.selectList(new LambdaQueryWrapper<Accounts>()
                .in(Accounts::getAccountId, body.getOrDefault("account_ids", List.of()))));
    }

    /** 批量创建 — 对齐 Python batch-create */
    @PostMapping("/batch-create")
    public ApiResponse<Map<String, Object>> batchCreate(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.getOrDefault("items", List.of());
        int created = 0;
        List<String> skipped = new ArrayList<>();
        for (var item : items) {
            try {
                accountService.create(p.getUserId(), str(item, "name"), str(item, "account_id"),
                        lng(item, "mcc_id"), resolveAgentId(str(item, "agent")), resolveStatusId(str(item, "status")),
                        str(item, "timezone"));
                created++;
            } catch (Exception e) {
                skipped.add(str(item, "account_id") + ": " + e.getMessage());
            }
        }
        return ApiResponse.ok(Map.of("created", created, "skipped", skipped));
    }

    /** 账户归属转移 — 对齐 Python reassign */
    @PutMapping("/{id}/reassign")
    public ApiResponse<Accounts> reassign(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        Accounts a = accountsMapper.selectById(id);
        if (a == null) return ApiResponse.fail("账户不存在");
        if (a.getOwnerId().equals(p.getUserId())) return ApiResponse.fail("账户已归属于你");
        // 记录 MCC 变更历史
        if (body.containsKey("mcc_id")) {
            AccountMccHistory h = new AccountMccHistory();
            h.setAccountId(id);
            h.setOldMccId(a.getMccId());
            h.setNewMccId(lng(body, "mcc_id"));
            h.setChangedBy(p.getUserId());
            h.setChangeType("reassign");
            h.setCreatedAt(LocalDateTime.now());
            mccHistoryMapper.insert(h);
        }
        a.setOwnerId(p.getUserId());
        if (body.containsKey("name")) a.setName(str(body, "name"));
        if (body.containsKey("mcc_id")) a.setMccId(lng(body, "mcc_id"));
        if (body.containsKey("agent_id")) a.setAgentId(lng(body, "agent_id"));
        if (body.containsKey("status_id")) a.setStatusId(lng(body, "status_id"));
        if (body.containsKey("timezone")) a.setTimezone(str(body, "timezone"));
        a.setUpdatedAt(LocalDateTime.now());
        accountsMapper.updateById(a);
        return ApiResponse.ok(a);
    }

    /** 删除单条 MCC 变更历史 */
    @DeleteMapping("/{aid}/mcc-history/{hid}")
    public ApiResponse<Void> deleteMccHistory(@PathVariable Long aid, @PathVariable Long hid) {
        AccountMccHistory h = mccHistoryMapper.selectById(hid);
        if (h != null && h.getAccountId().equals(aid)) mccHistoryMapper.deleteById(hid);
        return ApiResponse.ok();
    }

    // ── agent/status 文本名自动解析 ──

    @org.springframework.beans.factory.annotation.Autowired
    private com.lmserver.mapper.common.AgentsMapper agentsMapper;
    @org.springframework.beans.factory.annotation.Autowired
    private com.lmserver.mapper.common.AccountStatusesMapper statusesMapper;

    private Long resolveAgentId(String name) {
        if (name == null || name.isBlank()) return null;
        var existing = agentsMapper.selectList(
                new LambdaQueryWrapper<com.lmserver.entity.common.Agents>().eq(com.lmserver.entity.common.Agents::getName, name));
        if (!existing.isEmpty()) return existing.get(0).getId();
        com.lmserver.entity.common.Agents a = new com.lmserver.entity.common.Agents();
        a.setName(name);
        agentsMapper.insert(a);
        return a.getId();
    }

    private Long resolveStatusId(String name) {
        if (name == null || name.isBlank()) return null;
        var existing = statusesMapper.selectList(
                new LambdaQueryWrapper<com.lmserver.entity.common.AccountStatuses>().eq(com.lmserver.entity.common.AccountStatuses::getName, name));
        if (!existing.isEmpty()) return existing.get(0).getId();
        com.lmserver.entity.common.AccountStatuses s = new com.lmserver.entity.common.AccountStatuses();
        s.setName(name);
        s.setPlatform("gg");
        statusesMapper.insert(s);
        return s.getId();
    }

    private Long lng(Map<String,Object> m, String k) { Object v=m.get(k); return v!=null ? Long.valueOf(v.toString()) : null; }
    private String str(Map<String,Object> m, String k) { Object v=m.get(k); return v!=null ? v.toString() : null; }
}
