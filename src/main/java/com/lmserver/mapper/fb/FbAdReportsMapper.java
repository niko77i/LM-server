package com.lmserver.mapper.fb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmserver.entity.fb.FbAdReports;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FbAdReportsMapper extends BaseMapper<FbAdReports> {

    /** ON DUPLICATE KEY UPDATE — 对齐设计文档 FB 提取保存逻辑 */
    @Insert("INSERT INTO fb_ad_reports (user_id, product_name, line_name, report_date, "
            + "account_name, account_id, cost, impressions, clicks, registrations, purchases, "
            + "cost_per_purchase, saved_at) "
            + "VALUES (#{r.userId}, #{r.productName}, #{r.lineName}, #{r.reportDate}, "
            + "#{r.accountName}, #{r.accountId}, #{r.cost}, #{r.impressions}, #{r.clicks}, "
            + "#{r.registrations}, #{r.purchases}, #{r.costPerPurchase}, NOW()) "
            + "ON DUPLICATE KEY UPDATE "
            + "account_name=VALUES(account_name), cost=VALUES(cost), "
            + "impressions=VALUES(impressions), clicks=VALUES(clicks), "
            + "registrations=VALUES(registrations), purchases=VALUES(purchases), "
            + "cost_per_purchase=VALUES(cost_per_purchase), updated_at=NOW()")
    int upsert(@Param("r") FbAdReports report);
}
