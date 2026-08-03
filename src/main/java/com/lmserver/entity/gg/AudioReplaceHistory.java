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
@TableName("audio_replace_history")
public class AudioReplaceHistory {

        
    /** 主键ID */
    private Long id;

    @TableField("video_name")
    /** 视频文件名 */
    private String videoName;

    @TableField("audio_name")
    /** 音频文件名 */
    private String audioName;

    @TableField("output_name")
    /** 输出文件名 */
    private String outputName;

    @TableField("output_path")
    /** 输出路径 */
    private String outputPath;

    @TableField("size_mb")
    /** 文件大小(MB) */
    private Double sizeMb;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}