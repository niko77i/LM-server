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
@TableName("packages")
public class Packages {

        
    /** 主键ID */
    private Long id;

    @TableField("product_id")
    /** 产品ID */
    private Long productId;

    @TableField("series_name")
    /** 系列名称 */
    private String seriesName;

    @TableField("package_name")
    /** 包名称 */
    private String packageName;

    /** URL地址 */
    private String url;

    /** 状态 */
    private String status;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}