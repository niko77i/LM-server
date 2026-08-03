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
@TableName("delist_notifications")
public class DelistNotifications {

        
    /** 主键ID */
    private Long id;

    @TableField("package_id")
    private Long packageId;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;

    @TableField("first_notified")
    /** 是否已首次通知 */
    private Long firstNotified;

    @TableField("dismissed_at")
    /** 关闭时间 */
    private LocalDateTime dismissedAt;

    @TableField("reminder_count")
    /** 提醒次数 */
    private Long reminderCount;

}