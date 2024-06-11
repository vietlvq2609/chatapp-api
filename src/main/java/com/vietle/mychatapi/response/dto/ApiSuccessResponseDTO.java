package com.vietle.mychatapi.response.dto;

import org.springframework.http.HttpStatus;

public class ApiSuccessResponseDTO extends ApiResponseDTO {
    public ApiSuccessResponseDTO(HttpStatus httpStatus, Object payload) {
        super(true, httpStatus, payload);
    }
}
