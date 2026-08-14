package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 广告统计简要 DTO — 用于 /api/ad-reports/stats 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdStatsDto {

    private double totalCost;

    private long totalImpressions;

    private long totalClicks;

    private long totalInstalls;

    private double totalInApp;

    private long recordCount;
}
