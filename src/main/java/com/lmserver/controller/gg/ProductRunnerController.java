package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.ProductRunners;
import com.lmserver.mapper.gg.ProductRunnersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品在跑人员管理 — /api/product-runners/*。
 */
@RestController
@RequestMapping("/api/product-runners")
@RequiredArgsConstructor
public class ProductRunnerController {

    private final ProductRunnersMapper mapper;

    @GetMapping("/list")
    public ApiResponse<List<ProductRunners>> list(@RequestParam Long productId) {
        return ApiResponse.ok(mapper.selectList(
                new LambdaQueryWrapper<ProductRunners>().eq(ProductRunners::getProductId, productId)));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody Map<String, Long> body) {
        ProductRunners pr = new ProductRunners();
        pr.setProductId(body.get("product_id")); pr.setUserId(body.get("user_id"));
        mapper.insert(pr);
        return ApiResponse.ok();
    }

    @DeleteMapping("/remove")
    public ApiResponse<Void> remove(@RequestParam Long productId, @RequestParam Long userId) {
        mapper.delete(new LambdaQueryWrapper<ProductRunners>()
                .eq(ProductRunners::getProductId, productId).eq(ProductRunners::getUserId, userId));
        return ApiResponse.ok();
    }
}
