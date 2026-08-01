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
@TableName("fb_account_bm_history")
public class FbAccountBmHistory {

        
    private Long id;

    @TableField("account_id")
    private Long accountId;

    @TableField("old_bm_id")
    private Long oldBmId;

    @TableField("new_bm_id")
    private Long newBmId;

    @TableField("changed_by")
    private Long changedBy;

    @TableField("change_type")
    private String changeType;

    @TableField("created_at")
    private LocalDateTime createdAt;

}