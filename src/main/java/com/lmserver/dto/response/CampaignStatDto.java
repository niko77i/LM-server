package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Campaign 分组统计 DTO — 用于 Dashboard 和 Compare 接口。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatDto {

    private String campaign;

    private double totalCost;

    private double totalInstalls;

    private long totalImpressions;

    private long totalClicks;

    private double totalInApp;

    private double avgCpi;

    private double ctr;

    private double cvr;
}
