package com.lmserver.mapper.gg;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.entity.gg.AdReports;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

@Mapper
public interface AdReportsMapper extends BaseMapper<AdReports> {

    /** 广告报告汇总查询 — 按条件聚合 */
    LinkedHashMap selectSummary(
            @Param("userId") Long userId,
            @Param("productName") String productName,
            @Param("region") String region,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate);
}
