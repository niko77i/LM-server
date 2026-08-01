/**
 * 401 未认证异常 — Token 无效、过期或缺失
 */

package com.lmserver.exception;

import org.springframework.http.HttpStatus;
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
