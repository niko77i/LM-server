package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 多维自由分析返回 DTO — 用于 /api/ad-reports/multi-analysis 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiAnalysisDto {

    /** 散点数据 */
    private List<ScatterPointDto> points;

    /** X 轴指标名 */
    private String xAxis;

    /** Y 轴指标名 */
    private String yAxis;

    /** 分组维度 */
    private String groupBy;

    /** X 轴均值 */
    private double xAvg;

    /** Y 轴均值 */
    private double yAvg;

    /** Pearson 相关系数 */
    private double pearsonR;

    /** 分析洞察文本 */
    private String insight;
}
