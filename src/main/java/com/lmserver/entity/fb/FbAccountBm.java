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
@TableName("fb_account_bm")
public class FbAccountBm {

        
    /** 主键ID */
    private Long id;

    @TableField("account_id")
    /** 广告账户ID */
    private Long accountId;

    @TableField("bm_id")
    /** BM ID */
    private Long bmId;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}