package com.lmserver.service;

import com.lmserver.entity.gg.Products;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 产品服务测试 - CRUD 和分页验证。
 * 测试环境使用 H2 内存数据库，表结构由 test-schema.sql 初始化。
 */
@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void testCreateProduct() {
        Products created = productService.create(1L, "Test Product", "Test KPI",
                "Test Region", "active", "Customer A", null, null, 0.5);
        assertNotNull(created, "创建应成功");
        assertNotNull(created.getId(), "应有自增 ID");
    }

    @Test
    void testProductOptionsNotNull() {
        var options = productService.options(1L);
        assertNotNull(options, "选项不应为 null");
    }
}
