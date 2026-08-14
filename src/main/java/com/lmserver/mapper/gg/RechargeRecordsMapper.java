package com.lmserver.mapper.gg;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.dto.response.RechargeRecordDto;
import com.lmserver.entity.gg.RechargeRecords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RechargeRecordsMapper extends BaseMapper<RechargeRecords> {

    /** 按账户 ID 查询充值记录 DTO（LEFT JOIN agents 返回代理名称） */
    List<RechargeRecordDto> selectDtosByAccountId(@Param("accountId") String accountId);
}
