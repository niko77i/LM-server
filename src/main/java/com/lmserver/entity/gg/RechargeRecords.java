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
@Table(name = "recharge_records")
public class RechargeRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private String accountId;

    private String amount;

    private String operator;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String status;

    @Column(name = "sheets_synced")
    private Long sheetsSynced;

    @Column(name = "sheets_error")
    private String sheetsError;

    @Column(name = "agent_id")
    private Long agentId;

}