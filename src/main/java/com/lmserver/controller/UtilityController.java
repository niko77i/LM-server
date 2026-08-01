package com.lmserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具类控制器 — /api/browse 文件浏览 + /api/translate Google 翻译。
 * 翻译使用 Google 翻译免费接口（无需 API Key），与 Python deep-translator 一致。
 */
@Slf4j
@RestController
public class UtilityController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 浏览服务器目录 — 列出指定路径下的文件和文件夹。
     *
     * @param path 目录路径，默认为项目根目录
     */
    @GetMapping("/api/browse")
    public ApiResponse<List<String>> browse(@RequestParam(defaultValue = ".") String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) return ApiResponse.fail("目录不存在");
        return ApiResponse.ok(Arrays.stream(dir.listFiles())
                .map(f -> (f.isDirectory() ? "[DIR] " : "[FILE] ") + f.getName())
                .sorted()
                .collect(Collectors.toList()));
    }

    /**
     * Google 翻译 — 自动检测源语言，翻译为目标语言。
     * 使用 Google 翻译免费接口 translate.googleapis.com，
     * 无需 API Key，与 Python deep-translator 后端一致。
     *
     * 请求体: {"text": "要翻译的文本", "target": "zh-CN"}
     * 响应: {"success": true, "translated": "翻译结果"}
     */
    @PostMapping("/api/translate")
    public ApiResponse<Map<String, Object>> translate(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String target = body.getOrDefault("target", "zh-CN");

        if (text == null || text.isBlank()) {
            return ApiResponse.fail("请输入要翻译的文本");
        }

        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.googleapis.com/translate_a/single"
                    + "?client=gtx&sl=auto&tl=" + target
                    + "&dt=t&q=" + encoded;

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            ResponseEntity<String> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            // 解析 Google 翻译响应: [[["译文","原文",...]],...]
            JsonNode root = objectMapper.readTree(resp.getBody());
            StringBuilder translated = new StringBuilder();
            JsonNode sentences = root.get(0);
            if (sentences != null && sentences.isArray()) {
                for (JsonNode s : sentences) {
                    if (s.isArray() && s.size() > 0) {
                        translated.append(s.get(0).asText());
                    }
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("translated", translated.toString());
            result.put("source_lang", "auto");
            result.put("target_lang", target);
            return ApiResponse.ok(result);

        } catch (Exception e) {
            log.error("翻译失败: {}", e.getMessage());
            return ApiResponse.fail("翻译失败: " + e.getMessage());
        }
    }
}
