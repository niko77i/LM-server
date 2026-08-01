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

        
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("line_name")
    private String lineName;

    private String link;

    @TableField("pixel_id")
    private Long pixelId;

    @TableField("created_at")
    private LocalDateTime createdAt;

}