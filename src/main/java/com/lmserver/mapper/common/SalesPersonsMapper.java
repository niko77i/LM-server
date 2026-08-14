package com.lmserver.mapper.common;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.entity.common.SalesPersons;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SalesPersonsMapper extends BaseMapper<SalesPersons> {

    /** 按名称排序查询所有商务人员 */
    List<SalesPersons> selectAllOrdered();
}
