package com.lmserver.entity.fb;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("fb_product_runners")
public class FbProductRunners {

    @TableField("product_id")
    /** 产品ID */
    private Long productId;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;
}