package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.ScrapeCache;
import com.lmserver.mapper.gg.ScrapeCacheMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图片抓取控制器 - Google Play 截图抓取与缓存。
 * 路由前缀: /api/scrape
 */
/**
 * 图片抓取控制器 — /api/scrape/*，Google Play截图抓取缓存管理
 */

@RestController
@RequestMapping("/api/scrape")
@RequiredArgsConstructor
public class ScrapeController {

    private final ScrapeCacheMapper scrapeCacheMapper;

    /**
 * GG 图片抓取控制器 — /api/scrape*
 */
    @GetMapping("/cache")
    public ApiResponse<List<ScrapeCache>> listCache() {
        return ApiResponse.ok(scrapeCacheMapper.selectList(null));
    }
    @GetMapping("/cache/{packageName}")
    /** 按包名查询缓存 */
    public ApiResponse<ScrapeCache> getCache(@PathVariable String packageName) {
        ScrapeCache cache = scrapeCacheMapper.selectById(packageName);
        return cache != null ? ApiResponse.ok(cache) : ApiResponse.fail("无缓存");
    }
    @DeleteMapping("/cache/{packageName}")
    /** 清除指定包名的缓存 */
    public ApiResponse<Void> clearCache(@PathVariable String packageName,
            @AuthenticationPrincipal UserPrincipal principal) {
        scrapeCacheMapper.deleteById(packageName);
        return ApiResponse.ok();
    }
}
