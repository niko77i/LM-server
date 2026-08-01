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

        
    private Long id;

    @TableField("video_name")
    private String videoName;

    @TableField("audio_name")
    private String audioName;

    @TableField("output_name")
    private String outputName;

    @TableField("output_path")
    private String outputPath;

    @TableField("size_mb")
    private Double sizeMb;

    @TableField("created_at")
    private LocalDateTime createdAt;

}