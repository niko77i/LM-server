package com.lmserver.mapper.common;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.entity.common.Users;
import org.apache.ibatis.annotations.Mapper;

/** MyBatis-Plus Mapper */
@Mapper
public interface UsersMapper extends BaseMapper<Users> {
}