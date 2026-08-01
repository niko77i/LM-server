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
@TableName("videos")
public class Videos {

        private String id;

        @TableField("owner_id")
    private Long ownerId;

    private String url;

    private String title;

    private String region;

    @TableField("frame_type")
    private String frameType;

    private String effectiveness;

    @TableField("product_name")
    private String productName;

    @TableField("review_status")
    private String reviewStatus;

    @TableField("is_public")
    private Boolean isPublic;

    @TableField("imported_at")
    private LocalDateTime importedAt;

    @TableField("channel_name")
    private String channelName;
}