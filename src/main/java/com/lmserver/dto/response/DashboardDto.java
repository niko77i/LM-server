package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘 DTO — 用于 /api/ad-reports/dashboard 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    /** 汇总指标 */
    private DashboardSummaryDto summary;

    /** 环比：cost_change_pct / installs_change_pct / cpi_change_pct */
    private Map<String, Double> periodCompare;

    /** 异常检测列表 */
    private List<AnomalyDto> anomalies;

    /** Campaign 分组统计 */
    private List<CampaignStatDto> campaigns;

    /** 素材关联数 */
    private long assetCount;
}
