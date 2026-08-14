package com.lmserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.common.Config;
import com.lmserver.mapper.common.ConfigMapper;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * 系统配置控制器 — /api/config/*，键值对配置的查询和修改 + AI/Sheets 用户配置
 */

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/list")
    public ApiResponse<List<Config>> list() {
        return ApiResponse.ok(mapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/key/{key}")
    public ApiResponse<Config> get(@PathVariable String key) {
        Config c = mapper.selectById(key);
        return c != null ? ApiResponse.ok(c) : ApiResponse.fail("配置不存在");
    }

    @PutMapping("/key/{key}")
    public ApiResponse<Void> save(@PathVariable String key, @RequestBody Map<String, String> body) {
        Config c = mapper.selectById(key);
        if (c == null) {
            c = new Config(); c.setKey(key); c.setValue(body.get("value"));
            mapper.insert(c);
        } else {
            c.setValue(body.get("value")); mapper.updateById(c);
        }
        return ApiResponse.ok();
    }

    @DeleteMapping("/key/{key}")
    public ApiResponse<Void> delete(@PathVariable String key) { mapper.deleteById(key); return ApiResponse.ok(); }

    /** AI 分析配置（按用户隔离），前端读 res.config */
    @GetMapping("/ai")
    public Map<String, Object> getAiConfig(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        Config c = mapper.selectById("ai_analysis_" + principal.getUserId());
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("enabled", false);
        cfg.put("provider", "volcano");
        cfg.put("model", "deepseek-v4-flash");
        cfg.put("api_key", "");
        cfg.put("endpoint", "https://ark.cn-beijing.volces.com/api/coding/v3/chat/completions");
        if (c != null && c.getValue() != null && !c.getValue().isBlank()) {
            try {
                Map<String, Object> parsed = objectMapper.readValue(c.getValue(), new TypeReference<Map<String, Object>>() {});
                cfg.putAll(parsed);
            } catch (Exception ignored) {}
        }
        result.put("config", cfg);
        return result;
    }

    @PostMapping("/ai")
    public Map<String, Object> saveAiConfig(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("enabled", Boolean.TRUE.equals(body.get("enabled")));
        cfg.put("provider", body.getOrDefault("provider", "volcano"));
        cfg.put("model", body.getOrDefault("model", "deepseek-v4-flash"));
        cfg.put("api_key", body.getOrDefault("api_key", ""));
        cfg.put("endpoint", body.getOrDefault("endpoint", "https://ark.cn-beijing.volces.com/api/coding/v3/chat/completions"));
        try {
            upsert("ai_analysis_" + principal.getUserId(), objectMapper.writeValueAsString(cfg));
        } catch (Exception ignored) {}
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        return result;
    }

    /** Google Sheets 用户配置，前端读 res.sheets + res.active_id */
    @GetMapping("/google-sheets")
    public Map<String, Object> getGoogleSheets(@AuthenticationPrincipal UserPrincipal principal) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        String key = sheetConfigKey(principal);
        Config c = mapper.selectById(key);
        List<Object> sheets = List.of();
        String activeId = "";
        if (c != null && c.getValue() != null && !c.getValue().isBlank()) {
            try {
                sheets = objectMapper.readValue(c.getValue(), new TypeReference<List<Object>>() {});
            } catch (Exception ignored) {}
        }
        Config active = mapper.selectById(key + "_active");
        if (active != null && active.getValue() != null) {
            activeId = active.getValue();
        }
        result.put("sheets", sheets);
        result.put("active_id", activeId);
        return result;
    }

    @PostMapping("/google-sheets")
    public Map<String, Object> saveGoogleSheets(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String key = sheetConfigKey(principal);
        Object sheets = body.get("sheets");
        String activeId = String.valueOf(body.getOrDefault("active_id", ""));
        if (sheets != null) {
            try {
                upsert(key, objectMapper.writeValueAsString(sheets));
            } catch (Exception ignored) {}
        }
        upsert(key + "_active", activeId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        return result;
    }

    private String sheetConfigKey(UserPrincipal principal) {
        return "fb".equals(principal.getPlatform())
                ? "google_sheets_fb_" + principal.getUserId()
                : "google_sheets_" + principal.getUserId();
    }

    private void upsert(String key, String value) {
        Config c = mapper.selectById(key);
        if (c == null) {
            c = new Config(); c.setKey(key); c.setValue(value);
            mapper.insert(c);
        } else {
            c.setValue(value); mapper.updateById(c);
        }
    }
}
