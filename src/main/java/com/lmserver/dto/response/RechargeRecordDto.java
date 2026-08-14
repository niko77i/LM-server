package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lmserver.entity.gg.RechargeRecords;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 充值记录 DTO — 继承 RechargeRecords 实体，增加代理名称关联字段。
 * 用于账户详情的充值记录列表，LEFT JOIN agents 表返回代理名称。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RechargeRecordDto extends RechargeRecords {

    /** 代理名称 (LEFT JOIN agents.name) → agent_name */
    @TableField(exist = false)
    private String agentName;
}
