package com.lmserver.service;

import com.lmserver.dto.response.MccDto;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Mcc;

import java.util.List;

/**
 * MCC 管理服务接口 — 多条件分页查询(名称/ID搜索+等级筛选)
 */
public interface MccService {

    PagedResponse<MccDto> list(Long ownerId, int page, int size, String search, String level);

    MccDto detail(Long id);

    Mcc getById(Long id);

    Mcc create(Long ownerId, String name, String mccId, Long levelId, Long parentMccId);

    Mcc update(Long id, String name, Long levelId, Long parentMccId);

    void delete(Long id);

    List<Mcc> options(Long ownerId);
}
