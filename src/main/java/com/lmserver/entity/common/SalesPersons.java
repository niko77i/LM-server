package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sales_persons")
public class SalesPersons {

        
    /** 主键ID */
    private Long id;

    /** 名称 */
    private String name;

    @TableField("owner_id")
    /** 归属用户ID */
    private Long ownerId;

    /** 所属平台: gg/fb */
    private String platform;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}