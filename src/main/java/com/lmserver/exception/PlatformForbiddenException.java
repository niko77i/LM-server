/**
 * 403 平台禁止异常 — 跨平台越权访问
 */

package com.lmserver.exception;

import org.springframework.http.HttpStatus;
public class PlatformForbiddenException extends BusinessException {
    public PlatformForbiddenException() {
        super("平台访问被拒绝", HttpStatus.FORBIDDEN, "PLATFORM_FORBIDDEN");
    }
}
