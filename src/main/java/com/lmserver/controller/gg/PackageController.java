package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.Packages;
import com.lmserver.mapper.gg.PackagesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品包管理控制器 — /api/packages/*。管理产品下的素材包/系列。
 */
@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackagesMapper mapper;

    @GetMapping("/list")
    public ApiResponse<List<Packages>> list(@RequestParam(required = false) Long productId) {
        var qw = new LambdaQueryWrapper<Packages>();
        if (productId != null) qw.eq(Packages::getProductId, productId);
        return ApiResponse.ok(mapper.selectList(qw));
    }

    @PostMapping("/create")
    public ApiResponse<Packages> create(@RequestBody Map<String, Object> body) {
        Packages p = new Packages();
        p.setProductId(toLong(body.get("product_id")));
        p.setSeriesName((String) body.get("series_name"));
        p.setPackageName((String) body.get("package_name"));
        p.setUrl((String) body.get("url"));
        p.setStatus((String) body.getOrDefault("status", ""));
        mapper.insert(p);
        return ApiResponse.ok(p);
    }

    @PutMapping("/{id}")
    public ApiResponse<Packages> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Packages p = mapper.selectById(id);
        if (p == null) return ApiResponse.fail("不存在");
        if (body.containsKey("series_name")) p.setSeriesName((String) body.get("series_name"));
        if (body.containsKey("package_name")) p.setPackageName((String) body.get("package_name"));
        if (body.containsKey("url")) p.setUrl((String) body.get("url"));
        if (body.containsKey("status")) p.setStatus((String) body.get("status"));
        mapper.updateById(p);
        return ApiResponse.ok(p);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { mapper.deleteById(id); return ApiResponse.ok(); }

    private Long toLong(Object v) { return v != null ? Long.valueOf(v.toString()) : null; }
}
