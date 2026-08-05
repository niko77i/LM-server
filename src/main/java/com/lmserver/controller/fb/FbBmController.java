package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.*;
import com.lmserver.mapper.fb.*;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.FbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * FB BM 管理控制器 — 对齐 Python fb_routes.py ban_and_migrate + unified。
 */
@Slf4j
@RestController
@RequestMapping("/api/fb/bms")
@RequiredArgsConstructor
public class FbBmController {

    private final FbService fbService;

    @Autowired private FbAccountBmMapper accountBmMapper;
    @Autowired private FbAccountBmHistoryMapper accountBmHistoryMapper;
    @Autowired private FbProductBmsMapper productBmsMapper;
    @Autowired private FbPixelBmsMapper pixelBmsMapper;
    @Autowired private FbBmsMapper bmsMapper;
    @Autowired private FbPixelsMapper pixelsMapper;

    @GetMapping("/list")
    public PagedResponse<FbBms> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search, @RequestParam(required = false) String status) {
        return fbService.listBms(principal.getUserId(), page, size, search, status);
    }

    @GetMapping("/{id}")
    public ApiResponse<FbBms> detail(@PathVariable Long id) {
        FbBms b = fbService.getBmById(id);
        return b != null ? ApiResponse.ok(b) : ApiResponse.fail("BM不存在");
    }

    @PostMapping("/create")
    public ApiResponse<FbBms> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        String name = body.get("name"), bmId = body.get("bm_id");
        if (name == null || bmId == null) return ApiResponse.fail("名称和BM ID不能为空");
        if (!bmId.matches("\\d+")) return ApiResponse.fail("BMID必须是纯数字");
        return ApiResponse.ok(fbService.createBm(principal.getUserId(), name, bmId, body.get("note")));
    }

    @PutMapping("/{id}")
    public ApiResponse<FbBms> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FbBms b = fbService.updateBm(id, body.get("name"), body.get("note"));
        return b != null ? ApiResponse.ok(b) : ApiResponse.fail("BM不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fbService.deleteBm(id);
        return ApiResponse.ok();
    }

    @GetMapping("/options")
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(fbService.bmOptions(principal.getUserId()));
    }

    /** 统一列表 — UNION fb_bms + fb_pixel_bms，对齐 Python unified */
    @GetMapping("/unified")
    public PagedResponse<Map<String, Object>> unified(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search, @RequestParam(required = false) String status,
            @RequestParam(required = false) String bmType) {
        Long uid = principal.getUserId();

        // fb_bms
        var bmQw = new LambdaQueryWrapper<FbBms>().eq(FbBms::getOwnerId, uid);
        if (status != null) bmQw.eq(FbBms::getStatus, status);
        if (search != null && !search.isBlank())
            bmQw.and(w -> w.like(FbBms::getName, search).or().like(FbBms::getBmId, search));

        // fb_pixel_bms
        var pxQw = new LambdaQueryWrapper<FbPixelBms>().eq(FbPixelBms::getOwnerId, uid);
        if (status != null) pxQw.eq(FbPixelBms::getStatus, status);
        if (search != null && !search.isBlank())
            pxQw.and(w -> w.like(FbPixelBms::getName, search).or().like(FbPixelBms::getBmId, search));

        List<Map<String, Object>> all = new ArrayList<>();

        // "pixel_bm" 类型筛选只查像素 BM
        if (bmType == null || !"pixel_bm".equals(bmType)) {
            for (FbBms bm : bmsMapper.selectList(bmQw)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", bm.getId());
                item.put("name", bm.getName());
                item.put("bm_id", bm.getBmId());
                item.put("status", bm.getStatus());
                item.put("owner_id", bm.getOwnerId());
                item.put("bm_type", "normal");
                // account_count
                item.put("account_count", accountBmMapper.selectCount(
                        new LambdaQueryWrapper<FbAccountBm>().eq(FbAccountBm::getBmId, bm.getId())));
                item.put("pixel_count", 0);
                all.add(item);
            }
        }

        // "normal" 类型筛选只查普通 BM
        if (bmType == null || !"normal".equals(bmType)) {
            for (FbPixelBms px : pixelBmsMapper.selectList(pxQw)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", px.getId());
                item.put("name", px.getName());
                item.put("bm_id", px.getBmId());
                item.put("status", px.getStatus());
                item.put("owner_id", px.getOwnerId());
                item.put("bm_type", "pixel_bm");
                item.put("account_count", 0);
                item.put("pixel_count", pixelsMapper.selectCount(
                        new LambdaQueryWrapper<FbPixels>().eq(FbPixels::getPixelBmId, px.getId())));
                all.add(item);
            }
        }

        // 手动分页
        int total = all.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<Map<String, Object>> items = from < total ? all.subList(from, to) : List.of();
        return PagedResponse.of(items, total, page, size);
    }

    /** 封禁 BM 并迁移账户到目标 BM — 对齐 Python ban_and_migrate */
    @PostMapping("/{bid}/ban-and-migrate")
    public ApiResponse<Map<String, Object>> banAndMigrate(@PathVariable Long bid,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        Long uid = principal.getUserId();
        String targetBmIdStr = body.getOrDefault("target_bm_id", "").trim();
        String targetBmName = body.getOrDefault("target_bm_name", "").trim();

        FbBms bm = bmsMapper.selectById(bid);
        if (bm == null) return ApiResponse.fail("BM不存在");
        if ("banned".equals(bm.getStatus())) return ApiResponse.fail("该BM已被封禁");

        // 查找或创建目标 BM
        FbBms target = bmsMapper.selectOne(
                new LambdaQueryWrapper<FbBms>().eq(FbBms::getBmId, targetBmIdStr));
        Long targetBid;
        if (target != null) {
            if (target.getId().equals(bid)) return ApiResponse.fail("不能迁移到自身");
            targetBid = target.getId();
        } else {
            if (targetBmName.isEmpty()) return ApiResponse.fail("新建BM需要提供名称");
            if (!targetBmIdStr.matches("\\d+")) return ApiResponse.fail("BMID必须是纯数字");
            FbBms newBm = new FbBms();
            newBm.setName(targetBmName);
            newBm.setBmId(targetBmIdStr);
            newBm.setOwnerId(uid);
            newBm.setStatus("normal");
            newBm.setCreatedAt(LocalDateTime.now());
            bmsMapper.insert(newBm);
            targetBid = newBm.getId();
        }

        // 迁移账户关联
        List<FbAccountBm> accounts = accountBmMapper.selectList(
                new LambdaQueryWrapper<FbAccountBm>().eq(FbAccountBm::getBmId, bid));
        for (FbAccountBm acc : accounts) {
            // INSERT OR IGNORE 到目标 BM
            if (accountBmMapper.selectCount(new LambdaQueryWrapper<FbAccountBm>()
                    .eq(FbAccountBm::getAccountId, acc.getAccountId())
                    .eq(FbAccountBm::getBmId, targetBid)) == 0) {
                FbAccountBm newLink = new FbAccountBm();
                newLink.setAccountId(acc.getAccountId());
                newLink.setBmId(targetBid);
                newLink.setCreatedAt(LocalDateTime.now());
                accountBmMapper.insert(newLink);
            }
            // DELETE 原关联
            accountBmMapper.deleteById(acc.getId());
            // 写历史
            FbAccountBmHistory hist = new FbAccountBmHistory();
            hist.setAccountId(acc.getAccountId());
            hist.setOldBmId(bid);
            hist.setNewBmId(targetBid);
            hist.setChangedBy(uid);
            hist.setChangeType("banned_migration");
            hist.setCreatedAt(LocalDateTime.now());
            accountBmHistoryMapper.insert(hist);
        }

        // 标记封禁
        bm.setStatus("banned");
        bm.setUpdatedAt(LocalDateTime.now());
        bmsMapper.updateById(bm);

        // 检查产品-BM 关联
        List<String> warnings = new ArrayList<>();
        var productLinks = productBmsMapper.selectList(
                new LambdaQueryWrapper<FbProductBms>().eq(FbProductBms::getBmId, bid));
        for (var pl : productLinks) {
            warnings.add("产品ID「" + pl.getProductId() + "」仍关联此BM，请手动更新产品的在跑BM");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("migrated_accounts", accounts.size());
        result.put("target_bm_id", targetBid);
        result.put("warnings", warnings);
        return ApiResponse.ok(result);
    }
}
