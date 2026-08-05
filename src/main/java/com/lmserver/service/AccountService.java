package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.dto.response.SyncResult;
import com.lmserver.entity.gg.Accounts;
import java.util.List;

/**
 * 账户管理服务接口 — 多条件筛选+软删除+清账+Sheet同步。
 * 所有写操作要求传入 userId 校验 owner_id 权限。
 */
public interface AccountService {
    PagedResponse<Accounts> list(Long ownerId, int page, int size, String search, Long statusId, Long mccId, Long agentId);
    Accounts getById(Long id);
    Accounts create(Long ownerId, String name, String accountId, Long mccId, Long agentId, Long statusId, String timezone);

    /** 更新账户 — userId 用于校验 owner_id */
    Accounts update(Long id, Long userId, String name, Long mccId, Long agentId, Long statusId, String timezone);

    /** 软删除 — userId 用于校验 owner_id */
    void delete(Long id, Long userId);

    /** 恢复 — userId 用于校验 owner_id */
    void restore(Long id, Long userId);

    /** 物理删除 — userId 用于校验 owner_id（需先软删除） */
    void permanentDelete(Long id, Long userId);

    /** 批量删除 — userId 用于逐条校验 owner_id */
    int batchDelete(List<Long> ids, Long userId);

    /** 批量更新 — userId 用于逐条校验 owner_id */
    int batchUpdate(List<Long> ids, Long userId, String name, Long mccId, Long agentId, Long statusId, String timezone);

    List<Accounts> options(Long ownerId);

    /** 账户状态变更时触发清账 */
    void tryClearAccount(Long accountId, Long operatorId, String newStatus);

    /** Sheet双向同步 — spreadsheet_id 从服务端获取，不信任客户端输入 */
    SyncResult syncFromSheet(Long userId, String spreadsheetId, boolean dryRun);
}
