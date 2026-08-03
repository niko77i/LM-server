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

        
    /** 主键ID */
    private Long id;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;

    @TableField("product_name")
    /** 产品名称 */
    private String productName;

    /** 地区 */
    private String region;

    @TableField("report_date")
    /** 报告日期 */
    private LocalDateTime reportDate;

    @TableField("customer_id")
    private String customerId;

    /** 广告系列 */
    private String campaign;

    /** 消耗 */
    private Double cost;

    /** 展示次数 */
    private Long impressions;

    /** 点击次数 */
    private Long clicks;

    /** 安装数 */
    private Long installs;

    @TableField("in_app_actions")
    /** 应用内操作数 */
    private Double inAppActions;

    @TableField("cost_per_in_app")
    /** 单次应用内操作成本 */
    private Double costPerInApp;

    @TableField("saved_at")
    /** 保存时间 */
    private LocalDateTime savedAt;

    private String account;

}