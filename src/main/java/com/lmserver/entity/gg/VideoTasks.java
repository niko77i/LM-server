package com.lmserver.entity.gg;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_tasks")
public class VideoTasks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "package")
    private String pkg;

    private String status;

    private Double progress;

    private String message;

    @Column(name = "output_path")
    private String outputPath;

    private String settings;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

}