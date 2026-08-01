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
@Table(name = "fb_ad_reports")
public class FbAdReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "line_name")
    private String lineName;

    @Column(name = "report_date")
    private LocalDateTime reportDate;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_id")
    private String accountId;

    private Double cost;

    private Long impressions;

    private Long clicks;

    private Long registrations;

    private Long purchases;

    @Column(name = "cost_per_purchase")
    private Double costPerPurchase;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

}