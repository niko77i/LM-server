/**
 * 文案管理服务接口 — 按地区和归属用户筛选的文案CRUD
 */

package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.Copywritings;
import java.util.List;
public interface CopywritingService {
    PagedResponse<Copywritings> list(Long ownerId, int page, int size, String region);
    Copywritings create(Long ownerId, String region, String content, Integer isPublic);
    Copywritings update(Long id, String region, String content, String effectiveness);
    void delete(Long id);
    void batchDelete(List<Long> ids);
}
