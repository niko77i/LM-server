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
@Table(name = "fb_account_bm_history")
public class FbAccountBmHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "old_bm_id")
    private Long oldBmId;

    @Column(name = "new_bm_id")
    private Long newBmId;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "change_type")
    private String changeType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}