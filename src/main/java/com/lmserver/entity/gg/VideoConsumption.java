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
@TableName("video_consumption")
public class VideoConsumption {

        
    /** 主键ID */
    private Long id;

    @TableField("video_id")
    /** 视频ID */
    private String videoId;

    @TableField("video_owner_id")
    /** 视频归属用户ID */
    private Long videoOwnerId;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;

    @TableField("product_id")
    /** 产品ID */
    private Long productId;

    /** 金额 */
    private Double amount;

    @TableField("consume_date")
    /** 消耗日期 */
    private LocalDateTime consumeDate;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}