package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 多轮 AI 对话结果 DTO — 用于 /api/ad-reports/multi-ai-chat 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiAiChatResultDto {

    private String question;

    /** 数据上下文（含 summary, top_campaigns, history） */
    private Map<String, Object> dataContext;

    private String suggestion;
}
