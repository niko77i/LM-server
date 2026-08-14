package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lmserver.entity.gg.Mcc;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * MCC DTO — 继承 Mcc 实体，增加 JOIN 关联字段。
 * 用于列表和详情接口。
 * 注意：totalAccountCount 需要 Java 层递归计算子树账户总数。
 *
 * 字段命名：依赖全局 SNAKE_CASE 自动转 snake_case（levelName→level_name）。
 * total_accounts 是 GG-Server 权威命名，is_owner 显式标注避免 Lombok is 前缀坑。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MccDto extends Mcc {

    /** 等级名称 (JOIN mcc_levels.name) → level_name */
    @TableField(exist = false)
    private String levelName;

    /** 直属账户数（SQL 子查询）→ direct_count */
    @TableField(exist = false)
    private long directCount;

    /** 子树账户总数（Java 层递归计算）→ total_accounts（GG-Server 权威命名） */
    @TableField(exist = false)
    @JsonProperty("total_accounts")
    private long totalAccountCount;

    /** 是否为当前用户所有 → is_owner（显式标注，避免 Lombok isOwner() 被识别为 owner） */
    @TableField(exist = false)
    @JsonProperty("is_owner")
    private boolean isOwner;
}
