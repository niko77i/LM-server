package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Products;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/**
 * 产品管理控制器 — /api/products/*，GG平台产品的完整CRUD+下拉选项
 */

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
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
    /** 获取单条记录详情 — 按主键 ID 查询 */
    public ApiResponse<Products> detail(@PathVariable Long id) {
        Products p = productService.getById(id);
        return p != null ? ApiResponse.ok(p) : ApiResponse.fail("产品不存在");
    }

    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
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
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<Products> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Products p = productService.update(id,
                str(body, "product_name"), str(body, "kpi"), str(body, "region"),
                str(body, "status"), str(body, "customer"), lng(body, "sales_person_id"),
                lng(body, "mcc_id"), dbl(body, "agency_ratio"));
        return p != null ? ApiResponse.ok(p) : ApiResponse.fail("产品不存在");
    }

    @DeleteMapping("/{id}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/options")
    /** 获取下拉选项 — 返回 id + name 的简略列表 */
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(productService.options(principal.getUserId()));
    }

    @PostMapping("/batch-update") public ApiResponse<Integer> batchUpdate(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Long> ids = (List<Long>) body.getOrDefault("ids", List.of());
        int c=0; for (Long id : ids) { productService.update(id, str(body,"product_name"), str(body,"kpi"),
                str(body,"region"), str(body,"status"), str(body,"customer"), lng(body,"sales_person_id"),
                lng(body,"mcc_id"), dbl(body,"agency_ratio")); c++; }
        return ApiResponse.ok(c);
    }

    @GetMapping("/deleted") public PagedResponse<Products> deleted(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) {
        var qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Products>()
                .eq(Products::getOwnerId, principal.getUserId()).isNotNull(Products::getDeletedAt);
        var pg = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Products>(page, size);
        productsMapper.selectPage(pg, qw); return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }
    @org.springframework.beans.factory.annotation.Autowired private com.lmserver.mapper.gg.ProductsMapper productsMapper;

    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        Products p = productService.getById(id);
        if (p != null) { p.setIsArchived(1L); productService.update(id,null,null,null,null,null,null,null,null); }
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/unarchive")
    public ApiResponse<Void> unarchive(@PathVariable Long id) {
        Products p = productService.getById(id);
        if (p != null) { p.setIsArchived(0L); productService.update(id,null,null,null,null,null,null,null,null); }
        return ApiResponse.ok();
    }

    private String str(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? v.toString() : null; }
    private Long lng(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Long.valueOf(v.toString()) : null; }
    private Double dbl(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Double.valueOf(v.toString()) : null; }
}
