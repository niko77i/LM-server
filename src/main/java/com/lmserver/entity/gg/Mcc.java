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
@Table(name = "mcc")
public class Mcc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "mcc_id")
    private String mccId;

    @Column(name = "parent_mcc_id")
    private Long parentMccId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "shared_user_ids")
    private String sharedUserIds;

    @Column(name = "level_id")
    private Long levelId;

}