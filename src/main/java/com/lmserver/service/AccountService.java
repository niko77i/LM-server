/**
 * 账户管理服务接口 — 多条件筛选(名称/账号ID/状态/MCC/代理)+软删除
 */

/**
 * 账户管理服务接口 — 多条件筛选(名称/账号ID/状态/MCC/代理)+软删除
 */

package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.dto.response.SyncResult;
import com.lmserver.entity.gg.Accounts;
import java.util.List;
import java.util.Map;
public interface AccountService {
    PagedResponse<Accounts> list(Long ownerId, int page, int size, String search, Long statusId, Long mccId, Long agentId);
    Accounts getById(Long id);
    Accounts create(Long ownerId, String name, String accountId, Long mccId, Long agentId, Long statusId, String timezone);
    Accounts update(Long id, String name, Long mccId, Long agentId, Long statusId, String timezone);
    void delete(Long id);
    List<Accounts> options(Long ownerId);
    /** v1.4: 账户状态变更时触发清账 */
    void tryClearAccount(Long accountId, Long operatorId, String newStatus);

    /** v1.5: Sheet双向同步 */
    SyncResult syncFromSheet(Long userId, String spreadsheetId, boolean dryRun);
}
