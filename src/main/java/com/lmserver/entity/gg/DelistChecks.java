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
@Table(name = "delist_checks")
public class DelistChecks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "is_delisted")
    private Long isDelisted;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Column(name = "error_msg")
    private String errorMsg;

}