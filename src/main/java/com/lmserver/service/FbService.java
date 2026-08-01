package com.lmserver.service;

import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.*;

import java.util.List;

public interface FbService {
    // BM
    PagedResponse<FbBms> listBms(Long ownerId, int page, int size, String search, String status);
    FbBms getBmById(Long id);
    FbBms createBm(Long ownerId, String name, String bmId, String note);
    FbBms updateBm(Long id, String name, String note);
    void deleteBm(Long id);
    List<FbBms> bmOptions(Long ownerId);

    // Account
    PagedResponse<FbAccounts> listAccounts(Long ownerId, int page, int size, String search, Long statusId);
    FbAccounts getAccountById(Long id);
    FbAccounts createAccount(Long ownerId, String name, String accountId, Long statusId, String timezone);
    FbAccounts updateAccount(Long id, String name, Long statusId, String timezone);
    void deleteAccount(Long id);

    // Product
    PagedResponse<FbProducts> listProducts(Long ownerId, int page, int size, String search, String region);
    FbProducts getProductById(Long id);
    FbProducts createProduct(Long ownerId, String name, String kpi, String region, Long salesPersonId, Double ratio);
    FbProducts updateProduct(Long id, String name, String kpi, String region, Long salesPersonId, Double ratio);
    void deleteProduct(Long id);
    List<FbProducts> productOptions(Long ownerId);
}
