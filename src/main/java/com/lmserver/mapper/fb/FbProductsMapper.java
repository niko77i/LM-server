package com.lmserver.mapper.fb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.FbProductDto;
import com.lmserver.entity.fb.FbProducts;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FbProductsMapper extends BaseMapper<FbProducts> {

    /** 分页查询 FB 产品 DTO 列表（JOIN sales_persons） */
    List<FbProductDto> selectFbProductDtos(
            Page<FbProductDto> page,
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("status") String status);

    /** 按 ID 查询单个 FB 产品 DTO */
    FbProductDto selectFbProductDtoById(@Param("id") Long id);
}
