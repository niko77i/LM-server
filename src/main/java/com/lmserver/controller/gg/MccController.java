package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Accounts;
import com.lmserver.entity.gg.Mcc;
import com.lmserver.entity.gg.Products;
import com.lmserver.mapper.gg.AccountsMapper;
import com.lmserver.mapper.gg.MccMapper;
import com.lmserver.mapper.gg.ProductsMapper;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.MccService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/mcc")
@RequiredArgsConstructor
public class MccController {

    private final MccService mccService;
    @Autowired private MccMapper mccMapper;
    @Autowired private AccountsMapper accountsMapper;
    @Autowired private ProductsMapper productsMapper;

    @GetMapping("/list")
    public PagedResponse<Mcc> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search, @RequestParam(required = false) Long levelId) {
        return mccService.list(principal.getUserId(), page, size, search, levelId);
    }

    @GetMapping("/{id}")
    public ApiResponse<Mcc> detail(@PathVariable Long id) {
        Mcc m = mccService.getById(id);
        return m != null ? ApiResponse.ok(m) : ApiResponse.fail("MCC不存在");
    }

    @PostMapping("/create")
    public ApiResponse<Mcc> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String mccId = (String) body.get("mcc_id");
        if (name == null || name.isBlank() || mccId == null || mccId.isBlank())
            return ApiResponse.fail("名称和MCC ID不能为空");
        // 检查重复
        if (mccMapper.selectCount(new LambdaQueryWrapper<Mcc>().eq(Mcc::getMccId, mccId)) > 0)
            return ApiResponse.fail("MCC ID已存在");
        Long levelId = body.get("level_id") != null ? Long.valueOf(body.get("level_id").toString()) : null;
        Long parentId = body.get("parent_mcc_id") != null ? Long.valueOf(body.get("parent_mcc_id").toString()) : null;
        return ApiResponse.ok(mccService.create(principal.getUserId(), name, mccId, levelId, parentId));
    }

    @PutMapping("/{id}")
    public ApiResponse<Mcc> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Long levelId = body.get("level_id") != null ? Long.valueOf(body.get("level_id").toString()) : null;
        Long parentId = body.get("parent_mcc_id") != null ? Long.valueOf(body.get("parent_mcc_id").toString()) : null;
        // 循环引用检测
        if (parentId != null && hasCircularRef(id, parentId))
            return ApiResponse.fail("不能将上级MCC设为自己或子孙节点");
        Mcc m = mccService.update(id, name, levelId, parentId);
        return m != null ? ApiResponse.ok(m) : ApiResponse.fail("MCC不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // 检查关联
        Long childCount = mccMapper.selectCount(new LambdaQueryWrapper<Mcc>().eq(Mcc::getParentMccId, id));
        if (childCount > 0) return ApiResponse.fail("该MCC下还有" + childCount + "个子MCC，请先删除子MCC");
        Long acctCount = accountsMapper.selectCount(new LambdaQueryWrapper<Accounts>()
                .eq(Accounts::getMccId, id).isNull(Accounts::getDeletedAt));
        if (acctCount > 0) return ApiResponse.fail("该MCC下还有" + acctCount + "个账户，请先迁移账户");
        Long prodCount = productsMapper.selectCount(new LambdaQueryWrapper<Products>()
                .eq(Products::getMccId, id).isNull(Products::getDeletedAt));
        if (prodCount > 0) return ApiResponse.fail("该MCC下还有" + prodCount + "个产品，请先迁移产品");
        mccService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Object>> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.getOrDefault("ids", List.of());
        int deleted = 0;
        List<Map<String, Object>> skipped = new ArrayList<>();
        for (Long id : ids) {
            Long childCount = mccMapper.selectCount(new LambdaQueryWrapper<Mcc>().eq(Mcc::getParentMccId, id));
            Long acctCount = accountsMapper.selectCount(new LambdaQueryWrapper<Accounts>()
                    .eq(Accounts::getMccId, id).isNull(Accounts::getDeletedAt));
            if (childCount > 0 || acctCount > 0) {
                skipped.add(Map.of("id", id, "reason", "还有" + childCount + "个子MCC/" + acctCount + "个账户"));
                continue;
            }
            mccService.delete(id);
            deleted++;
        }
        return ApiResponse.ok(Map.of("deleted", deleted, "skipped", skipped));
    }

    @GetMapping("/options")
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(mccService.options(principal.getUserId()));
    }

    /** 将 MCC 关联到上游链路 */
    @PostMapping("/{mid}/link")
    public ApiResponse<Mcc> link(@PathVariable Long mid, @RequestBody Map<String, Object> body) {
        Long parentId = body.get("parent_mcc_id") != null ? Long.valueOf(body.get("parent_mcc_id").toString()) : null;
        if (parentId != null && hasCircularRef(mid, parentId))
            return ApiResponse.fail("不能形成循环引用");
        Mcc m = mccService.getById(mid);
        if (m == null) return ApiResponse.fail("MCC不存在");
        m.setParentMccId(parentId);
        mccMapper.updateById(m);
        return ApiResponse.ok(m);
    }

    // ── 辅助 ──

    private boolean hasCircularRef(Long nodeId, Long proposedParentId) {
        if (proposedParentId.equals(nodeId)) return true;
        Set<Long> visited = new HashSet<>();
        Long current = proposedParentId;
        while (current != null && visited.add(current)) {
            if (current.equals(nodeId)) return true;
            Mcc parent = mccMapper.selectById(current);
            if (parent == null) break;
            current = parent.getParentMccId();
        }
        return false;
    }
}
