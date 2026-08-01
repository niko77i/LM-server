/**
 * 充值管理服务接口 — 按账户ID筛选的充值记录查询
 */

package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.RechargeRecords;
public interface RechargeService {
    PagedResponse<RechargeRecords> list(Long userId, int page, int size, String accountId);
    RechargeRecords create(Long userId, String accountId, String amount, String operator, String status, Long agentId);
    RechargeRecords update(Long id, String amount, String status, String operator);
    void delete(Long id);
}
