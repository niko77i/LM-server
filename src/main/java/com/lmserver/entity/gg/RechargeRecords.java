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

        
    private Long id;

    @TableField("account_id")
    private String accountId;

    private String amount;

    private String operator;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    private String status;

    @TableField("sheets_synced")
    private Long sheetsSynced;

    @TableField("sheets_error")
    private String sheetsError;

    @TableField("agent_id")
    private Long agentId;

}