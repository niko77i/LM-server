package com.lmserver.service;

import com.lmserver.entity.common.ImportHistory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据导入导出服务接口 — 用户级数据备份与恢复、导入历史查询。
 */
public interface DataImportExportService {

    /**
     * 导出当前用户的所有业务数据为 JSON 字符串。
     */
    String exportUserData(Long userId);

    /**
     * 从 JSON 文件导入用户数据，返回导入的记录数。
     */
    int importUserData(Long userId, MultipartFile file);

    /**
     * 获取用户的导入历史记录列表。
     */
    List<ImportHistory> getImportHistory(Long userId);
}
