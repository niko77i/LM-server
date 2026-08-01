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

        
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("video_id")
    private String videoId;

    @TableField("video_owner_id")
    private Long videoOwnerId;

    @TableField("added_by")
    private Long addedBy;

    @TableField("added_at")
    private LocalDateTime addedAt;

}