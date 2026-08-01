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
@Table(name = "sales_persons")
public class SalesPersons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "owner_id")
    private Long ownerId;

    private String platform;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}