package com.lmserver.entity.fb;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("fb_product_bms")
public class FbProductBms {

        
    /** 主键ID */
    private Long id;

    @TableField("product_id")
    /** 产品ID */
    private Long productId;

    @TableField("bm_id")
    /** BM ID */
    private Long bmId;

}