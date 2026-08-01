package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.common.Config;
import com.lmserver.entity.common.Tags;
import com.lmserver.mapper.common.ConfigMapper;
import com.lmserver.mapper.common.TagsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统设置控制器 - 配置管理、标签管理、账户设置。
 * 路由前缀: /api/settings
 */
/**
 * 系统设置控制器 — /api/settings/*，批量配置保存+标签管理
 */

/**
 * 系统设置控制器 — /api/settings/*，批量配置保存+标签管理
 */

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ConfigMapper configMapper;
    private final TagsMapper tagsMapper;

    /**
 * GG 系统设置控制器 — /api/settings*
 */
    @GetMapping
    public ApiResponse<List<Config>> getAllConfig() {
        return ApiResponse.ok(configMapper.selectList(null));
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
}
