package com.lmserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.entity.common.ImportHistory;
import com.lmserver.mapper.common.ImportHistoryMapper;
import com.lmserver.service.DataImportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据导入导出服务实现 — JSON 序列化/反序列化用户数据，记录导入历史。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportExportServiceImpl implements DataImportExportService {

    private final ImportHistoryMapper importHistoryMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String exportUserData(Long userId) {
        // TODO: Phase 5 实现完整导出逻辑
        log.info("用户 {} 导出数据", userId);
        return "{}";
    }

    @Override
    public int importUserData(Long userId, MultipartFile file) {
        try {
            String content = new String(file.getBytes());
            int count = 0;
            // TODO: Phase 5 实现完整导入逻辑
            ImportHistory history = new ImportHistory();
            history.setUserId(userId);
            history.setFileName(file.getOriginalFilename());
            history.setFileType("json");
            history.setStatus("success");
            history.setCreatedAt(LocalDateTime.now());
            importHistoryMapper.insert(history);
            log.info("用户 {} 导入数据: {}", userId, file.getOriginalFilename());
            return count;
        } catch (Exception e) {
            log.error("导入失败", e);
            return -1;
        }
    }

    @Override
    public List<ImportHistory> getImportHistory(Long userId) {
        return importHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ImportHistory>()
                        .eq(ImportHistory::getUserId, userId)
                        .orderByDesc(ImportHistory::getCreatedAt));
    }
}
