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

        
    private Long id;

    @TableField("video_id")
    private String videoId;

    @TableField("video_owner_id")
    private Long videoOwnerId;

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private Long productId;

    private Double amount;

    @TableField("consume_date")
    private LocalDateTime consumeDate;

    @TableField("created_at")
    private LocalDateTime createdAt;

}