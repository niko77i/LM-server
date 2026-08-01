package com.lmserver.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Atlas Cloud AI 视频 Provider — 通过 Atlas 统一网关调用 Seedance/Kling 等模型。
 * 接口: https://api.atlascloud.ai/v1
 * 默认模型: seedance-2.0
 */
@Slf4j
@Component
public class AtlasProvider implements AiVideoProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.atlas.endpoint}")
    private String endpoint;

    @Value("${ai.atlas.default-model}")
    private String defaultModel;

    @Override
    public String getName() { return "atlas"; }

    @Override
    public String generate(String imagePath, int duration, String prompt, String apiKey) throws Exception {
        String url = endpoint + "/image-to-video";
        Map<String, Object> body = Map.of(
            "model", defaultModel,
            "image_url", imagePath,
            "duration", duration,
            "prompt", prompt
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        ResponseEntity<Map> resp = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        Map<String, Object> result = resp.getBody();
        String taskId = result != null ? (String) result.getOrDefault("id", "") : "";
        log.info("[Atlas] 任务已提交: {}", taskId);
        return taskId;
    }

    @Override
    public String getStatus(String taskId) throws Exception {
        String url = endpoint + "/tasks/" + taskId;
        ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = resp.getBody();
        return body != null ? (String) body.getOrDefault("status", "pending") : "pending";
    }
}
