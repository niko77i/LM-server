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
@TableName("fb_pixels")
public class FbPixels {

        
    /** 主键ID */
    private Long id;

    @TableField("pixel_bm_id")
    /** Pixel BM ID */
    private Long pixelBmId;

    @TableField("pixel_name")
    /** Pixel名称 */
    private String pixelName;

    @TableField("pixel_id")
    /** Pixel ID */
    private String pixelId;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}