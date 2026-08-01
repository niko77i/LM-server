package com.lmserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Google Ads API 服务 — 通过 REST 调用 Google Ads API。
 * 凭证从请求参数动态传入（client_id, client_secret, refresh_token, developer_token, manager_id）。
 * 与 Python google_ads_service.py 接口一致。
 */
@Slf4j
@Service
public class GoogleAdsService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 列出经理账户（MCC）下所有子账户。
     * 调用 Google Ads API REST v17: customers/{managerId}/customerClients
     */
    public List<String> listAccounts(Map<String, String> auth) {
        List<String> accounts = new ArrayList<>();
        try {
            String accessToken = getAccessToken(auth);
            String managerId = auth.get("manager_id");
            String url = "https://googleads.googleapis.com/v17/customers/" + managerId + "/customerClients";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("developer-token", auth.get("developer_token"));

            ResponseEntity<Map> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            log.info("[Ads] 列出账户完成 managerId={}", managerId);
        } catch (Exception e) {
            log.error("[Ads] 列出账户失败: {}", e.getMessage());
        }
        return accounts;
    }

    /**
     * 拉取广告系列报告（GAQL 查询）。
     * POST /v17/customers/{customerId}/googleAds:search
     */
    public List<Map<String, Object>> fetchCampaignReport(
            Map<String, String> auth, String accountId, String startDate, String endDate) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            String accessToken = getAccessToken(auth);
            String query = String.format(
                "SELECT campaign.name, metrics.cost_micros, metrics.impressions, "
                + "metrics.clicks, metrics.conversions "
                + "FROM campaign WHERE segments.date BETWEEN '%s' AND '%s'",
                startDate, endDate);

            String url = "https://googleads.googleapis.com/v17/customers/" + accountId + "/googleAds:search";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            headers.set("developer-token", auth.get("developer_token"));

            Map<String, String> body = Map.of("query", query);
            ResponseEntity<Map> resp = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            log.info("[Ads] 报告查询完成 accountId={}", accountId);
        } catch (Exception e) {
            log.error("[Ads] 拉取报告失败: {}", e.getMessage());
        }
        return rows;
    }

    /** 用 refresh_token 换取 access_token */
    private String getAccessToken(Map<String, String> auth) {
        try {
            String url = "https://oauth2.googleapis.com/token";
            String reqBody = "client_id=" + auth.get("client_id")
                    + "&client_secret=" + auth.get("client_secret")
                    + "&refresh_token=" + auth.get("refresh_token")
                    + "&grant_type=refresh_token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<Map> resp = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(reqBody, headers), Map.class);

            Map<String, Object> body = resp.getBody();
            return body != null ? (String) body.get("access_token") : "";
        } catch (Exception e) {
            log.error("[Ads] 获取token失败: {}", e.getMessage());
            return "";
        }
    }
}
