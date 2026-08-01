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

        
    private Long id;

    @TableField("product_name")
    private String productName;

    private String kpi;

    private String region;

    private String status;

    @TableField("sales_person_id")
    private Long salesPersonId;

    @TableField("agency_ratio")
    private Double agencyRatio;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("is_archived")
    private Long isArchived;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

}