package com.lmserver.dto.sheets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * GG 做表行数据 — 对齐 Python upsert_zuobiao 的 14 列 A-N。
 *
 * <pre>
 * A=日期 | B=运营 | C=账户名称 | D=客户ID | E=账号消耗 | F=留空(报给客户)
 * G=产品名/养户 | H=商务/止戈 | I=投放国家 | J=广告系列 | K=留空(平台实际)
 * L=代投比例 | M=F*L(公式) | N=F-K+M(公式)
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZuobiaoRow {

    // ── 请求字段（对应 Python row dict）──
    private String account;       // C: 账户名称
    private String customerId;    // D: 客户ID（纯数字串）
    private double cost;          // E: 账号消耗
    private String campaign;      // J: 广告系列
    private boolean isYanghu;     // 是否为养户行
    private int impressions;      // 展示次数（DB同步用）
    private int clicks;           // 点击次数（DB同步用）
    private double installs;      // 安装数（DB同步用）
    private double inAppActions;  // 应用内操作（DB同步用）
    private double costPerInApp;  // 单次应用内操作成本（DB同步用）

    /**
     * 生成 14 列 Sheet 行数据。
     *
     * @param reportDate    A列 日期
     * @param operatorName  B列 运营
     * @param productName   G列 产品名（非养户行使用）
     * @param salesPerson   H列 商务（非养户行使用）
     * @param region        I列 投放国家
     * @param agencyRatio   L列 代投比例数值
     * @return 14 元素的 List（M/N 为 null，由调用方按行号填入公式）
     */
    public List<Object> toSheetRow(String reportDate, String operatorName,
            String productName, String salesPerson, String region, Integer agencyRatio) {
        String percentStr = agencyRatio != null ? agencyRatio + "%" : "";

        String gVal = isYanghu ? "养户" : (productName != null ? productName : "");
        String hVal = isYanghu ? "止戈" : (salesPerson != null ? salesPerson : "");
        String lVal = isYanghu ? "0%" : percentStr;

        List<Object> row = new ArrayList<>(14);
        row.add(reportDate != null ? reportDate : "");                    // A: 日期
        row.add(operatorName != null ? operatorName : "");                // B: 运营
        row.add(account != null ? account : "");                          // C: 账户名称
        row.add(customerId != null ? customerId : "");                    // D: 客户ID
        row.add(cost);                                                    // E: 账号消耗
        row.add("");                                                      // F: 留空(报给客户)
        row.add(gVal);                                                    // G: 产品名/养户
        row.add(hVal);                                                    // H: 商务/止戈
        row.add(region != null ? region : "");                            // I: 投放国家
        row.add(campaign != null ? campaign : "");                        // J: 广告系列
        row.add("");                                                      // K: 留空(平台实际)
        row.add(lVal);                                                    // L: 代投比例
        row.add(null);                                                    // M: =F*L (公式)
        row.add(null);                                                    // N: =F-K+M (公式)
        return row;
    }
}
