package com.lmserver.entity.gg;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ad_reports")
public class AdReports {

        
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("product_name")
    private String productName;

    private String region;

    @TableField("report_date")
    private LocalDateTime reportDate;

    @TableField("customer_id")
    private String customerId;

    private String campaign;

    private Double cost;

    private Long impressions;

    private Long clicks;

    private Long installs;

    @TableField("in_app_actions")
    private Double inAppActions;

    @TableField("cost_per_in_app")
    private Double costPerInApp;

    @TableField("saved_at")
    private LocalDateTime savedAt;

    private String account;

}