package com.lmserver.mapper.common;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.entity.common.AuditLog;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}