package com.lmserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一 API 响应 — 对齐 Python helpers.ok()。
 *
 * <p>关键行为：当 data 是 Map 时，展平到顶层（与 Python ok(dict) 一致）。
 * <pre>
 * ok(Map.of("items",[...],"total",10)) → {"success":true,"items":[...],"total":10}
 * ok(somePojo)                          → {"success":true,"data":somePojo}
 * ok()                                  → {"success":true}
 * fail("msg")                           → {"success":false,"error":"msg"}
 * </pre>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> fail(String error) {
        return new ApiResponse<>(false, null, error);
    }

    /**
     * 自定义序列化 — 对齐 Python helpers.ok 的展平行为。
     * Map 数据展平到顶层，普通对象保留 data 键。
     */
    @JsonValue
    @SuppressWarnings("unchecked")
    public Map<String, Object> toJson() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("success", success);
        if (error != null) json.put("error", error);
        if (data instanceof Map) {
            json.putAll((Map<String, Object>) data);
        } else if (data != null) {
            json.put("data", data);
        }
        return json;
    }
}
