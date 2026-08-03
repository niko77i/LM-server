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

        
    /** 主键ID */
    private Long id;

    @TableField("package_id")
    private Long packageId;

    @TableField("product_id")
    /** 产品ID */
    private Long productId;

    @TableField("is_delisted")
    /** 是否掉包: 0否/1是 */
    private Long isDelisted;

    @TableField("checked_at")
    /** 检测时间 */
    private LocalDateTime checkedAt;

    @TableField("error_msg")
    /** 错误信息 */
    private String errorMsg;

}