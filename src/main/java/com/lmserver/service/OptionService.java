package com.lmserver.service;

import java.util.List;

/**
 * 选项数据管理（agents / statuses / mcc-levels / sales-persons / regions）
 */
public interface OptionService {

    // ──────── 通用 ────────
    <T> List<T> list(String type, Long ownerId, String platform);
    <T> T getById(String type, Long id);
    <T> T create(String type, String name, Long ownerId, String platform);
    <T> T update(String type, Long id, String name);
    void delete(String type, Long id);
}
