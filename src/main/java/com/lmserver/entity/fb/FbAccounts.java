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
@TableName("fb_accounts")
public class FbAccounts {

        
    private Long id;

    private String name;

    @TableField("account_id")
    private String accountId;

    private String timezone;

    @TableField("status_id")
    private Long statusId;

    @TableField("acquired_date")
    private LocalDate acquiredDate;

    @TableField("status_changed_date")
    private LocalDateTime statusChangedDate;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

}