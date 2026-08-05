package com.lmserver.service;

import java.util.List;

/**
 * 选项数据管理（agents / statuses / mcc-levels / sales-persons / regions）。
 * 写操作要求 userId 用于校验 owner_id 权限。
 */
public interface OptionService {

    <T> List<T> list(String type, Long ownerId, String platform);
    <T> T getById(String type, Long id);
    <T> T create(String type, String name, Long ownerId, String platform);

    /** 更新 — userId 用于校验 owner_id */
    <T> T update(String type, Long id, String name, Long userId);

    /** 删除 — userId 用于校验 owner_id */
    void delete(String type, Long id, Long userId);
}
