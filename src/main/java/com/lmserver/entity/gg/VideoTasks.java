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

        
    /** 主键ID */
    private Long id;

    @TableField("task_id")
    /** 任务唯一标识 */
    private String taskId;

    @TableField("package")
    /** 包名称 */
    private String pkg;

    /** 状态 */
    private String status;

    /** 进度(0~1) */
    private Double progress;

    /** 状态信息 */
    private String message;

    @TableField("output_path")
    /** 输出路径 */
    private String outputPath;

    /** 设置JSON */
    private String settings;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("finished_at")
    /** 完成时间 */
    private LocalDateTime finishedAt;

}