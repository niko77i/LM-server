package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 跨用户对比条目 DTO — 内嵌在 CrossUserResultDto 中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrossUserDto {

    private Long userId;

    private String displayName;

    private String username;

    private double totalCost;

    private long totalInstalls;

    private double totalInApp;

    private double avgCpi;

    private long reportDays;
}
