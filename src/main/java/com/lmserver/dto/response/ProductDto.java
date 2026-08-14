package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmserver.entity.gg.Products;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Google Ads 产品 DTO — 继承 Products 实体，增加 JOIN 关联字段。
 * 用于分页列表接口，列表中的 packages/runnerIdList 由 Service 层批量填充。
 *
 * 注意：忽略父类的 runnerIds(JSON字符串列)/salesPerson(冗余列)，
 * 改用 runner_id_list→runner_ids(数组) 和 sales_person(名称) 对齐旧版 Map key。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"runnerIds", "salesPerson"})
public class ProductDto extends Products {

    /** MCC 名称 (JOIN mcc.name) */
    @TableField(exist = false)
    private String mccName;

    /** MCC 编码 (JOIN mcc.mcc_id) */
    @TableField(exist = false)
    private String mccCode;

    /** 商务名称 (JOIN sales_persons.name) — 旧版 key 为 "sales_person" */
    @TableField(exist = false)
    @JsonProperty("sales_person")
    private String salesPersonName;

    /** 成效素材数 (子查询 product_assets) */
    @TableField(exist = false)
    private int assetCount;

    /** 关联账户数 (子查询 accounts) */
    @TableField(exist = false)
    private int relatedAccountCount;

    /** 包列表（批量查询后回填） */
    @TableField(exist = false)
    private List<PackageDto> packages;

    /** 在跑人员 ID 列表（批量查询后回填）— 旧版 key 为 "runner_ids" */
    @TableField(exist = false)
    @JsonProperty("runner_ids")
    private List<Long> runnerIdList;
}
