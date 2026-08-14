package com.lmserver.mapper.fb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.FbBmDto;
import com.lmserver.entity.fb.FbBms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FbBmsMapper extends BaseMapper<FbBms> {

    /** unified 查询：UNION ALL 合并 fb_bms + fb_pixel_bms */
    List<FbBmDto> selectFbBmDtos(@Param("ownerId") Long ownerId);

    /** 普通 BM 列表查询（仅 fb_bms，含 account_count 子查询） */
    List<FbBmDto> selectFbBmDtosNormal(
            Page<FbBmDto> page,
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("status") String status);
}
