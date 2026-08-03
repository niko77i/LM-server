package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.AccountMccHistory;
import com.lmserver.mapper.gg.AccountMccHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账户 MCC 变更历史 — /api/account-mcc-history/*。
 */
@RestController
@RequestMapping("/api/account-mcc-history")
@RequiredArgsConstructor
public class AccountMccHistoryController {

    private final AccountMccHistoryMapper mapper;

    @GetMapping("/list")
    public ApiResponse<List<AccountMccHistory>> list(@RequestParam Long accountId) {
        return ApiResponse.ok(mapper.selectList(
                new LambdaQueryWrapper<AccountMccHistory>().eq(AccountMccHistory::getAccountId, accountId)
                        .orderByDesc(AccountMccHistory::getCreatedAt)));
    }
}
