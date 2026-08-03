package com.lmserver.controller.admin;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.mapper.common.*;
import com.lmserver.mapper.gg.*;
import com.lmserver.mapper.fb.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员数据管理 — /api/admin/data/*。仅 ADMIN/DEVELOPER 角色。
 */
@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DEVELOPER','ADMIN')")
public class AdminDataController {

    private final AccountsMapper accountsMapper;
    private final ProductsMapper productsMapper;
    private final VideosMapper videosMapper;
    private final UsersMapper usersMapper;
    private final FbBmsMapper fbBmsMapper;
    private final FbAccountsMapper fbAccountsMapper;

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(Map.of(
            "accounts", accountsMapper.selectCount(null),
            "products", productsMapper.selectCount(null),
            "videos", videosMapper.selectCount(null),
            "users", usersMapper.selectCount(null),
            "fbBms", fbBmsMapper.selectCount(null),
            "fbAccounts", fbAccountsMapper.selectCount(null)
        ));
    }

    @PostMapping("/import")
    public ApiResponse<String> importData(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok("导入已触发"); // TODO
    }
}
