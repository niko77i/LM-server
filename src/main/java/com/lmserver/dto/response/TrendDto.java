package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 趋势图返回 DTO — 用于 /api/ad-reports/trends 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendDto {

    /** 多系列数据 */
    private List<TrendSeriesDto> series;
}
