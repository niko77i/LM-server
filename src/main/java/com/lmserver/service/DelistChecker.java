package com.lmserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.entity.gg.DelistChecks;
import com.lmserver.entity.gg.Packages;
import com.lmserver.entity.gg.Products;
import com.lmserver.mapper.gg.DelistChecksMapper;
import com.lmserver.mapper.gg.PackagesMapper;
import com.lmserver.mapper.gg.ProductsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 掉包检测服务 — 每小时遍历活跃产品的包，Jsoup 检查 Google Play 是否下架。
 * 发现掉包后通知邮件+Telegram。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DelistChecker {

    private final ProductsMapper productsMapper;
    private final PackagesMapper packagesMapper;
    private final DelistChecksMapper delistChecksMapper;
    private final NotificationService notificationService;

    /** 检查所有活跃产品的包是否掉包 */
    public void checkAll() {
        List<Products> activeProducts = productsMapper.selectList(
                new LambdaQueryWrapper<Products>().eq(Products::getStatus, "active").eq(Products::getIsArchived, 0L));
        int delistedCount = 0;
        for (Products p : activeProducts) {
            List<Packages> pkgs = packagesMapper.selectList(
                    new LambdaQueryWrapper<Packages>().eq(Packages::getProductId, p.getId()));
            for (Packages pkg : pkgs) {
                if (pkg.getUrl() == null || pkg.getUrl().isBlank()) continue;
                boolean delisted = checkGooglePlay(pkg.getUrl());
                DelistChecks dc = new DelistChecks();
                dc.setPackageId(pkg.getId()); dc.setProductId(p.getId());
                dc.setIsDelisted(delisted ? 1L : 0L); dc.setCheckedAt(LocalDateTime.now());
                delistChecksMapper.insert(dc);
                if (delisted) {
                    delistedCount++;
                    log.warn("[掉包检测] 产品{} 包{} 已下架! URL: {}", p.getProductName(), pkg.getPackageName(), pkg.getUrl());
                    notificationService.sendEmail("2350574164@qq.com",
                            "[LM-Server] 掉包告警 - " + p.getProductName(),
                            "产品: " + p.getProductName() + "\n包: " + pkg.getPackageName() + "\nURL: " + pkg.getUrl());
                    notificationService.sendTelegram("<b>掉包告警</b>\n产品: " + p.getProductName() + "\n包: " + pkg.getPackageName());
                }
            }
        }
        log.info("[定时任务] 掉包检测完成: 检查{}个产品, 发现{}个掉包", activeProducts.size(), delistedCount);
    }

    /** Jsoup 检查 Google Play 页面是否返回404或"未找到" */
    private boolean checkGooglePlay(String url) {
        try {
            var doc = Jsoup.connect(url).timeout(15000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36").get();
            String text = doc.text();
            return text.contains("未找到") || text.contains("Not Found") || text.contains("找不到");
        } catch (Exception e) {
            log.debug("Jsoup请求失败: {} - {}", url, e.getMessage());
            return false; // 网络错误不算掉包
        }
    }
}
