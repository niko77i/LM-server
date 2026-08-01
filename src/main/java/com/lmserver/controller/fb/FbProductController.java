package com.lmserver.controller.fb;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.FbProducts;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.FbService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fb/products")
@RequiredArgsConstructor
public class FbProductController {

    private final FbService fbService;

    @GetMapping("/list")
    public PagedResponse<FbProducts> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region) {
        return fbService.listProducts(principal.getUserId(), page, size, search, region);
    }

    @GetMapping("/{id}")
    public ApiResponse<FbProducts> detail(@PathVariable Long id) {
        FbProducts p = fbService.getProductById(id);
        return p != null ? ApiResponse.ok(p) : ApiResponse.fail("产品不存在");
    }

    @PostMapping("/create")
    public ApiResponse<FbProducts> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("product_name");
        if (name == null || name.isBlank()) return ApiResponse.fail("产品名不能为空");
        Long spId = body.get("sales_person_id") != null ? Long.valueOf(body.get("sales_person_id").toString()) : null;
        Double ratio = body.get("agency_ratio") != null ? Double.valueOf(body.get("agency_ratio").toString()) : null;
        return ApiResponse.ok(fbService.createProduct(principal.getUserId(), name,
                (String) body.get("kpi"), (String) body.get("region"), spId, ratio));
    }

    @PutMapping("/{id}")
    public ApiResponse<FbProducts> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long spId = body.get("sales_person_id") != null ? Long.valueOf(body.get("sales_person_id").toString()) : null;
        Double ratio = body.get("agency_ratio") != null ? Double.valueOf(body.get("agency_ratio").toString()) : null;
        FbProducts p = fbService.updateProduct(id, (String) body.get("product_name"),
                (String) body.get("kpi"), (String) body.get("region"), spId, ratio);
        return p != null ? ApiResponse.ok(p) : ApiResponse.fail("产品不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { fbService.deleteProduct(id); return ApiResponse.ok(); }

    @GetMapping("/options")
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(fbService.productOptions(principal.getUserId()));
    }
}
