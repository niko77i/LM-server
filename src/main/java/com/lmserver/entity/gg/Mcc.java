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
@TableName("mcc")
public class Mcc {

        
    /** 主键ID */
    private Long id;

    /** 名称 */
    private String name;

    @TableField("mcc_id")
    /** MCC ID */
    private String mccId;

    @TableField("parent_mcc_id")
    /** 父MCC ID */
    private Long parentMccId;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("updated_at")
    /** 更新时间 */
    private LocalDateTime updatedAt;

    @TableField("owner_id")
    /** 归属用户ID */
    private Long ownerId;

    @TableField("shared_user_ids")
    /** 共享用户ID列表(JSON) */
    private String sharedUserIds;

    @TableField("level_id")
    /** MCC等级ID */
    private Long levelId;

}