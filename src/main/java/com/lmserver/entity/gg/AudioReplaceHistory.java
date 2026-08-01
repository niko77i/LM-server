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
@Table(name = "audio_replace_history")
public class AudioReplaceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_name")
    private String videoName;

    @Column(name = "audio_name")
    private String audioName;

    @Column(name = "output_name")
    private String outputName;

    @Column(name = "output_path")
    private String outputPath;

    @Column(name = "size_mb")
    private Double sizeMb;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}