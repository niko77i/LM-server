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

        
    private Long id;

    @TableField("package_id")
    private Long packageId;

    @TableField("user_id")
    private Long userId;

    @TableField("first_notified")
    private Long firstNotified;

    @TableField("dismissed_at")
    private LocalDateTime dismissedAt;

    @TableField("reminder_count")
    private Long reminderCount;

}