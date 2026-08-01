package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Products;
import java.util.List;

public interface ProductService {
    PagedResponse<Products> list(Long ownerId, int page, int size, String search, String region, String status);
    Products getById(Long id);
    Products create(Long ownerId, String productName, String kpi, String region, String status,
                    String customer, Long salesPersonId, Long mccId, Double agencyRatio);
    Products update(Long id, String productName, String kpi, String region, String status,
                    String customer, Long salesPersonId, Long mccId, Double agencyRatio);
    void delete(Long id);
    List<Products> options(Long ownerId);
}
