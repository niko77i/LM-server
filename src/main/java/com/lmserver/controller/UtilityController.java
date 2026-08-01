package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具类控制器 — /api/browse/* 文件浏览 + /api/translate 翻译。
 */
@RestController
public class UtilityController {
    @GetMapping("/api/browse")
    public ApiResponse<List<String>> browse(@RequestParam(defaultValue = ".") String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) return ApiResponse.fail("目录不存在");
        return ApiResponse.ok(Arrays.stream(dir.listFiles())
                .map(f -> (f.isDirectory() ? "[DIR] " : "[FILE] ") + f.getName())
                .collect(Collectors.toList()));
    }
    @PostMapping("/api/translate")
    public ApiResponse<String> translate(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String target = body.getOrDefault("target", "zh");
        // TODO: Phase 5 对接翻译 API
        return ApiResponse.ok("[翻译占位] " + target + ": " + text);
    }
}
