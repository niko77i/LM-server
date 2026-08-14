package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.common.Config;
import com.lmserver.entity.common.Tags;
import com.lmserver.mapper.common.ConfigMapper;
import com.lmserver.mapper.common.TagsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置控制器 — /api/settings/*，批量配置保存+标签管理+账户设置
 */

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ConfigMapper configMapper;
    private final TagsMapper tagsMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
 * GG 系统设置控制器 — /api/settings*
 */
    @GetMapping
    public ApiResponse<List<Config>> getAllConfig() {
        return ApiResponse.ok(configMapper.selectList(new LambdaQueryWrapper<>()));
    }
    @PutMapping
    /** 批量保存配置 — 遍历 Map 逐个 upsert */
    public ApiResponse<Void> saveConfig(@RequestBody Map<String, String> settings) {
        settings.forEach((key, value) -> {
            Config c = configMapper.selectById(key);
            if (c == null) {
                c = new Config(); c.setKey(key); c.setValue(value);
                configMapper.insert(c);
            } else {
                c.setValue(value); configMapper.updateById(c);
            }
        });
        return ApiResponse.ok();
    }
    @GetMapping("/tags")
    /** 获取标签列表 — 返回所有标签键值对 */
    public ApiResponse<List<Tags>> getTags() {
        return ApiResponse.ok(tagsMapper.selectList(null));
    }
    @PutMapping("/tags/{key}")
    /** 保存标签 — 按 key upsert */
    public ApiResponse<Void> saveTag(@PathVariable String key, @RequestBody Map<String, String> body) {
        Tags t = tagsMapper.selectById(key);
        if (t == null) {
            t = new Tags(); t.setKey(key); t.setValue(body.get("value"));
            tagsMapper.insert(t);
        } else {
            t.setValue(body.get("value")); tagsMapper.updateById(t);
        }
        return ApiResponse.ok();
    }

    /** 账户设置 — 返回 recharge_sheet_id 和 sheet_mappings（对齐 GG-Server /api/settings/account，无 data 层） */
    @GetMapping("/account")
    public Map<String, Object> accountSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        Tags rid = tagsMapper.selectById("recharge_sheet_id");
        settings.put("recharge_sheet_id", rid != null ? rid.getValue() : "");

        // sheet_mappings：内置默认 → tags 表覆盖
        Map<String, Object> mappings = new LinkedHashMap<>();
        mappings.put("recharge", "充值表");
        mappings.put("received_accounts", "已接账户明细");
        mappings.put("my_dashboard", "我的看板");
        Tags sm = tagsMapper.selectById("sheet_mappings");
        if (sm != null && sm.getValue() != null && !sm.getValue().isBlank()) {
            try {
                Map<String, Object> parsed = objectMapper.readValue(sm.getValue(), new TypeReference<Map<String, Object>>() {});
                mappings.putAll(parsed);
            } catch (Exception ignored) {}
        }
        settings.put("sheet_mappings", mappings);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("settings", settings);
        return result;
    }

    /** 保存账户设置 — recharge_sheet_id 和 sheet_mappings 存 tags 表 */
    @PostMapping("/account")
    public ApiResponse<Void> saveAccountSettings(@RequestBody Map<String, Object> body) {
        if (body.containsKey("recharge_sheet_id")) {
            upsertTag("recharge_sheet_id", String.valueOf(body.get("recharge_sheet_id")));
        }
        if (body.containsKey("sheet_mappings")) {
            try {
                String json = objectMapper.writeValueAsString(body.get("sheet_mappings"));
                upsertTag("sheet_mappings", json);
            } catch (Exception ignored) {}
        }
        return ApiResponse.ok();
    }

    private void upsertTag(String key, String value) {
        Tags t = tagsMapper.selectById(key);
        if (t == null) {
            t = new Tags(); t.setKey(key); t.setValue(value);
            tagsMapper.insert(t);
        } else {
            t.setValue(value); tagsMapper.updateById(t);
        }
    }
}
