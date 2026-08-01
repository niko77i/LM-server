package com.lmserver.entity.fb;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_accounts")
public class FbAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "account_id")
    private String accountId;

    private String timezone;

    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    @Column(name = "status_changed_date")
    private LocalDateTime statusChangedDate;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}