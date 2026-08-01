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
@TableName("video_tasks")
public class VideoTasks {

        
    private Long id;

    @TableField("task_id")
    private String taskId;

    @TableField("package")
    private String pkg;

    private String status;

    private Double progress;

    private String message;

    @TableField("output_path")
    private String outputPath;

    private String settings;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;

}