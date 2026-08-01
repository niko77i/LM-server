package com.lmserver.entity.gg;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("product_runners")
public class ProductRunners {

    @TableField("product_id")
    private Long productId;

    @TableField("user_id")
    private Long userId;
}