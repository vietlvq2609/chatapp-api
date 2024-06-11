package com.vietle.mychatapi.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException{
    private final HttpStatus httpStatus;
    private ApiErrorType apiErrorType;

    public ApiException(String message, HttpStatus httpStatus, ApiErrorType apiErrorType) {
        super(message);
        this.httpStatus = httpStatus;
        this.apiErrorType = apiErrorType;
    }

    public ApiException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, ApiErrorType.UNKNOWN_ERROR);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ApiErrorType getApiErrorType() {
        return apiErrorType;
    }
}
