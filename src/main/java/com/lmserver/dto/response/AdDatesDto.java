package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 日期标记 DTO — 用于 /api/ad-reports/dates 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdDatesDto {

    /** 日期 → 记录数映射 */
    private Map<String, Object> dates;
}
