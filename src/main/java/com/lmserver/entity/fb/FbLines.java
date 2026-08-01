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
@Table(name = "fb_lines")
public class FbLines {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "line_name")
    private String lineName;

    private String link;

    @Column(name = "pixel_id")
    private Long pixelId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}