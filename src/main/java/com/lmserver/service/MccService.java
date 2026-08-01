package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Mcc;
import java.util.List;

/** Service interface */
public interface MccService {

    PagedResponse<Mcc> list(Long ownerId, int page, int size, String search, Long levelId);

    Mcc getById(Long id);

    Mcc create(Long ownerId, String name, String mccId, Long levelId, Long parentMccId);

    Mcc update(Long id, String name, Long levelId, Long parentMccId);

    void delete(Long id);

    List<Mcc> options(Long ownerId);
}
