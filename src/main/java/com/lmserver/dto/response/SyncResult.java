package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * v1.5 Sheet 双向同步结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResult {
    private List<Map<String, Object>> toCreate;
    private List<Map<String, Object>> toUpdate;
    private List<Map<String, Object>> unchanged;
    private int created;
    private int updated;
    private boolean dryRun;
}
