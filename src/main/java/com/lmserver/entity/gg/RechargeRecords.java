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
@TableName("recharge_records")
public class RechargeRecords {

        
    /** 主键ID */
    private Long id;

    @TableField("account_id")
    /** 广告账户ID */
    private String accountId;

    /** 金额 */
    private String amount;

    /** 操作员 */
    private String operator;

    @TableField("created_by")
    /** 创建者用户ID */
    private Long createdBy;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 状态 */
    private String status;

    @TableField("sheets_synced")
    /** Sheets同步标记: 0未同步/1已同步 */
    private Long sheetsSynced;

    @TableField("sheets_error")
    /** Sheets同步错误信息 */
    private String sheetsError;

    @TableField("agent_id")
    /** 代理ID */
    private Long agentId;

}