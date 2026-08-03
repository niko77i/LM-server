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
@TableName("fb_products")
public class FbProducts {

        
    /** 主键ID */
    private Long id;

    @TableField("product_name")
    /** 产品名称 */
    private String productName;

    /** KPI指标 */
    private String kpi;

    /** 地区 */
    private String region;

    /** 状态 */
    private String status;

    @TableField("sales_person_id")
    /** 商务人员ID */
    private Long salesPersonId;

    @TableField("agency_ratio")
    /** 代理比例 */
    private Double agencyRatio;

    @TableField("owner_id")
    /** 归属用户ID */
    private Long ownerId;

    @TableField("is_archived")
    /** 是否归档: 0否/1是 */
    private Long isArchived;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("updated_at")
    /** 更新时间 */
    private LocalDateTime updatedAt;

}