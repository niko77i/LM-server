package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异常检测 DTO — 用于仪表盘异常列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDto {

    /** 系列名 */
    private String campaign;

    /** 异常日期 */
    private Object date;

    /** 类型: cost_spike / cpi_spike */
    private String type;

    /** 详情描述 */
    private String detail;
}
