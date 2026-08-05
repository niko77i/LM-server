package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.*;
import com.lmserver.mapper.gg.*;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DelistController {

    private final DelistChecksMapper mapper;
    @Autowired private DelistNotificationsMapper notifMapper;
    @Autowired private PackagesMapper packagesMapper;
    @Autowired private ProductsMapper productsMapper;

    // ═══════ 查询 ═══════

    @GetMapping("/api/delist/checks")
    public PagedResponse<DelistChecks> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) Long productId) {
        var qw = new LambdaQueryWrapper<DelistChecks>();
        if (productId != null) qw.eq(DelistChecks::getProductId, productId);
        qw.orderByDesc(DelistChecks::getCheckedAt);
        var pg = mapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @GetMapping("/api/delist/product/{productId}")
    public ApiResponse<?> getByProduct(@PathVariable Long productId) {
        return ApiResponse.ok(mapper.selectList(
                new LambdaQueryWrapper<DelistChecks>().eq(DelistChecks::getProductId, productId)));
    }

    @GetMapping("/api/delist/pending")
    public ApiResponse<?> pending(@AuthenticationPrincipal UserPrincipal p) {
        var list = notifMapper.selectList(
                new LambdaQueryWrapper<DelistNotifications>()
                        .eq(DelistNotifications::getUserId, p.getUserId())
                        .eq(DelistNotifications::getFirstNotified, 0));
        return ApiResponse.ok(list);
    }

    @PostMapping("/api/delist/dismiss")
    public ApiResponse<Void> dismiss(@AuthenticationPrincipal UserPrincipal p, @RequestBody Map<String, Long> body) {
        Long pkgId = body.get("package_id");
        var n = notifMapper.selectOne(new LambdaQueryWrapper<DelistNotifications>()
                .eq(DelistNotifications::getPackageId, pkgId).eq(DelistNotifications::getUserId, p.getUserId()));
        if (n != null) { n.setDismissedAt(LocalDateTime.now()); notifMapper.updateById(n); }
        return ApiResponse.ok();
    }

    // ═══════ 执行掉包检测 ═══════

    @PostMapping("/api/products/{pid}/check-delist")
    public ApiResponse<Map<String, Object>> checkDelist(@PathVariable Long pid,
            @AuthenticationPrincipal UserPrincipal p) {
        Products prod = productsMapper.selectById(pid);
        if (prod == null) return ApiResponse.fail("产品不存在");

        var pkgs = packagesMapper.selectList(
                new LambdaQueryWrapper<Packages>().eq(Packages::getProductId, pid));
        if (pkgs.isEmpty()) return ApiResponse.fail("该产品下无包");

        int delisted = 0, checked = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Packages pkg : pkgs) {
            checked++;
            boolean isDelisted = checkGooglePlay(pkg.getPackageName());
            DelistChecks dc = mapper.selectOne(
                    new LambdaQueryWrapper<DelistChecks>().eq(DelistChecks::getPackageId, pkg.getId()));
            if (dc == null) {
                dc = new DelistChecks(); dc.setPackageId(pkg.getId()); dc.setProductId(pid);
            }
            dc.setIsDelisted(isDelisted ? 1L : 0L);
            dc.setCheckedAt(LocalDateTime.now());
            if (dc.getId() == null) mapper.insert(dc); else mapper.updateById(dc);

            if (isDelisted) {
                delisted++;
                notifyDelist(pkg, prod, p.getUserId());
            }
            results.add(Map.of("package", pkg.getPackageName(), "delisted", isDelisted));
        }
        return ApiResponse.ok(Map.of("checked", checked, "delisted", delisted, "results", results));
    }

    /** 通过 Google Play 页面检测包是否下架 */
    private boolean checkGooglePlay(String packageName) {
        try {
            String url = "https://play.google.com/store/apps/details?id=" + packageName;
            var doc = Jsoup.connect(url).timeout(10000).userAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36").get();
            String text = doc.text().toLowerCase();
            return text.contains("not found") || text.contains("not available")
                    || text.contains("not exist") || doc.title().toLowerCase().contains("not found");
        } catch (org.jsoup.HttpStatusException e) {
            return e.getStatusCode() == 404;
        } catch (Exception e) {
            log.warn("检测失败 {}: {}", packageName, e.getMessage());
            return false;
        }
    }

    /** 发送掉包通知（写入 delist_notifications 表 + 日志输出） */
    private void notifyDelist(Packages pkg, Products prod, Long userId) {
        var existing = notifMapper.selectOne(new LambdaQueryWrapper<DelistNotifications>()
                .eq(DelistNotifications::getPackageId, pkg.getId())
                .eq(DelistNotifications::getUserId, userId));
        if (existing == null) {
            DelistNotifications dn = new DelistNotifications();
            dn.setPackageId(pkg.getId()); dn.setUserId(userId);
            dn.setFirstNotified(0L); dn.setReminderCount(0L);
            notifMapper.insert(dn);
            log.info("掉包通知: 产品={} 包={}", prod.getProductName(), pkg.getPackageName());
        }
    }
}
