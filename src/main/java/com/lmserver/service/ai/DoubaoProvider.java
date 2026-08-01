package com.lmserver.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 豆包 AI 视频 Provider — 火山方舟 Ark API，Seedance 1.5 Pro。
 * 接口: https://ark.cn-beijing.volces.com/api/v3
 */
@Slf4j
@Component
public class DoubaoProvider implements AiVideoProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.doubao.endpoint}")
    private String endpoint;

    @Override
    public String getName() { return "doubao"; }

    @Override
    public String generate(String imagePath, int duration, String prompt, String apiKey) throws Exception {
        String url = endpoint + "/video/generation";
        Map<String, Object> body = Map.of(
            "model", "doubao-seedance-1.5-pro",
            "input", Map.of("image_url", imagePath, "prompt", prompt, "duration", duration)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        ResponseEntity<Map> resp = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        Map<String, Object> result = resp.getBody();
        String taskId = result != null ? (String) result.getOrDefault("id", "") : "";
        log.info("[豆包] 任务已提交: {}", taskId);
        return taskId;
    }

    @Override
    public String getStatus(String taskId) throws Exception {
        String url = endpoint + "/video/result";
        Map<String, Object> body = Map.of("id", taskId);
        ResponseEntity<Map> resp = restTemplate.postForEntity(url, body, Map.class);
        Map<String, Object> result = resp.getBody();
        return result != null ? (String) result.getOrDefault("status", "pending") : "pending";
    }
}
