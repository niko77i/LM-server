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
@Table(name = "products")
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name")
    private String productName;

    private String kpi;

    private String region;

    private String status;

    @Column(name = "mcc_id")
    private Long mccId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "runner_ids")
    private String runnerIds;

    @Column(name = "is_archived")
    private Long isArchived;

    private String customer;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "agency_ratio")
    private Double agencyRatio;

    @Column(name = "sales_person_id")
    private Long salesPersonId;

    @Column(name = "sales_person")
    private String salesPerson;

}