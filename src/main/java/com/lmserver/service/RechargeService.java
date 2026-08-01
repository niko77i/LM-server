package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.RechargeRecords;

/** Service interface */
public interface RechargeService {
    PagedResponse<RechargeRecords> list(Long userId, int page, int size, String accountId);
    RechargeRecords create(Long userId, String accountId, String amount, String operator, String status, Long agentId);
    RechargeRecords update(Long id, String amount, String status, String operator);
    void delete(Long id);
}
