package com.lmserver.dto.sheets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FB 做表请求行数据 — 对齐 Python upsert_fb_reports 的 row 字段。
 *
 * <pre>
 * A=日期 | B=运营 | C=账户名称 | D=广告账户ID('前缀防科学计数法) | E=账号消耗 | F=留空
 * G=客户名称(产品名) | H=商务 | I=投放国家 | J=渠道号(线名) | K=留空 | L=代投比例(%字符串)
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbReportRow {
    private String accountName;   // C: 账户名称
    private String accountId;     // D: 广告账户ID
    private Double cost;          // E: 账号消耗
    private String channelNo;     // J: 渠道号(线名)
}
