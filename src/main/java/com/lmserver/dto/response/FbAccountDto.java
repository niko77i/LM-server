package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.lmserver.entity.fb.FbAccounts;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Facebook 广告账户 DTO — 继承 FbAccounts 实体，增加 JOIN 关联字段。
 * 注意：bms 列表需要通过 fb_account_bm 中间表查询，Service 层批量填充。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FbAccountDto extends FbAccounts {

    /** 状态名称 (JOIN account_statuses.name) */
    @TableField(exist = false)
    private String statusName;

    /** 关联的 BM 列表（通过 fb_account_bm 中间表查询） */
    @TableField(exist = false)
    private List<BmBriefDto> bms;
}
