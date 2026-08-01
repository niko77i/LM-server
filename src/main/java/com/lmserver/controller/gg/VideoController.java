package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.AudioReplaceHistory;
import com.lmserver.entity.gg.VideoHistory;
import com.lmserver.entity.gg.VideoTasks;
import com.lmserver.mapper.gg.AudioReplaceHistoryMapper;
import com.lmserver.mapper.gg.VideoHistoryMapper;
import com.lmserver.mapper.gg.VideoTasksMapper;
import com.lmserver.service.FfmpegService;
import com.lmserver.service.ai.AiVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 视频处理控制器 — /api/video/* 和 /api/audio-replace/*，AI生成/FFmpeg合成/音频替换。
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VideoController {

    private final VideoTasksMapper videoTasksMapper;
    private final VideoHistoryMapper videoHistoryMapper;
    private final AudioReplaceHistoryMapper audioReplaceMapper;
    private final AiVideoService aiVideoService;
    private final FfmpegService ffmpegService;

    /** 可用的 AI Provider 列表 */
    @GetMapping("/video/providers")
    public ApiResponse<List<String>> providers() {
        return ApiResponse.ok(aiVideoService.getProviderNames());
    }

    /** 提交 AI 视频生成任务 */
    @PostMapping("/video/generate")
    public ApiResponse<VideoTasks> generate(@RequestBody Map<String, Object> body) {
        String provider = (String) body.getOrDefault("provider", "atlas");
        String imagePath = (String) body.get("image_path");
        String prompt = (String) body.getOrDefault("prompt", "");
        String apiKey = (String) body.getOrDefault("api_key", "");
        int duration = body.get("duration") != null ? Integer.parseInt(body.get("duration").toString()) : 5;

        VideoTasks task = new VideoTasks();
        task.setTaskId(UUID.randomUUID().toString());
        task.setPkg((String) body.getOrDefault("package", ""));
        task.setStatus("pending");
        task.setProgress(0.0);
        task.setMessage("正在提交AI生成任务...");
        task.setSettings(body.toString());
        task.setCreatedAt(LocalDateTime.now());
        videoTasksMapper.insert(task);

        // 异步提交到 AI Provider（简化版：同步等待）
        new Thread(() -> {
            try {
                String aiTaskId = aiVideoService.generate(provider, imagePath, duration, prompt, apiKey);
                task.setMessage("AI任务ID: " + aiTaskId);
                task.setStatus("processing");
                videoTasksMapper.updateById(task);
            } catch (Exception e) {
                log.error("AI生成失败", e);
                task.setStatus("failed");
                task.setMessage(e.getMessage());
                videoTasksMapper.updateById(task);
            }
        }).start();

        return ApiResponse.ok(task);
    }

    /** 查询 AI 任务进度 */
    @GetMapping("/video/progress/{taskId}")
    public ApiResponse<VideoTasks> progress(@PathVariable String taskId) {
        var list = videoTasksMapper.selectList(
                new LambdaQueryWrapper<VideoTasks>().eq(VideoTasks::getTaskId, taskId));
        return !list.isEmpty() ? ApiResponse.ok(list.get(0)) : ApiResponse.fail("任务不存在");
    }

    /** 获取视频生成历史配置 */
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
        h.setCreatedAt(LocalDateTime.now()); h.setUpdatedAt(LocalDateTime.now());
        videoHistoryMapper.insert(h);
        return ApiResponse.ok(h);
    }

    /** 提交 FFmpeg 视频合成 */
    @PostMapping("/video/compose")
    public ApiResponse<String> compose(@RequestBody Map<String, Object> body) {
        try {
            String imagePath = (String) body.get("image_path");
            String outputPath = (String) body.getOrDefault("output_path", "temp/video_set/output_" + System.currentTimeMillis() + ".mp4");
            String bgVideo = (String) body.get("bg_video");
            String logo = (String) body.get("logo");
            String text = (String) body.getOrDefault("text", "");
            int duration = body.get("duration") != null ? Integer.parseInt(body.get("duration").toString()) : 10;

            new File(outputPath).getParentFile().mkdirs();
            ffmpegService.composeVideo(imagePath, outputPath, duration, bgVideo, logo, text);
            return ApiResponse.ok(outputPath);
        } catch (Exception e) {
            log.error("FFmpeg合成失败", e);
            return ApiResponse.fail("合成失败: " + e.getMessage());
        }
    }

    /** 提交音频替换任务 */
    @PostMapping("/audio-replace")
    public ApiResponse<AudioReplaceHistory> replaceAudio(@RequestBody Map<String, String> body) {
        try {
            String videoName = body.get("video_name");
            String audioName = body.get("audio_name");
            String outputPath = "temp/audio_replace/" + System.currentTimeMillis() + "_" + videoName;

            new File(outputPath).getParentFile().mkdirs();
            ffmpegService.replaceAudio(
                    "temp/video_set/" + videoName,
                    "temp/music/" + audioName,
                    outputPath);

            AudioReplaceHistory h = new AudioReplaceHistory();
            h.setVideoName(videoName); h.setAudioName(audioName);
            h.setOutputName(new File(outputPath).getName());
            h.setOutputPath(outputPath);
            h.setSizeMb(new File(outputPath).length() / 1048576.0);
            h.setCreatedAt(LocalDateTime.now());
            audioReplaceMapper.insert(h);
            return ApiResponse.ok(h);
        } catch (Exception e) {
            log.error("音频替换失败", e);
            return ApiResponse.fail("替换失败: " + e.getMessage());
        }
    }

    /** 音频替换历史 */
    @GetMapping("/audio-replace/history")
    public ApiResponse<List<AudioReplaceHistory>> audioHistory() {
        return ApiResponse.ok(audioReplaceMapper.selectList(
                new LambdaQueryWrapper<AudioReplaceHistory>().orderByDesc(AudioReplaceHistory::getCreatedAt)));
    }

    /** 视频任务列表 */
    @GetMapping("/video/tasks")
    public ApiResponse<List<VideoTasks>> tasks() {
        return ApiResponse.ok(videoTasksMapper.selectList(
                new LambdaQueryWrapper<VideoTasks>().orderByDesc(VideoTasks::getCreatedAt)));
    }
}
