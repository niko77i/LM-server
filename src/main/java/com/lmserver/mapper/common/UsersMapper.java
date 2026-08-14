package com.lmserver.mapper.common;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.dto.response.UserBriefDto;
import com.lmserver.entity.common.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UsersMapper extends BaseMapper<Users> {

    /** 查询用户简要信息列表 */
    List<UserBriefDto> selectUserBriefs(
            @Param("platform") String platform,
            @Param("role") String role);

    /** 按 ID 查询用户简要信息 */
    UserBriefDto selectUserBriefById(@Param("id") Long id);
}
