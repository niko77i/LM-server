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

        
    /** 主键ID */
    private Long id;

    /** 名称 */
    private String name;

    @TableField("account_id")
    /** 广告账户ID */
    private String accountId;

    /** 时区 */
    private String timezone;

    @TableField("status_id")
    /** 状态ID */
    private Long statusId;

    @TableField("acquired_date")
    /** 获取日期 */
    private LocalDate acquiredDate;

    @TableField("status_changed_date")
    /** 状态变更日期 */
    private LocalDateTime statusChangedDate;

    @TableField("owner_id")
    /** 归属用户ID */
    private Long ownerId;

    @TableField("deleted_at")
    /** 软删除时间 */
    private LocalDateTime deletedAt;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("updated_at")
    /** 更新时间 */
    private LocalDateTime updatedAt;

}