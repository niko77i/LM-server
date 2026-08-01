package com.lmserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.common.Config;
import com.lmserver.mapper.common.ConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * 系统配置控制器 — /api/config/*，键值对配置的查询和修改
 */

/**
 * 系统配置控制器 — /api/config/*，键值对配置的查询和修改
 */

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigMapper mapper;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public ApiResponse<List<Config>> list() {
        return ApiResponse.ok(mapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>()));
    }

    @GetMapping("/{key}")
    /** 获取单个配置 — 按 key 查询 */
    public ApiResponse<Config> get(@PathVariable String key) {
        Config c = mapper.selectById(key);
        return c != null ? ApiResponse.ok(c) : ApiResponse.fail("配置不存在");
    }

    @PutMapping("/{key}")
    /** 保存配置 — 存在则更新，不存在则插入 */
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
}
