package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.Tags;
import com.lmserver.entity.gg.VideoConsumption;
import com.lmserver.entity.gg.Videos;
import com.lmserver.mapper.common.TagsMapper;
import com.lmserver.mapper.gg.*;
import com.lmserver.entity.gg.ProductAssets;
import com.lmserver.entity.gg.Products;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import com.lmserver.mapper.gg.ProductAssetsMapper;
import com.lmserver.entity.gg.ProductAssets;
import com.lmserver.entity.gg.Products;

/**
 * YouTube 视频管理控制器 — /api/youtube/*，视频CRUD/批量导入/消耗追踪/标签配置。
 */
@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class YoutubeController {

    private final VideosMapper videosMapper;
    private final VideoConsumptionMapper consumptionMapper;
    private final TagsMapper tagsMapper;
    @Autowired private ProductAssetsMapper productAssetsMapper;
    @Autowired private com.lmserver.mapper.gg.ProductsMapper productsMapper;

        @GetMapping("/list")
    public PagedResponse<Videos> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String frameType,
            @RequestParam(required = false) String effectiveness,
            @RequestParam(required = false) String reviewStatus) {
        var qw = new LambdaQueryWrapper<Videos>().eq(Videos::getOwnerId, principal.getUserId());
        if (region != null && !region.isBlank()) qw.eq(Videos::getRegion, region);
        if (frameType != null && !frameType.isBlank()) qw.eq(Videos::getFrameType, frameType);
        if (effectiveness != null && !effectiveness.isBlank()) qw.eq(Videos::getEffectiveness, effectiveness);
        if (reviewStatus != null && !reviewStatus.isBlank()) qw.eq(Videos::getReviewStatus, reviewStatus);
        qw.orderByDesc(Videos::getImportedAt);
        var pg = videosMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

        @GetMapping("/{id}")
    public ApiResponse<Videos> detail(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        var v = videosMapper.selectList(new LambdaQueryWrapper<Videos>()
                .eq(Videos::getId, id).eq(Videos::getOwnerId, principal.getUserId()));
        return !v.isEmpty() ? ApiResponse.ok(v.get(0)) : ApiResponse.fail("视频不存在");
    }

        @PutMapping("/{id}")
    public ApiResponse<Videos> update(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        var list = videosMapper.selectList(new LambdaQueryWrapper<Videos>()
                .eq(Videos::getId, id).eq(Videos::getOwnerId, principal.getUserId()));
        if (list.isEmpty()) return ApiResponse.fail("视频不存在");
        Videos v = list.get(0);
        if (body.containsKey("title")) v.setTitle(body.get("title"));
        if (body.containsKey("region")) v.setRegion(body.get("region"));
        if (body.containsKey("frame_type")) v.setFrameType(body.get("frame_type"));
        if (body.containsKey("effectiveness")) v.setEffectiveness(body.get("effectiveness"));
        if (body.containsKey("review_status")) v.setReviewStatus(body.get("review_status"));
        if (body.containsKey("product_name")) v.setProductName(body.get("product_name"));
        if (body.containsKey("is_public")) v.setIsPublic("1".equals(body.get("is_public")) || "true".equalsIgnoreCase(body.get("is_public")));
        videosMapper.updateById(v);
        return ApiResponse.ok(v);
    }

        @PostMapping("/import")
    public ApiResponse<Integer> importVideos(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody List<Map<String, String>> items) {
        int count = 0;
        for (Map<String, String> item : items) {
            Videos v = new Videos();
            v.setId(item.get("id"));
            v.setOwnerId(principal.getUserId());
            v.setUrl(item.get("url"));
            v.setTitle(item.get("title"));
            v.setRegion(item.getOrDefault("region", "通用"));
            v.setFrameType(item.getOrDefault("frame_type", "非融帧"));
            v.setProductName(item.getOrDefault("product_name", ""));
            v.setChannelName(item.getOrDefault("channel_name", ""));
            v.setReviewStatus(item.getOrDefault("review_status", "能过审"));
            v.setIsPublic(false);
            v.setImportedAt(java.time.LocalDateTime.now());
            try { videosMapper.insert(v); count++; } catch (Exception ignored) {}
        }
        return ApiResponse.ok(count);
    }

        @PostMapping("/consumption")
    public ApiResponse<VideoConsumption> addConsumption(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        VideoConsumption vc = new VideoConsumption();
        vc.setVideoId((String) body.get("video_id"));
        vc.setVideoOwnerId(principal.getUserId());
        vc.setUserId(principal.getUserId());
        vc.setAmount(Double.valueOf(body.get("amount").toString()));
        vc.setConsumeDate(java.time.LocalDateTime.now());
        if (body.get("product_id") != null) vc.setProductId(Long.valueOf(body.get("product_id").toString()));
        consumptionMapper.insert(vc);
        return ApiResponse.ok(vc);
    }

        @GetMapping("/consumption/list")
    public PagedResponse<VideoConsumption> consumptionList(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String videoId) {
        var qw = new LambdaQueryWrapper<VideoConsumption>().eq(VideoConsumption::getUserId, principal.getUserId());
        if (videoId != null && !videoId.isBlank()) qw.eq(VideoConsumption::getVideoId, videoId);
        qw.orderByDesc(VideoConsumption::getConsumeDate);
        var pg = consumptionMapper.selectPage(new Page<>(page, size), qw);
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @DeleteMapping("/consumption/{id}")
    public ApiResponse<Void> deleteConsumption(@PathVariable Long id) { consumptionMapper.deleteById(id); return ApiResponse.ok(); }

        @GetMapping("/tags")
    public ApiResponse<List<Tags>> getTags() {
        return ApiResponse.ok(tagsMapper.selectList(null));
    }

        @PutMapping("/tags/{key}")
    public ApiResponse<Void> saveTag(@PathVariable String key, @RequestBody Map<String, String> body) {
        Tags t = tagsMapper.selectById(key);
        if (t == null) { t = new Tags(); t.setKey(key); t.setValue(body.get("value")); tagsMapper.insert(t); }
        else { t.setValue(body.get("value")); tagsMapper.updateById(t); }
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVideo(@PathVariable String id,
            @AuthenticationPrincipal UserPrincipal principal) {
        videosMapper.delete(new LambdaQueryWrapper<Videos>()
                .eq(Videos::getId, id).eq(Videos::getOwnerId, principal.getUserId()));
        return ApiResponse.ok();
    }

    @GetMapping("/export")
    public void export(@AuthenticationPrincipal UserPrincipal principal,
            jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException {
        var list = videosMapper.selectList(
                new LambdaQueryWrapper<Videos>().eq(Videos::getOwnerId, principal.getUserId()));
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=youtube-videos.csv");
        var w = resp.getWriter();
        w.write("ID,标题,URL,地区,频道,融帧,成效,审核,产品\n");
        for (Videos v : list) w.write(String.format("%s,\"%s\",%s,%s,%s,%s,%s,%s,%s\n",
                v.getId(), v.getTitle() != null ? v.getTitle().replace("\"", "\"\"") : "",
                v.getUrl(), v.getRegion(), v.getChannelName(), v.getFrameType(),
                v.getEffectiveness(), v.getReviewStatus(), v.getProductName()));
    }

    /** 批量编辑 — 将指定视频列更新为统一值 */
    @PostMapping("/batch-edit")
    public ApiResponse<Integer> batchEdit(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) body.getOrDefault("ids", List.of());
        String region = (String) body.get("region");
        String frameType = (String) body.get("frame_type");
        String effectiveness = (String) body.get("effectiveness");
        String reviewStatus = (String) body.get("review_status");
        String productName = (String) body.get("product_name");
        int count = 0;
        for (String vid : ids) {
            Videos v = videosMapper.selectById(vid);
            if (v != null) {
                if (region != null) v.setRegion(region);
                if (frameType != null) v.setFrameType(frameType);
                if (effectiveness != null) v.setEffectiveness(effectiveness);
                if (reviewStatus != null) v.setReviewStatus(reviewStatus);
                if (productName != null) v.setProductName(productName);
                videosMapper.updateById(v); count++;
            }
        }
        return ApiResponse.ok(count);
    }

    /** 频道补全 — 通过 yt-dlp 或手工为缺失频道的视频补全 channel_name */
    @PostMapping("/backfill-channels")
    public ApiResponse<Map<String, Object>> backfillChannels() {
        int filled = 0;
        var videos = videosMapper.selectList(
                new LambdaQueryWrapper<Videos>().isNull(Videos::getChannelName).or().eq(Videos::getChannelName, ""));
        for (Videos v : videos) {
            // 尝试通过 yt-dlp 获取频道名
            try {
                Process p = new ProcessBuilder("yt-dlp", "--get-filename", "-o", "%(uploader)s", v.getUrl())
                        .redirectErrorStream(true).start();
                String result = new String(p.getInputStream().readAllBytes()).trim();
                if (!result.isEmpty() && p.waitFor() == 0) {
                    v.setChannelName(result); videosMapper.updateById(v); filled++;
                }
            } catch (Exception ignored) {}
        }
        return ApiResponse.ok(Map.of("filled", filled, "total", videos.size()));
    }

    /** 有成效素材的产品名列表 — 对齐 Python youtube_asset_products */
    @GetMapping("/asset-products")
    public ApiResponse<List<String>> assetProducts() {
        var rows = productAssetsMapper.selectList(
                new LambdaQueryWrapper<ProductAssets>().select(ProductAssets::getProductId).groupBy(ProductAssets::getProductId));
        List<String> names = new ArrayList<>();
        for (var pa : rows) {
            Products p = productsMapper.selectById(pa.getProductId());
            if (p != null && p.getProductName() != null) names.add(p.getProductName());
        }
        return ApiResponse.ok(names);
    }

    /** 批量查询视频关联的产品名 — 对齐 Python youtube_product_assets */
    @GetMapping("/product-assets")
    public ApiResponse<Map<String, List<String>>> productAssets(@RequestParam(defaultValue = "") String videoIds) {
        if (videoIds.isBlank()) return ApiResponse.ok(Map.of());
        List<String> ids = List.of(videoIds.split(","));
        Map<String, List<String>> mapping = new LinkedHashMap<>();
        for (String vid : ids) {
            vid = vid.trim();
            var assets = productAssetsMapper.selectList(
                    new LambdaQueryWrapper<ProductAssets>().eq(ProductAssets::getVideoId, vid));
            List<String> pnames = new ArrayList<>();
            for (var a : assets) {
                Products p = productsMapper.selectById(a.getProductId());
                if (p != null) pnames.add(p.getProductName());
            }
            mapping.put(vid, pnames);
        }
        return ApiResponse.ok(mapping);
    }
}
