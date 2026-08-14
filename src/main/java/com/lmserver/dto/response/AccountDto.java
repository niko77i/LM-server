package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lmserver.entity.gg.Accounts;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Google Ads 账户 DTO — 继承 Accounts 实体，增加 JOIN 关联字段。
 * 用于列表和详情接口，通过 Mapper XML 一条 SQL 完成关联查询。
 *
 * 字段命名：依赖全局 SNAKE_CASE 自动转 snake_case（agentName→agent_name）。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccountDto extends Accounts {

    /** MCC 名称 (JOIN mcc.name) → mcc_name */
    @TableField(exist = false)
    private String mccName;

    /** MCC 编码 (JOIN mcc.mcc_id) → mcc_code */
    @TableField(exist = false)
    private String mccCode;

    /** 代理名称 (JOIN agents.name) → agent_name */
    @TableField(exist = false)
    private String agentName;

    /** 状态名称 (JOIN account_statuses.name) → status_name */
    @TableField(exist = false)
    private String statusName;
}
