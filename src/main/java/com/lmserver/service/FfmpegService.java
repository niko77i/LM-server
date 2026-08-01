package com.lmserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * FFmpeg 视频处理服务 — 图片合成视频、音频替换、滤镜特效。
 * 通过 ProcessBuilder 调用系统 FFmpeg，限制并发最多 2 个进程。
 */
@Slf4j
@Service
public class FfmpegService {

    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    /** 最多同时运行 2 个 FFmpeg 进程 */
    private final Semaphore semaphore = new Semaphore(2);

    /**
     * 图片合成视频 — 背景 + 图片 + Logo + 文案。
     * @param imagePath   输入图片路径
     * @param outputPath  输出视频路径
     * @param duration    时长（秒）
     * @param bgVideoPath 背景视频路径（可选）
     * @return 输出文件路径
     */
    public String composeVideo(String imagePath, String outputPath, int duration,
                               String bgVideoPath, String logoPath, String text) throws Exception {
        if (!semaphore.tryAcquire(5, TimeUnit.MINUTES)) {
            throw new RuntimeException("FFmpeg 队列已满，请稍后重试");
        }
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(ffmpegPath);
            cmd.add("-y");  // 覆盖已有文件

            if (bgVideoPath != null && new File(bgVideoPath).exists()) {
                cmd.add("-i"); cmd.add(bgVideoPath);
            }
            cmd.add("-loop"); cmd.add("1");
            cmd.add("-i"); cmd.add(imagePath);
            if (logoPath != null && new File(logoPath).exists()) {
                cmd.add("-i"); cmd.add(logoPath);
            }

            cmd.add("-filter_complex");
            StringBuilder filter = new StringBuilder();
            filter.append("[1:v]scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2[v1]");
            if (bgVideoPath != null) {
                filter.append(";[0:v][v1]overlay=0:0:shortest=1");
            }
            if (logoPath != null) {
                filter.append(";[v][2:v]overlay=W-w-10:H-h-10");
            }
            cmd.add(filter.toString());

            cmd.add("-t"); cmd.add(String.valueOf(duration));
            cmd.add("-c:v"); cmd.add("libx264");
            cmd.add("-preset"); cmd.add("fast");
            cmd.add(outputPath);

            log.info("[FFmpeg] 执行: {}", String.join(" ", cmd));
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取输出（最多 10MB）
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int total = 0;
            try (InputStream is = process.getInputStream()) {
                int n;
                while ((n = is.read(buf)) != -1) {
                    total += n;
                    if (total > 10_000_000) { process.destroyForcibly(); break; }
                    out.write(buf, 0, n);
                }
            }

            if (!process.waitFor(300, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new RuntimeException("FFmpeg 超时（5分钟）");
            }
            if (process.exitValue() != 0) {
                throw new RuntimeException("FFmpeg 失败: " + out.toString("UTF-8"));
            }
            log.info("[FFmpeg] 完成: {}", outputPath);
            return outputPath;
        } finally {
            semaphore.release();
        }
    }

    /**
     * 音频替换 — 将视频的音频替换为指定音频文件。
     */
    public String replaceAudio(String videoPath, String audioPath, String outputPath) throws Exception {
        if (!semaphore.tryAcquire(5, TimeUnit.MINUTES)) {
            throw new RuntimeException("FFmpeg 队列已满");
        }
        try {
            List<String> cmd = List.of(
                ffmpegPath, "-y",
                "-i", videoPath, "-i", audioPath,
                "-c:v", "copy", "-c:a", "aac",
                "-map", "0:v:0", "-map", "1:a:0",
                "-shortest", outputPath
            );
            Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            if (!process.waitFor(120, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new RuntimeException("音频替换超时");
            }
            log.info("[FFmpeg] 音频替换完成: {}", outputPath);
            return outputPath;
        } finally {
            semaphore.release();
        }
    }
}
