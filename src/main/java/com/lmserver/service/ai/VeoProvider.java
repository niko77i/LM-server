package com.lmserver.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Google Veo AI 视频 Provider — Veo 3.1 Lite，完全免费，视频自带音频。
 * 接口: 通过 Google AI Studio 或 API 网关
 */
@Slf4j
@Component
public class VeoProvider implements AiVideoProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.veo.endpoint}")
    private String endpoint;

    @Override
    public String getName() { return "veo"; }

    @Override
    public String generate(String imagePath, int duration, String prompt, String apiKey) throws Exception {
        String url = endpoint + "/models/veo-3.1-lite-i2v:predict";
        Map<String, Object> body = Map.of(
            "instances", Map.of("image", Map.of("bytesBase64Encoded", imagePath),
                                 "prompt", prompt, "durationSeconds", duration)
        );
        ResponseEntity<Map> resp = restTemplate.exchange(
                url, HttpMethod.POST,
                new HttpEntity<>(body, createHeaders(apiKey)), Map.class);
        Map<String, Object> result = resp.getBody();
        String taskId = result != null ? (String) result.getOrDefault("name", "") : "";
        log.info("[Veo] 任务已提交: {}", taskId);
        return taskId;
    }

    @Override
    public String getStatus(String taskId) throws Exception {
        String url = endpoint + "/" + taskId;
        ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> body = resp.getBody();
        return body != null ? (String) body.getOrDefault("state", "pending") : "pending";
    }

    private HttpHeaders createHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }
}
