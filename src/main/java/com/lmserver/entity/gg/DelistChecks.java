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
@TableName("delist_checks")
public class DelistChecks {

        
    private Long id;

    @TableField("package_id")
    private Long packageId;

    @TableField("product_id")
    private Long productId;

    @TableField("is_delisted")
    private Long isDelisted;

    @TableField("checked_at")
    private LocalDateTime checkedAt;

    @TableField("error_msg")
    private String errorMsg;

}