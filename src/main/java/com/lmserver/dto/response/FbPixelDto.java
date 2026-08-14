package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Facebook Pixel DTO — 独立 DTO，对应 fb_pixels 表，增加 BM 名称关联字段。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FbPixelDto {

    private Long id;

    @TableField("pixel_bm_id")
    private Long pixelBmId;

    @TableField("pixel_name")
    private String pixelName;

    @TableField("pixel_id")
    private String pixelId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 所属 BM 名称 (JOIN fb_pixel_bms.name) */
    @TableField(exist = false)
    private String bmName;

    /** 所属 BM 的 bm_id (JOIN fb_pixel_bms.bm_id) */
    @TableField(exist = false)
    private String bmBmId;
}
