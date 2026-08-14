package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对比项目 DTO — 用于 /api/ad-reports/compare 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompareItemDto {

    private String name;

    private double totalCost;

    private long totalImpressions;

    private long totalClicks;

    private long totalInstalls;

    private double totalInApp;

    private double cpi;

    private double ctr;

    private double cvr;
}
