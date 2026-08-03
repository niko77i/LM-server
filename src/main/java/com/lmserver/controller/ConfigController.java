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

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigMapper mapper;

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
}
