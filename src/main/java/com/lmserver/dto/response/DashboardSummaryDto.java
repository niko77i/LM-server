package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘汇总指标 DTO — 内嵌在 DashboardDto 中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {

    private double totalCost;

    private long totalImpressions;

    private long totalClicks;

    private long totalInstalls;

    private double totalInApp;

    private double avgCpi;

    private double avgCtr;

    private double avgCvr;
}
