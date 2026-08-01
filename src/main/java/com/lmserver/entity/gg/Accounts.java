package com.lmserver.entity.gg;

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
@Table(name = "accounts")
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "mcc_id")
    private Long mccId;

    private String timezone;

    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    @Column(name = "death_date")
    private LocalDateTime deathDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "status_changed_date")
    private LocalDateTime statusChangedDate;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}