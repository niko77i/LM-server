/**
 * MCC 管理服务接口 — 多条件分页查询(名称/ID搜索+等级筛选)
 */

package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Mcc;
import java.util.List;
public interface MccService {

    PagedResponse<Mcc> list(Long ownerId, int page, int size, String search, Long levelId);

    Mcc getById(Long id);

    Mcc create(Long ownerId, String name, String mccId, Long levelId, Long parentMccId);

    Mcc update(Long id, String name, Long levelId, Long parentMccId);

    void delete(Long id);

    List<Mcc> options(Long ownerId);
}
