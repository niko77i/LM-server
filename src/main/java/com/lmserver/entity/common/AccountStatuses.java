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
@TableName("account_statuses")
public class AccountStatuses {

        
    private Long id;

    private String name;

    @TableField("owner_id")
    private Long ownerId;

    private String platform;

    @TableField("created_at")
    private LocalDateTime createdAt;

}