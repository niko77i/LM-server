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
@Table(name = "ad_reports")
public class AdReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_name")
    private String productName;

    private String region;

    @Column(name = "report_date")
    private LocalDateTime reportDate;

    @Column(name = "customer_id")
    private String customerId;

    private String campaign;

    private Double cost;

    private Long impressions;

    private Long clicks;

    private Long installs;

    @Column(name = "in_app_actions")
    private Double inAppActions;

    @Column(name = "cost_per_in_app")
    private Double costPerInApp;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    private String account;

}