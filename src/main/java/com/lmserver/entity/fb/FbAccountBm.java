package com.lmserver.entity.fb;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: fbaccountbm */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("fb_account_bm")
public class FbAccountBm {

        
    private Long id;

    @TableField("account_id")
    private Long accountId;

    @TableField("bm_id")
    private Long bmId;

    @TableField("created_at")
    private LocalDateTime createdAt;

}