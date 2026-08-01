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
@Table(name = "videos")
public class Videos {

    @Id
    private String id;

    @Id
    @Column(name = "owner_id")
    private Long ownerId;

    private String url;

    private String title;

    private String region;

    @Column(name = "frame_type")
    private String frameType;

    private String effectiveness;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "review_status")
    private String reviewStatus;

    @Column(name = "is_public")
    private Long isPublic;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

}