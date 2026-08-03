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
@TableName("account_mcc_history")
public class AccountMccHistory {

        
    /** 主键ID */
    private Long id;

    @TableField("account_id")
    /** 广告账户ID */
    private Long accountId;

    @TableField("old_mcc_id")
    /** 旧MCC ID */
    private Long oldMccId;

    @TableField("new_mcc_id")
    /** 新MCC ID */
    private Long newMccId;

    @TableField("changed_by")
    /** 操作人ID */
    private Long changedBy;

    @TableField("change_type")
    /** 变更类型: manual/auto */
    private String changeType;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}