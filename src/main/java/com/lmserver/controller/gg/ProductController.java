package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Products;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    public PagedResponse<Products> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status) {
        return productService.list(principal.getUserId(), page, size, search, region, status);
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<Products> detail(@PathVariable Long id) {
        Products p = productService.getById(id);
        return p != null ? ApiResponse.ok(p) : ApiResponse.fail("产品不存在");
    }

    @PostMapping("/create")
    public ApiResponse<Products> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String name = str(body, "product_name");
        if (name == null || name.isBlank()) return ApiResponse.fail("产品名不能为空");
        return ApiResponse.ok(productService.create(
                principal.getUserId(), name,
                str(body, "kpi"), str(body, "region"), str(body, "status"),
                str(body, "customer"), lng(body, "sales_person_id"),
                lng(body, "mcc_id"), dbl(body, "agency_ratio")));
    }

    @PutMapping("/{id}")
    public ApiResponse<Products> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Products p = productService.update(id,
                str(body, "product_name"), str(body, "kpi"), str(body, "region"),
                str(body, "status"), str(body, "customer"), lng(body, "sales_person_id"),
                lng(body, "mcc_id"), dbl(body, "agency_ratio"));
        return p != null ? ApiResponse.ok(p) : ApiResponse.fail("产品不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/options")
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(productService.options(principal.getUserId()));
    }

    private String str(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? v.toString() : null; }
    private Long lng(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Long.valueOf(v.toString()) : null; }
    private Double dbl(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Double.valueOf(v.toString()) : null; }
}
