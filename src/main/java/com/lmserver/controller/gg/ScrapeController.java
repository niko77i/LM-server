package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.ScrapeCache;
import com.lmserver.mapper.gg.ScrapeCacheMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 图片抓取控制器 — 对齐 Python main.py scrape 段。
 *
 * <h3>端点</h3>
 * <ul>
 * <li>GET /api/scrape/cache              — 全量缓存列表</li>
 * <li>GET /api/scrape/cache/{packageName} — 按包名查缓存</li>
 * <li>DELETE /api/scrape/cache/{packageName} — 清除缓存+文件</li>
 * <li>POST /api/scrape/trigger             — 触发抓取：从Google Play页面提取截图URL+Logo → 下载PNG → 写缓存</li>
 * <li>GET /api/scrape/download?packageName= — 下载已抓取的截图文件</li>
 * </ul>
 *
 * <h3>抓取流程</h3>
 * 1. Jsoup 访问 Google Play 页面<br>
 * 2. 解析 img 标签提取截图URL（含 screen/screenshot 关键字）<br>
 * 3. 提取 Logo URL（含 icon/logo 关键字）<br>
 * 4. 逐个下载截图（最多10张）+ Logo<br>
 * 5. 写入 scrape_cache 表 + 本地 scrape_images/ 目录
 */
@Slf4j
@RestController
@RequestMapping("/api/scrape")
@RequiredArgsConstructor
public class ScrapeController {

    private final ScrapeCacheMapper scrapeCacheMapper;
    private static final Path SCRAPE_DIR = Paths.get("scrape_images");

    @GetMapping("/cache")
    public ApiResponse<List<ScrapeCache>> listCache() {
        return ApiResponse.ok(scrapeCacheMapper.selectList(null));
    }

    @GetMapping("/cache/{packageName}")
    public ApiResponse<ScrapeCache> getCache(@PathVariable String packageName) {
        ScrapeCache c = scrapeCacheMapper.selectById(packageName);
        return c != null ? ApiResponse.ok(c) : ApiResponse.fail("无缓存");
    }

    @DeleteMapping("/cache/{packageName}")
    public ApiResponse<Void> clearCache(@PathVariable String packageName,
            @AuthenticationPrincipal UserPrincipal principal) {
        scrapeCacheMapper.deleteById(packageName);
        try { Files.deleteIfExists(SCRAPE_DIR.resolve(packageName)); } catch (Exception ignored) {}
        return ApiResponse.ok();
    }

    /** 抓取 Google Play 截图和 Logo */
    @PostMapping("/trigger")
    public ApiResponse<Map<String, Object>> triggerScrape(@RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        String url = body.get("url");
        if (url == null || url.isBlank()) return ApiResponse.fail("URL不能为空");

        String pkgName = body.getOrDefault("package_name", extractPkg(url));
        if (pkgName.isBlank()) pkgName = url.replaceAll(".*id=", "").replaceAll("&.*", "");
        if (pkgName.isBlank()) return ApiResponse.fail("无法提取包名");

        try {
            Files.createDirectories(SCRAPE_DIR);
            Path pkgDir = SCRAPE_DIR.resolve(pkgName);
            Files.createDirectories(pkgDir);

            Document doc = Jsoup.connect(url)
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            // 提取截图 URL
            List<String> screenshots = new ArrayList<>();
            for (Element img : doc.select("img[src]")) {
                String src = img.attr("src");
                if (src.contains("play.google.com") && (src.contains("screen") || src.contains("screenshot")))
                    screenshots.add(src.startsWith("http") ? src : "https:" + src);
            }

            // 提取 Logo
            String logoUrl = null;
            for (Element img : doc.select("img[src*='icon'],img[src*='logo'],img[alt*='icon']")) {
                String src = img.attr("src");
                if (src.contains("play.google.com")) {
                    logoUrl = src.startsWith("http") ? src : "https:" + src;
                    break;
                }
            }

            // 下载截图
            int downloaded = 0;
            for (int i = 0; i < Math.min(screenshots.size(), 10); i++) {
                try {
                    downloadImage(screenshots.get(i), pkgDir.resolve("screenshot_" + (i + 1) + ".png"));
                    downloaded++;
                } catch (Exception e) { log.warn("下载截图失败 {}: {}", i, e.getMessage()); }
            }

            // 下载 Logo
            if (logoUrl != null) {
                try { downloadImage(logoUrl, pkgDir.resolve("logo.png")); } catch (Exception ignored) {}
            }

            // 更新缓存
            ScrapeCache cache = scrapeCacheMapper.selectById(pkgName);
            if (cache == null) { cache = new ScrapeCache(); cache.setPackageName(pkgName); }
            cache.setImageCount((long) downloaded);
            cache.setSavedPath(pkgDir.toString());
            if (logoUrl != null) cache.setLogoPath(pkgDir.resolve("logo.png").toString());
            cache.setLastScraped(LocalDateTime.now());
            cache.setScrapedBy(principal.getUserId());
            if (scrapeCacheMapper.selectById(pkgName) != null) scrapeCacheMapper.updateById(cache);
            else scrapeCacheMapper.insert(cache);

            return ApiResponse.ok(Map.of("package_name", pkgName, "screenshots", downloaded,
                    "logo", logoUrl != null, "saved_path", pkgDir.toString()));
        } catch (Exception e) {
            log.error("抓取失败: {}", e.getMessage());
            return ApiResponse.fail("抓取失败: " + e.getMessage());
        }
    }

    /** 下载图片到指定路径 */
    private void downloadImage(String url, Path dest) throws Exception {
        BufferedImage img = ImageIO.read(new URL(url));
        if (img != null) ImageIO.write(img, "png", dest.toFile());
    }

    private String extractPkg(String url) {
        int idx = url.indexOf("id=");
        if (idx < 0) return "";
        String after = url.substring(idx + 3);
        int end = after.indexOf("&");
        return end > 0 ? after.substring(0, end) : after;
    }

    /** 下载已抓取的图片 */
    @GetMapping("/download")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.FileSystemResource> download(
            @RequestParam String packageName) {
        Path dir = SCRAPE_DIR.resolve(packageName);
        if (!Files.isDirectory(dir)) return org.springframework.http.ResponseEntity.notFound().build();
        // 返回第一个截图
        for (int i = 1; i <= 10; i++) {
            Path f = dir.resolve("screenshot_" + i + ".png");
            if (Files.isRegularFile(f))
                return org.springframework.http.ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=screenshot_" + i + ".png")
                        .body(new org.springframework.core.io.FileSystemResource(f));
        }
        return org.springframework.http.ResponseEntity.notFound().build();
    }
}
