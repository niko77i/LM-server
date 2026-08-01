package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.Tags;
import com.lmserver.entity.gg.VideoConsumption;
import com.lmserver.entity.gg.Videos;
import com.lmserver.mapper.common.TagsMapper;
import com.lmserver.mapper.gg.VideoConsumptionMapper;
import com.lmserver.mapper.gg.VideosMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
}
