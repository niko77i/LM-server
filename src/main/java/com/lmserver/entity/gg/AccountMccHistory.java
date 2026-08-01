package com.lmserver.entity.gg;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: accountmcchistory */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("account_mcc_history")
public class AccountMccHistory {

        
    private Long id;

    @TableField("account_id")
    private Long accountId;

    @TableField("old_mcc_id")
    private Long oldMccId;

    @TableField("new_mcc_id")
    private Long newMccId;

    @TableField("changed_by")
    private Long changedBy;

    @TableField("change_type")
    private String changeType;

    @TableField("created_at")
    private LocalDateTime createdAt;

}