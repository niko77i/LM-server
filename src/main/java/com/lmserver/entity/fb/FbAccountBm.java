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
@Table(name = "fb_account_bm")
public class FbAccountBm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "bm_id")
    private Long bmId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}