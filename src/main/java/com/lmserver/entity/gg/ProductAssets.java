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
@Table(name = "product_assets")
public class ProductAssets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "video_id")
    private String videoId;

    @Column(name = "video_owner_id")
    private Long videoOwnerId;

    @Column(name = "added_by")
    private Long addedBy;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

}