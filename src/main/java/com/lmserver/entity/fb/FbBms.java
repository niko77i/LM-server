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
@TableName("fb_bms")
public class FbBms {

        
    /** 主键ID */
    private Long id;

    /** 名称 */
    private String name;

    @TableField("bm_id")
    /** BM ID */
    private String bmId;

    /** 备注 */
    private String note;

    /** 状态 */
    private String status;

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