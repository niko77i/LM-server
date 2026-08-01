package com.lmserver.mapper.common;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.entity.common.Config;
import org.apache.ibatis.annotations.Mapper;

/** MyBatis-Plus Mapper */
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {
}