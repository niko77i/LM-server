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
@Table(name = "account_mcc_history")
public class AccountMccHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "old_mcc_id")
    private Long oldMccId;

    @Column(name = "new_mcc_id")
    private Long newMccId;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "change_type")
    private String changeType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}