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

        
    private Long id;

    @TableField("pixel_bm_id")
    private Long pixelBmId;

    @TableField("pixel_name")
    private String pixelName;

    @TableField("pixel_id")
    private String pixelId;

    @TableField("created_at")
    private LocalDateTime createdAt;

}