package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.fb.FbAccountBm;
import com.lmserver.mapper.fb.FbAccountBmMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FB 账户-BM 关联管理 — /api/fb/account-bm/*。
 */
@RestController
@RequestMapping("/api/fb/account-bm")
@RequiredArgsConstructor
public class FbAccountBmController {

    private final FbAccountBmMapper mapper;

    @GetMapping("/list")
    public ApiResponse<List<FbAccountBm>> list(@RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long bmId) {
        var qw = new LambdaQueryWrapper<FbAccountBm>();
        if (accountId != null) qw.eq(FbAccountBm::getAccountId, accountId);
        if (bmId != null) qw.eq(FbAccountBm::getBmId, bmId);
        return ApiResponse.ok(mapper.selectList(qw));
    }

    @PostMapping("/associate")
    public ApiResponse<FbAccountBm> associate(@RequestBody Map<String, Long> body) {
        FbAccountBm ab = new FbAccountBm();
        ab.setAccountId(body.get("account_id")); ab.setBmId(body.get("bm_id"));
        mapper.insert(ab);
        return ApiResponse.ok(ab);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> dissociate(@PathVariable Long id) { mapper.deleteById(id); return ApiResponse.ok(); }
}
