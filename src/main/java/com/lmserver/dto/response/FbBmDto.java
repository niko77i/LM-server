package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lmserver.entity.fb.FbBms;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Facebook BM DTO — 继承 FbBms 实体，增加类型和计数字段。
 * 用于 unified 端点（UNION ALL 合并 fb_bms + fb_pixel_bms）。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FbBmDto extends FbBms {

    /** BM 类型: normal / pixel_bm */
    @TableField(exist = false)
    private String bmType;

    /** 关联账户数 */
    @TableField(exist = false)
    private int accountCount;

    /** 关联像素数 */
    @TableField(exist = false)
    private int pixelCount;
}
