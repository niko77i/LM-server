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

        
    /** 主键ID */
    private Long id;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;

    @TableField("product_name")
    /** 产品名称 */
    private String productName;

    @TableField("line_name")
    /** 线名称 */
    private String lineName;

    @TableField("report_date")
    /** 报告日期 */
    private LocalDateTime reportDate;

    @TableField("account_name")
    /** 账户名称 */
    private String accountName;

    @TableField("account_id")
    /** 广告账户ID */
    private String accountId;

    /** 消耗 */
    private Double cost;

    /** 展示次数 */
    private Long impressions;

    /** 点击次数 */
    private Long clicks;

    /** 注册数 */
    private Long registrations;

    /** 购买数 */
    private Long purchases;

    @TableField("cost_per_purchase")
    /** 单次购买成本 */
    private Double costPerPurchase;

    @TableField("updated_at")
    /** 更新时间 */
    private LocalDateTime updatedAt;

    @TableField("saved_at")
    /** 保存时间 */
    private LocalDateTime savedAt;

}