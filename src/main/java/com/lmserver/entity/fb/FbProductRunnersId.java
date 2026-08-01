package com.lmserver.entity.fb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FbProductRunnersId implements Serializable {
    private Long productId;
    private Long userId;
}