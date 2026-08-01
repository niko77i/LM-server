package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.Accounts;
import java.util.List;

/** Service interface */
public interface AccountService {
    PagedResponse<Accounts> list(Long ownerId, int page, int size, String search, Long statusId, Long mccId, Long agentId);
    Accounts getById(Long id);
    Accounts create(Long ownerId, String name, String accountId, Long mccId, Long agentId, Long statusId, String timezone);
    Accounts update(Long id, String name, Long mccId, Long agentId, Long statusId, String timezone);
    void delete(Long id);
    List<Accounts> options(Long ownerId);
}
