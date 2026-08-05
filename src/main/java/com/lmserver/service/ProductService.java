/**
 * 产品管理服务接口 — 按名称/地区/状态筛选的产品分页查询
 */

package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Products;
import java.util.List;
import java.util.Map;
public interface ProductService {
    PagedResponse<Map<String, Object>> list(Long ownerId, int page, int size, String search, String region, String status);
    Products getById(Long id);
    Products create(Long ownerId, String productName, String kpi, String region, String status,
                    String customer, Long salesPersonId, Long mccId, Double agencyRatio);
    Products update(Long id, String productName, String kpi, String region, String status,
                    String customer, Long salesPersonId, Long mccId, Double agencyRatio);
    void delete(Long id);
    List<Products> options(Long ownerId);
}
