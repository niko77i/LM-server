package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_log")
public class AuditLog {

        
    /** 主键ID */
    private Long id;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;

    /** 操作类型 */
    private String action;

    @TableField("target_type")
    /** 目标类型 */
    private String targetType;

    @TableField("target_id")
    /** 目标ID */
    private Long targetId;

    @TableField("target_name")
    /** 目标名称 */
    private String targetName;

    /** 详情JSON */
    private String detail;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}