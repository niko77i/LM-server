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
@Table(name = "fb_products")
public class FbProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name")
    private String productName;

    private String kpi;

    private String region;

    private String status;

    @Column(name = "sales_person_id")
    private Long salesPersonId;

    @Column(name = "agency_ratio")
    private Double agencyRatio;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "is_archived")
    private Long isArchived;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}