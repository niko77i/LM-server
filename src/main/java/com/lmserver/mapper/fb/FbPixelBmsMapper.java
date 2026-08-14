package com.lmserver.mapper.fb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.FbPixelBmDto;
import com.lmserver.entity.fb.FbPixelBms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FbPixelBmsMapper extends BaseMapper<FbPixelBms> {

    /** 分页查询 Pixel BM DTO 列表（含像素计数子查询） */
    List<FbPixelBmDto> selectFbPixelBmDtos(
            Page<FbPixelBmDto> page,
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("status") String status);
}
