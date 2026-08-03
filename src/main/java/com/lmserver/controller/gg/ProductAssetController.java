package com.lmserver.controller.gg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.gg.ProductAssets;
import com.lmserver.mapper.gg.ProductAssetsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品素材关联管理 — /api/product-assets/*。
 */
@RestController
@RequestMapping("/api/product-assets")
@RequiredArgsConstructor
public class ProductAssetController {

    private final ProductAssetsMapper mapper;

    @GetMapping("/list")
    public ApiResponse<List<ProductAssets>> list(@RequestParam Long productId) {
        return ApiResponse.ok(mapper.selectList(
                new LambdaQueryWrapper<ProductAssets>().eq(ProductAssets::getProductId, productId)));
    }

    @PostMapping("/bind")
    public ApiResponse<ProductAssets> bind(@RequestBody Map<String, Object> body) {
        ProductAssets pa = new ProductAssets();
        pa.setProductId(Long.valueOf(body.get("product_id").toString()));
        pa.setVideoId((String) body.get("video_id"));
        pa.setVideoOwnerId(body.get("video_owner_id") != null ? Long.valueOf(body.get("video_owner_id").toString()) : 1L);
        pa.setAddedBy(body.get("added_by") != null ? Long.valueOf(body.get("added_by").toString()) : null);
        mapper.insert(pa);
        return ApiResponse.ok(pa);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> unbind(@PathVariable Long id) { mapper.deleteById(id); return ApiResponse.ok(); }
}
