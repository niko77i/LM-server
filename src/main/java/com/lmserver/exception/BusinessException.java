package com.lmserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
/**
 * 业务异常基类 — 携带 HTTP 状态码和可选错误码，由 GlobalExceptionHandler 统一转换为 ApiResponse
 */

/**
 * 业务异常基类 — 携带 HTTP 状态码和可选错误码，由 GlobalExceptionHandler 统一转换为 ApiResponse
 */

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST, null);
    }

    public BusinessException(String message, HttpStatus status) {
        this(message, status, null);
    }

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
