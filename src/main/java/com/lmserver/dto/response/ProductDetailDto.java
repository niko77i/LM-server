package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lmserver.entity.gg.ProductRunners;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 产品详情 DTO — 继承 ProductDto，增加详情接口特有字段。
 * 用于 ProductController.detail() 端点。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductDetailDto extends ProductDto {

    /** 关联账户简单列表（从 MCC 子树收集） */
    @TableField(exist = false)
    private List<Map<String, Object>> relatedAccounts;

    /** 账户状态分布 */
    @TableField(exist = false)
    private Map<String, Long> statusCount;

    /** 在跑人员详情 */
    @TableField(exist = false)
    private List<ProductRunners> runners;
}
