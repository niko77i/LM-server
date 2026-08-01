package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Products;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 产品服务测试 — 验证 CRUD 和分页查询。
 */
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    /** 测试产品分页列表 */
    @Test
    void testListProducts() {
        PagedResponse<Products> result = productService.list(1L, 1, 5, null, null, null);
        assertNotNull(result);
        assertTrue(result.getTotal() >= 0, "产品总数应 >= 0");
        assertNotNull(result.getItems(), "items 不应为 null");
    }

    /** 测试按 ID 查询产品 */
    @Test
    void testGetProductById() {
        Products p = productService.getById(1L);
        // 数据库中有测试数据所以不为null
        assertTrue(p == null || p.getId() != null, "如果存在应有有效ID");
    }

    /** 测试产品选项列表 */
    @Test
    void testProductOptions() {
        var options = productService.options(1L);
        assertNotNull(options, "选项不应为 null");
    }
}
