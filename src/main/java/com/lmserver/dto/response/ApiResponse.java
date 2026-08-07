package com.lmserver.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * 统一 API 响应 — 对齐 Python helpers.ok()。
 *
 * <p>关键行为：当 data 是 Map 时，将 Map 的条目展平到顶层（与 Python ok(dict) 一致）。
 * <pre>
 * ok(Map.of("items",[...],"total",10)) → {"success":true,"items":[...],"total":10}
 * ok(somePojo)                          → {"success":true,"data":somePojo}
 * ok()                                  → {"success":true}
 * fail("msg")                           → {"success":false,"error":"msg"}
 * </pre>
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;

    @JsonIgnore
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
     * 展平输出：当 data 是 Map 时，将其条目提升到顶层（与 Python helpers.ok 行为一致）。
     * 当 data 是普通对象时，包装在 "data" 键下。
     */
    @JsonAnyGetter
    @SuppressWarnings("unchecked")
    public Map<String, Object> getFlatData() {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        }
        if (data != null) {
            return Map.of("data", data);
        }
        return null;
    }
}
