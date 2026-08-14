package com.lmserver.mapper.gg;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.MccDto;
import com.lmserver.entity.gg.Mcc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MccMapper extends BaseMapper<Mcc> {

    /** 分页查询 MCC DTO 列表（JOIN mcc_levels + 子查询直属账户数） */
    List<MccDto> selectMccDtos(
            Page<MccDto> page,
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("level") String level);

    /** 按 ID 查询单个 MCC DTO */
    MccDto selectMccDtoById(@Param("id") Long id);
}
