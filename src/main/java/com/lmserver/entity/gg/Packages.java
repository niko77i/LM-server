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

        
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("series_name")
    private String seriesName;

    @TableField("package_name")
    private String packageName;

    private String url;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

}