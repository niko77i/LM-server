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
@TableName("products")
public class Products {

        
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

    @TableField("mcc_id")
    /** MCC ID */
    private Long mccId;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("owner_id")
    /** 归属用户ID */
    private Long ownerId;

    @TableField("runner_ids")
    /** 在跑人员ID列表(JSON) */
    private String runnerIds;

    @TableField("is_archived")
    /** 是否归档: 0否/1是 */
    private Long isArchived;

    /** 客户名称 */
    private String customer;

    @TableField("deleted_at")
    /** 软删除时间 */
    private LocalDateTime deletedAt;

    @TableField("agency_ratio")
    /** 代理比例 */
    private Double agencyRatio;

    @TableField("sales_person_id")
    /** 商务人员ID */
    private Long salesPersonId;

    @TableField("sales_person")
    /** 商务人员 */
    private String salesPerson;

}