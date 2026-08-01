package com.lmserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 结构化错误响应 — 对齐设计文档附录 C M7。
 * 包含错误码（前端 i18n）、错误消息、字段级校验错误。
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    /** 错误码，如 AUTH_001, VALID_001 */
    private final String code;

    /** 用户可读的错误消息 */
    private final String message;

    /** 字段名（参数校验失败时） */
    private final String field;

    private ApiError(String code, String message, String field) {
        this.code = code;
        this.message = message;
        this.field = field;
    }

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }

    public static ApiError field(String code, String field, String message) {
        return new ApiError(code, message, field);
    }

    // ────── 预定义错误码 ──────

    public static final String AUTH_INVALID = "AUTH_001";
    public static final String AUTH_EXPIRED = "AUTH_002";
    public static final String AUTH_FORBIDDEN = "AUTH_003";
    public static final String VALID_INPUT = "VALID_001";
    public static final String NOT_FOUND = "BIZ_001";
    public static final String PLATFORM_DENIED = "BIZ_002";
    public static final String SERVER_ERROR = "SYS_001";
}
