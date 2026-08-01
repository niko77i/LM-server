package com.lmserver.entity.fb;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(FbProductRunnersId.class)
@Table(name = "fb_product_runners")
public class FbProductRunners {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Id
    @Column(name = "user_id")
    private Long userId;
}