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
@Table(name = "video_consumption")
public class VideoConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id")
    private String videoId;

    @Column(name = "video_owner_id")
    private Long videoOwnerId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    private Double amount;

    @Column(name = "consume_date")
    private LocalDateTime consumeDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}