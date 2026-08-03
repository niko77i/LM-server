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
@TableName("product_assets")
public class ProductAssets {

        
    /** 主键ID */
    private Long id;

    @TableField("product_id")
    /** 产品ID */
    private Long productId;

    @TableField("video_id")
    /** 视频ID */
    private String videoId;

    @TableField("video_owner_id")
    /** 视频归属用户ID */
    private Long videoOwnerId;

    @TableField("added_by")
    /** 添加者ID */
    private Long addedBy;

    @TableField("added_at")
    /** 添加时间 */
    private LocalDateTime addedAt;

}