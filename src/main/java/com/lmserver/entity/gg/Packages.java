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
@Table(name = "packages")
public class Packages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "series_name")
    private String seriesName;

    @Column(name = "package_name")
    private String packageName;

    private String url;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}