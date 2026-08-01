package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: auditlog */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_log")
public class AuditLog {

        
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String action;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_name")
    private String targetName;

    private String detail;

    @TableField("created_at")
    private LocalDateTime createdAt;

}