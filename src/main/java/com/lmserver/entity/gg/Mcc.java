package com.lmserver.entity.gg;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: mcc */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("mcc")
public class Mcc {

        
    private Long id;

    private String name;

    @TableField("mcc_id")
    private String mccId;

    @TableField("parent_mcc_id")
    private Long parentMccId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("shared_user_ids")
    private String sharedUserIds;

    @TableField("level_id")
    private Long levelId;

}