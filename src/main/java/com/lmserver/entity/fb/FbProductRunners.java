package com.lmserver.entity.fb;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: fbproductrunners */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("fb_product_runners")
public class FbProductRunners {

    @TableField("product_id")
    private Long productId;

    @TableField("user_id")
    private Long userId;
}