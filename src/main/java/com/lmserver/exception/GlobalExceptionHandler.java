package com.lmserver.exception;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler - converts all exceptions to ApiResponse.fail().
 * Error codes: AUTH_xxx / VALID_xxx / BIZ_xxx / SYS_xxx.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("[Business] {} - {}", ex.getStatus().value(), ex.getMessage());
        String code = ex.getErrorCode() != null ? ex.getErrorCode() : "BIZ_000";
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.fail("[" + code + "] " + ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[AccessDenied] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("[" + ApiError.AUTH_FORBIDDEN + "] Access denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("[Validation] {}", msg);
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("[" + ApiError.VALID_INPUT + "] " + msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        log.error("[SystemError]", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("[" + ApiError.SERVER_ERROR + "] Internal server error"));
    }
}
