package com.vietle.mychatapi.response.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import com.vietle.mychatapi.exception.ApiErrorType;

@Getter
public class ApiExceptionResponseDTO extends ApiResponseDTO {
    private final String message;
    private final String errorType;

    public ApiExceptionResponseDTO(String message, HttpStatus httpStatus, Object payload, ApiErrorType errorType) {
        super(false, httpStatus, payload);
        this.message = message;
        this.errorType = errorType.name();
    }

    public ApiExceptionResponseDTO(String message, HttpStatus httpStatus, Object payload) {
        super(false, httpStatus, payload);
        this.message = message;
        this.errorType = ApiErrorType.UNKNOWN_ERROR.name();
    }
}
