package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.entity.common.ImportHistory;
import com.lmserver.mapper.common.ImportHistoryMapper;
import com.lmserver.mapper.gg.AccountsMapper;
import com.lmserver.mapper.gg.ProductsMapper;
import com.lmserver.mapper.gg.MccMapper;
import com.lmserver.service.DataImportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据导入导出服务实现 — JSON 序列化/反序列化用户数据，记录导入历史。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportExportServiceImpl implements DataImportExportService {

    private final ImportHistoryMapper importHistoryMapper;
    private final AccountsMapper accountsMapper;
    private final ProductsMapper productsMapper;
    private final MccMapper mccMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String exportUserData(Long userId) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("accounts", accountsMapper.selectList(new LambdaQueryWrapper<com.lmserver.entity.gg.Accounts>().eq(com.lmserver.entity.gg.Accounts::getOwnerId, userId)));
            data.put("products", productsMapper.selectList(new LambdaQueryWrapper<com.lmserver.entity.gg.Products>().eq(com.lmserver.entity.gg.Products::getOwnerId, userId)));
            data.put("mcc", mccMapper.selectList(new LambdaQueryWrapper<com.lmserver.entity.gg.Mcc>().eq(com.lmserver.entity.gg.Mcc::getOwnerId, userId)));
            data.put("exportTime", LocalDateTime.now().toString());
            log.info("用户 {} 导出数据", userId);
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) { log.error("导出失败", e); return "{}"; }
    }

    @Override
    public int importUserData(Long userId, MultipartFile file) {
        try {
            String content = new String(file.getBytes());
            Map<String, Object> data = objectMapper.readValue(content, Map.class);
            int count = 0;
            // 导入 accounts
            List<Map<String, Object>> accounts = (List<Map<String, Object>>) data.getOrDefault("accounts", List.of());
            for (var a : accounts) {
                try {
                    com.lmserver.entity.gg.Accounts acct = objectMapper.convertValue(a, com.lmserver.entity.gg.Accounts.class);
                    acct.setOwnerId(userId); acct.setCreatedAt(LocalDateTime.now()); acct.setUpdatedAt(LocalDateTime.now());
                    accountsMapper.insert(acct); count++;
                } catch (Exception ignored) {}
            }
            // 导入 products
            List<Map<String, Object>> products = (List<Map<String, Object>>) data.getOrDefault("products", List.of());
            for (var p : products) {
                try {
                    com.lmserver.entity.gg.Products prod = objectMapper.convertValue(p, com.lmserver.entity.gg.Products.class);
                    prod.setOwnerId(userId); prod.setCreatedAt(LocalDateTime.now());
                    productsMapper.insert(prod); count++;
                } catch (Exception ignored) {}
            }
            ImportHistory history = new ImportHistory();
            history.setUserId(userId); history.setFileName(file.getOriginalFilename());
            history.setFileType("json"); history.setStatus("success"); history.setCreatedAt(LocalDateTime.now());
            importHistoryMapper.insert(history);
            log.info("用户 {} 导入 {} 条数据", userId, count);
            return count;
        } catch (Exception e) { log.error("导入失败", e); return -1; }
    }

    @Override
    public List<ImportHistory> getImportHistory(Long userId) {
        return importHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ImportHistory>()
                        .eq(ImportHistory::getUserId, userId)
                        .orderByDesc(ImportHistory::getCreatedAt));
    }
}
