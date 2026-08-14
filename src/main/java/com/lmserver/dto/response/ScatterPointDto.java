package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 散点数据 DTO — 内嵌在 MultiAnalysisDto 中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScatterPointDto {

    private String name;

    private double x;

    private double y;

    /** 气泡大小（可选） */
    private double size;

    private double totalCost;
}
