package com.lmserver.dto.sheets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FB 做表行数据 — 12 列映射 A-L。
 * 列: 日期|运营|账户名称|广告账户ID|账号消耗|报给客户|客户名称|商务|投放国家|渠道号|平台实际|代投比例
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbReportRow {
    private String date;           // A
    private String operator;       // B
    private String accountName;    // C
    private String accountId;      // D
    private Double cost;           // E: 账号消耗
    private Double reportToClient; // F: 报给客户
    private String customerName;   // G
    private String salesPerson;    // H
    private String country;        // I
    private String channelNo;      // J
    private Double platformActual; // K: 平台实际
    private Double agencyRatio;    // L: 代投比例

    public java.util.List<Object> toSheetRow() {
        return java.util.List.of(
            date != null ? date : "",
            operator != null ? operator : "",
            accountName != null ? accountName : "",
            accountId != null ? accountId : "",
            cost != null ? cost : 0,
            reportToClient != null ? reportToClient : 0,
            customerName != null ? customerName : "",
            salesPerson != null ? salesPerson : "",
            country != null ? country : "",
            channelNo != null ? channelNo : "",
            platformActual != null ? platformActual : 0,
            agencyRatio != null ? agencyRatio : 0
        );
    }
}
