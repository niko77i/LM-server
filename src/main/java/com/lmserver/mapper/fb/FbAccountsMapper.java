package com.lmserver.mapper.fb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.FbAccountDto;
import com.lmserver.entity.fb.FbAccounts;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FbAccountsMapper extends BaseMapper<FbAccounts> {

    /** 分页查询 FB 账户 DTO 列表（JOIN account_statuses，支持 bm_id 筛选） */
    List<FbAccountDto> selectFbAccountDtos(
            Page<FbAccountDto> page,
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("statusId") Long statusId,
            @Param("bmId") Long bmId);

    /** 按 ID 查询单个 FB 账户 DTO */
    FbAccountDto selectFbAccountDtoById(@Param("id") Long id);
}
