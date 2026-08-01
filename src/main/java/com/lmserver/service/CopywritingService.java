package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.Copywritings;
import java.util.List;

/** Service interface */
public interface CopywritingService {
    PagedResponse<Copywritings> list(Long ownerId, int page, int size, String region);
    Copywritings create(Long ownerId, String region, String content, Integer isPublic);
    Copywritings update(Long id, String region, String content, String effectiveness);
    void delete(Long id);
    void batchDelete(List<Long> ids);
}
