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
    /** 产品ID */
    private Long productId;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;
}