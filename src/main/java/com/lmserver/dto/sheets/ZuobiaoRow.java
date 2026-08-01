package com.lmserver.dto.sheets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GG 做表行数据 — 14 列映射 A-N。
 * 列: 日期|运营|客户名称|商务|投放国家|渠道号|系列名|包名|账户ID|素材图|落地页|账号消耗(¥)|广告系列|利润|客户实际消耗
 * M = F*L (利润), N = F-K+M (客户实际消耗)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZuobiaoRow {
    private String date;           // A: 日期
    private String operator;       // B: 运营
    private String customerName;   // C: 客户名称
    private String salesPerson;    // D: 商务
    private String country;        // E: 投放国家
    private String channelNo;      // F: 渠道号
    private String seriesName;     // G: 系列名
    private String packageName;    // H: 包名
    private String accountId;      // I: 账户ID
    private String imageUrl;       // J: 素材图
    private String landingPage;    // K: 落地页
    private Double accountCost;    // L: 账号消耗(¥)

    public java.util.List<Object> toSheetRow() {
        return java.util.List.of(
            date != null ? date : "",
            operator != null ? operator : "",
            customerName != null ? customerName : "",
            salesPerson != null ? salesPerson : "",
            country != null ? country : "",
            channelNo != null ? channelNo : "",
            seriesName != null ? seriesName : "",
            packageName != null ? packageName : "",
            accountId != null ? accountId : "",
            imageUrl != null ? imageUrl : "",
            landingPage != null ? landingPage : "",
            accountCost != null ? accountCost : 0
        );
    }
}
