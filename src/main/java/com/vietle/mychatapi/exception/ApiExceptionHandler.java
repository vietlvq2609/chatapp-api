package com.vietle.mychatapi.exception;

import com.vietle.mychatapi.response.dto.ApiExceptionResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiExceptionResponseDTO> handleInvalidArgument(MethodArgumentNotValidException ex) {
        Map<String, Object> errorMap = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });

        ApiExceptionResponseDTO exceptionResponse = new ApiExceptionResponseDTO(
                "MethodArgumentNotValidException: Your request body is missing 1 or some mandatory arguments.",
                HttpStatus.BAD_REQUEST,
                errorMap
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiExceptionResponseDTO> handleApiException(ApiException ex) {
        ApiExceptionResponseDTO exceptionResponse = new ApiExceptionResponseDTO(
                ex.getMessage(),
                ex.getHttpStatus(),
                null,
                ex.getApiErrorType()
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
