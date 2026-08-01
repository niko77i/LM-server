package com.lmserver.entity.gg;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_runners")
public class ProductRunners {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Id
    @Column(name = "user_id")
    private Long userId;

}