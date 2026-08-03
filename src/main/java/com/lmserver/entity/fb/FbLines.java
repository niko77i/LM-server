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
@TableName("fb_lines")
public class FbLines {

        
    /** 主键ID */
    private Long id;

    @TableField("product_id")
    /** 产品ID */
    private Long productId;

    @TableField("line_name")
    /** 线名称 */
    private String lineName;

    /** 链接地址 */
    private String link;

    @TableField("pixel_id")
    /** Pixel ID */
    private Long pixelId;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}