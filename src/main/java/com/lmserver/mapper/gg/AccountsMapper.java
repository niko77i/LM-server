package com.lmserver.mapper.gg;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.AccountDto;
import com.lmserver.entity.gg.Accounts;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountsMapper extends BaseMapper<Accounts> {

    /** 分页查询账户 DTO 列表（JOIN mcc/agents/account_statuses，status/agent 为名称筛选） */
    List<AccountDto> selectAccountDtos(
            Page<AccountDto> page,
            @Param("ownerId") Long ownerId,
            @Param("search") String search,
            @Param("status") String status,
            @Param("mccId") Long mccId,
            @Param("agent") String agent);

    /** 按 ID 查询单个账户 DTO */
    AccountDto selectAccountDtoById(@Param("id") Long id);
}
