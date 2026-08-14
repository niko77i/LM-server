package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Facebook 广告报告统计 DTO — /api/fb/reports/stats 的数组元素。
 * 对齐 GG-Server 权威后端：GROUP BY product_name, line_name, report_date。
 * 字段依赖全局 SNAKE_CASE 自动转 snake_case。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FbReportStatDto {

    private String productName;      // → product_name

    private String lineName;         // → line_name

    private LocalDateTime reportDate; // → report_date

    private Double totalCost;        // → total_cost

    private Long totalImpressions;   // → total_impressions

    private Long totalClicks;        // → total_clicks

    private Long totalRegistrations; // → total_registrations

    private Long totalPurchases;     // → total_purchases

    private Long accountCount;       // → account_count（COUNT DISTINCT account_id）
}
