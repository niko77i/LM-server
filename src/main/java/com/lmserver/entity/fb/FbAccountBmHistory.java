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

        
    /** 主键ID */
    private Long id;

    @TableField("account_id")
    /** 广告账户ID */
    private Long accountId;

    @TableField("old_bm_id")
    /** 旧BM ID */
    private Long oldBmId;

    @TableField("new_bm_id")
    /** 新BM ID */
    private Long newBmId;

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