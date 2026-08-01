package com.lmserver.entity.fb;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: fbproductbms */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("fb_product_bms")
public class FbProductBms {

        
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("bm_id")
    private Long bmId;

}