package com.lmserver.exception;

import org.springframework.http.HttpStatus;

/** Exception class */
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
