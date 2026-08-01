package com.lmserver.service.ai;

/**
 * AI 视频生成 Provider 接口 — 策略模式，支持多种 AI 后端。
 * 实现类：Atlas(Seedance), Doubao(豆包), Veo(Google)。
 */
public interface AiVideoProvider {

    /** Provider 名称标识 */
    String getName();

    /**
     * 提交视频生成任务。
     * @param imagePath  输入图片路径
     * @param duration   视频时长（秒）
     * @param prompt     文本提示词
     * @param apiKey     API 密钥
     * @return 生成结果（视频URL或任务ID）
     */
    String generate(String imagePath, int duration, String prompt, String apiKey) throws Exception;

    /**
     * 查询任务状态。
     * @param taskId 任务 ID
     * @return 状态: pending/processing/completed/failed
     */
    String getStatus(String taskId) throws Exception;
}
