package com.lmserver.entity.common;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "copywritings")
public class Copywritings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String region;

    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "owner_id")
    private Long ownerId;

    private String effectiveness;

    @Column(name = "is_public")
    private Long isPublic;

}