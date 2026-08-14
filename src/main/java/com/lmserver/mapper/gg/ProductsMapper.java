package com.lmserver.mapper.gg;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ProductDto;
import com.lmserver.entity.gg.Products;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductsMapper extends BaseMapper<Products> {

    /** 分页查询产品 DTO 列表（JOIN mcc/sales_persons + 子查询计数） */
    List<ProductDto> selectProductDtos(
            Page<ProductDto> page,
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("region") String region,
            @Param("status") String status);

    /** 按 ID 查询单个产品 DTO */
    ProductDto selectProductDtoById(@Param("id") Long id);
}
