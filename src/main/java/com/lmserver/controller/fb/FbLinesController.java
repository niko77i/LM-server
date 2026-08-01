package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.fb.FbLines;
import com.lmserver.mapper.fb.FbLinesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FB 广告线（落地页）控制器 — /api/fb/lines/*。
 */
@RestController
@RequestMapping("/api/fb/lines")
@RequiredArgsConstructor
public class FbLinesController {

    private final FbLinesMapper mapper;
    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public ApiResponse<List<FbLines>> list(@RequestParam Long productId) {
        return ApiResponse.ok(mapper.selectList(
                new LambdaQueryWrapper<FbLines>().eq(FbLines::getProductId, productId)));
    }
    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<FbLines> create(@RequestBody Map<String, Object> body) {
        FbLines line = new FbLines();
        line.setProductId(Long.valueOf(body.get("product_id").toString()));
        line.setLineName((String) body.get("line_name"));
        line.setLink((String) body.get("link"));
        if (body.get("pixel_id") != null) line.setPixelId(Long.valueOf(body.get("pixel_id").toString()));
        mapper.insert(line);
        return ApiResponse.ok(line);
    }
    @DeleteMapping("/{id}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return ApiResponse.ok();
    }
}
