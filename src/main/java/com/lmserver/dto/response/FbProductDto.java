package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lmserver.entity.fb.FbProducts;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Facebook 产品 DTO — 继承 FbProducts 实体，增加 JOIN 和集合字段。
 * 注意：bms/runnerIds/lines 由 Service 层批量查询填充。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FbProductDto extends FbProducts {

    /** 商务名称 (JOIN sales_persons.name) */
    @TableField(exist = false)
    private String salesPersonName;

    /** 关联 BM 列表（通过 fb_product_bms 中间表查询） */
    @TableField(exist = false)
    private List<BmBriefDto> bms;

    /** 在跑人员 ID 列表（通过 fb_product_runners 查询） */
    @TableField(exist = false)
    private List<Long> runnerIds;

    /** 广告线列表（通过 fb_lines 查询） */
    @TableField(exist = false)
    private List<LineBriefDto> lines;
}
