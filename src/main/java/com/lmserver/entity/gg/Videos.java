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

        /** 主键ID */
        private String id;

        @TableField("owner_id")
    /** 归属用户ID */
    private Long ownerId;

    /** URL地址 */
    private String url;

    /** 标题 */
    private String title;

    /** 地区 */
    private String region;

    @TableField("frame_type")
    /** 融帧类型 */
    private String frameType;

    /** 成效标记 */
    private String effectiveness;

    @TableField("product_name")
    /** 产品名称 */
    private String productName;

    @TableField("review_status")
    /** 审核状态 */
    private String reviewStatus;

    @TableField("is_public")
    /** 是否公开: 0否/1是 */
    private Boolean isPublic;

    @TableField("imported_at")
    /** 导入时间 */
    private LocalDateTime importedAt;

    @TableField("channel_name")
    /** 频道名称 */
    private String channelName;
}