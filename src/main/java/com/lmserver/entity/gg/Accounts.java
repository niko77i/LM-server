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
@TableName("accounts")
public class Accounts {

        
    private Long id;

    private String name;

    @TableField("account_id")
    private String accountId;

    @TableField("mcc_id")
    private Long mccId;

    private String timezone;

    @TableField("acquired_date")
    private LocalDate acquiredDate;

    @TableField("death_date")
    private LocalDateTime deathDate;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("status_changed_date")
    private LocalDateTime statusChangedDate;

    @TableField("agent_id")
    private Long agentId;

    @TableField("status_id")
    private Long statusId;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

}