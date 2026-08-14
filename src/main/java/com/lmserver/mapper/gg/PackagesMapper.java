package com.lmserver.mapper.gg;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.dto.response.PackageDto;
import com.lmserver.entity.gg.Packages;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PackagesMapper extends BaseMapper<Packages> {

    /** 按产品 ID 查询包列表 */
    List<PackageDto> selectPackagesByProductId(@Param("productId") Long productId);

    /** 批量按产品 ID 查询包列表 */
    List<PackageDto> selectPackagesByProductIds(@Param("productIds") List<Long> productIds);
}
