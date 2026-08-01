package com.lmserver.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI 视频生成服务 — 策略模式管理多个 Provider。
 * 根据 provider 参数路由到对应的 AI 后端（atlas/doubao/veo）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiVideoService {

    private final List<AiVideoProvider> providers;

    /** 获取可用 Provider 列表 */
    public List<String> getProviderNames() {
        return providers.stream().map(AiVideoProvider::getName).toList();
    }

    /** 提交视频生成任务 */
    public String generate(String provider, String imagePath, int duration, String prompt, String apiKey) throws Exception {
        AiVideoProvider p = findProvider(provider);
        return p.generate(imagePath, duration, prompt, apiKey);
    }

    /** 查询任务状态 */
    public String getStatus(String provider, String taskId) throws Exception {
        AiVideoProvider p = findProvider(provider);
        return p.getStatus(taskId);
    }

    private AiVideoProvider findProvider(String name) {
        return providers.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知的 AI Provider: " + name));
    }
}
