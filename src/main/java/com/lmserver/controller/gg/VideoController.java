package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.AudioReplaceHistory;
import com.lmserver.entity.gg.VideoHistory;
import com.lmserver.entity.gg.VideoTasks;
import com.lmserver.mapper.gg.AudioReplaceHistoryMapper;
import com.lmserver.mapper.gg.VideoHistoryMapper;
import com.lmserver.mapper.gg.VideoTasksMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 视频处理控制器 — /api/video/* 和 /api/audio-replace/*。
 * AI 视频生成、FFmpeg 合成、音频替换。
 * Phase 5: FFmpeg 和 AI Provider 待对接。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VideoController {

    private final VideoTasksMapper videoTasksMapper;
    private final VideoHistoryMapper videoHistoryMapper;
    private final AudioReplaceHistoryMapper audioReplaceMapper;

    /** 提交视频生成任务 */
    @PostMapping("/video/generate")
    public ApiResponse<VideoTasks> generate(@RequestBody Map<String, Object> body) {
        VideoTasks task = new VideoTasks();
        task.setTaskId(java.util.UUID.randomUUID().toString());
        task.setPkg((String) body.getOrDefault("package", ""));
        task.setStatus("pending");
        task.setProgress(0.0);
        task.setSettings(body.containsKey("settings") ? body.get("settings").toString() : "{}");
        task.setCreatedAt(java.time.LocalDateTime.now());
        videoTasksMapper.insert(task);
        return ApiResponse.ok(task);
    }

    /** 查询任务进度 */
    @GetMapping("/video/progress/{taskId}")
    public ApiResponse<VideoTasks> progress(@PathVariable String taskId) {
        var list = videoTasksMapper.selectList(
                new LambdaQueryWrapper<VideoTasks>().eq(VideoTasks::getTaskId, taskId));
        return !list.isEmpty() ? ApiResponse.ok(list.get(0)) : ApiResponse.fail("任务不存在");
    }

    /** 视频生成历史 */
    @GetMapping("/video/history")
    public ApiResponse<List<VideoHistory>> history(@RequestParam(required = false) String pkg) {
        var qw = new LambdaQueryWrapper<VideoHistory>();
        if (pkg != null && !pkg.isBlank()) qw.eq(VideoHistory::getPkg, pkg);
        qw.orderByDesc(VideoHistory::getCreatedAt);
        return ApiResponse.ok(videoHistoryMapper.selectList(qw));
    }

    /** 保存视频生成历史配置 */
    @PostMapping("/video/history")
    public ApiResponse<VideoHistory> saveHistory(@RequestBody Map<String, Object> body) {
        VideoHistory h = new VideoHistory();
        h.setPkg((String) body.getOrDefault("package", ""));
        h.setName((String) body.getOrDefault("name", ""));
        h.setSettings(body.containsKey("settings") ? body.get("settings").toString() : "{}");
        h.setCreatedAt(java.time.LocalDateTime.now());
        h.setUpdatedAt(java.time.LocalDateTime.now());
        videoHistoryMapper.insert(h);
        return ApiResponse.ok(h);
    }

    /** 查询任务列表 */
    @GetMapping("/video/tasks")
    public ApiResponse<List<VideoTasks>> tasks() {
        return ApiResponse.ok(videoTasksMapper.selectList(
                new LambdaQueryWrapper<VideoTasks>().orderByDesc(VideoTasks::getCreatedAt)));
    }

    /** 音频替换历史 */
    @GetMapping("/audio-replace/history")
    public ApiResponse<List<AudioReplaceHistory>> audioHistory() {
        return ApiResponse.ok(audioReplaceMapper.selectList(
                new LambdaQueryWrapper<AudioReplaceHistory>().orderByDesc(AudioReplaceHistory::getCreatedAt)));
    }
}
