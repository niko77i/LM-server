package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势数据点 DTO — 内嵌在 TrendSeriesDto 中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointDto {

    /** 日期 */
    private Object date;

    /** 指标值 */
    private double value;
}
