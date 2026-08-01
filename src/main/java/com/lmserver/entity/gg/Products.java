package com.lmserver.entity.gg;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: products */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("products")
public class Products {

        
    private Long id;

    @TableField("product_name")
    private String productName;

    private String kpi;

    private String region;

    private String status;

    @TableField("mcc_id")
    private Long mccId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("runner_ids")
    private String runnerIds;

    @TableField("is_archived")
    private Long isArchived;

    private String customer;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("agency_ratio")
    private Double agencyRatio;

    @TableField("sales_person_id")
    private Long salesPersonId;

    @TableField("sales_person")
    private String salesPerson;

}