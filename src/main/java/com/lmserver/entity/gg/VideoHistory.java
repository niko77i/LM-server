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
@TableName("video_history")
public class VideoHistory {

        
    /** 主键ID */
    private Long id;

    @TableField("package")
    /** 包名称 */
    private String pkg;

    /** 名称 */
    private String name;

    /** 设置JSON */
    private String settings;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("updated_at")
    /** 更新时间 */
    private LocalDateTime updatedAt;

}