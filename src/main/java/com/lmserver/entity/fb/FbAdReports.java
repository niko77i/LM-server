package com.lmserver.entity.fb;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("fb_ad_reports")
public class FbAdReports {

        
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("product_name")
    private String productName;

    @TableField("line_name")
    private String lineName;

    @TableField("report_date")
    private LocalDateTime reportDate;

    @TableField("account_name")
    private String accountName;

    @TableField("account_id")
    private String accountId;

    private Double cost;

    private Long impressions;

    private Long clicks;

    private Long registrations;

    private Long purchases;

    @TableField("cost_per_purchase")
    private Double costPerPurchase;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("saved_at")
    private LocalDateTime savedAt;

}