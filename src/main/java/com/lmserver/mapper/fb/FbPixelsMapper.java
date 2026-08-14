package com.lmserver.mapper.fb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.FbPixelDto;
import com.lmserver.entity.fb.FbPixels;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FbPixelsMapper extends BaseMapper<FbPixels> {

    /** 分页查询 Pixel DTO 列表（JOIN fb_pixel_bms） */
    List<FbPixelDto> selectFbPixelDtos(
            Page<FbPixelDto> page,
            @Param("search") String search);
}
