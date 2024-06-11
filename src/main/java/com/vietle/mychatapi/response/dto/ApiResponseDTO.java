package com.vietle.mychatapi.response.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Getter
public abstract class ApiResponseDTO {
    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final ZonedDateTime timestamp;
    private final Object payload;

    public ApiResponseDTO(boolean isSuccess, HttpStatus httpStatus, Object payload) {
        this.isSuccess = isSuccess;
        this.httpStatus = httpStatus;
        this.timestamp = ZonedDateTime.now();
        this.payload = payload;
    }
}