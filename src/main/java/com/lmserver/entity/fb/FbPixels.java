package com.lmserver.entity.fb;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_pixels")
public class FbPixels {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pixel_bm_id")
    private Long pixelBmId;

    @Column(name = "pixel_name")
    private String pixelName;

    @Column(name = "pixel_id")
    private String pixelId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}