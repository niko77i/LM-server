package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lmserver.entity.fb.FbPixelBms;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Facebook Pixel BM DTO — 继承 FbPixelBms 实体，增加像素计数字段。
 * 用于 /api/fb/pixel-bms/list 端点。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FbPixelBmDto extends FbPixelBms {

    /** 关联像素数（子查询） */
    @TableField(exist = false)
    private int pixelCount;
}
