package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI 分析结果 DTO — 用于 /api/ad-reports/analyze 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeResultDto {

    private String question;

    private Map<String, Object> dataContext;

    private String suggestion;
}
